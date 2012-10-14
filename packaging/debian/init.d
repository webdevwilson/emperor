#!/bin/sh
#
# /etc/init.d/emperor -- startup script for Emperor
#
# Written by Cory Watson <cwatson@emperorapp.com>.
#
### BEGIN INIT INFO
# Provides:          emperor
# Required-Start:    $all
# Required-Stop:     $all
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Starts emperor
# Description:       Starts emperor using start-stop-daemon
### END INIT INFO

set -e

PATH=/bin:/usr/bin:/sbin:/usr/sbin
NAME=emperor
DESC="Emperor"
DEFAULT=/etc/default/$NAME

if [ `id -u` -ne 0 ]; then
  echo "You need root privileges to run this script"
  exit 1
fi


. /lib/lsb/init-functions

if [ -r /etc/default/rcS ]; then
  . /etc/default/rcS
fi


# The following variables can be overwritten in $DEFAULT

# Run Emperor as this user ID and group ID
EMP_USER=emperor
EMP_GROUP=emperor

# The first existing directory is used for JAVA_HOME (if JAVA_HOME is not defined in $DEFAULT)
JDK_DIRS="/usr/lib/jvm/java-7-oracle /usr/lib/jvm/java-7-openjdk /usr/lib/jvm/java-7-openjdk-amd64/ /usr/lib/jvm/java-7-openjdk-i386/ /usr/lib/jvm/java-6-sun /usr/lib/jvm/java-6-openjdk"

# Look for the right JVM to use
for jdir in $JDK_DIRS; do
    if [ -r "$jdir/bin/java" -a -z "${JAVA_HOME}" ]; then
  JAVA_HOME="$jdir"
    fi
done
export JAVA_HOME

# Maximum number of open files
MAX_OPEN_FILES=65535

# Maximum amount of locked memory
#MAX_LOCKED_MEMORY=

# Emperor log directory
LOG_DIR=/var/log/$NAME

# Emperor data directory
DATA_DIR=/var/lib/$NAME

# Emperor configuration directory
CONF_DIR=/etc/$NAME

# Emperor configuration file (emperor.conf)
CONF_FILE=$CONF_DIR/emperor.conf

# End of variables that can be overwritten in $DEFAULT

# overwrite settings from default file
if [ -f "$DEFAULT" ]; then
  . "$DEFAULT"
fi

# Define other required variables
PID_FILE=/var/run/$NAME.pid
DAEMON=$EMP_HOME/bin/emperor
DAEMON_OPTS="-p $PID_FILE"


# Check DAEMON exists
test -x $DAEMON || exit 0

case "$1" in
  start)
  if [ -z "$JAVA_HOME" ]; then
    log_failure_msg "no JDK found - please set JAVA_HOME"
    exit 1
  fi

  if [ -n "$MAX_LOCKED_MEMORY" -a -z "$ES_HEAP_SIZE" ]; then
    log_failure_msg "MAX_LOCKED_MEMORY is set - ES_HEAP_SIZE must also be set"
    exit 1
  fi

  log_daemon_msg "Starting $DESC"

  if start-stop-daemon --test --start --pidfile "$PID_FILE" \
    --user "$ES_USER" --exec "$JAVA_HOME/bin/java" \
    >/dev/null; then

    # Prepare environment
    mkdir -p "$LOG_DIR" "$DATA_DIR" "$WORK_DIR" && chown "$EMP_USER":"$EMP_GROUP" "$LOG_DIR" "$DATA_DIR" "$WORK_DIR"
    touch "$PID_FILE" && chown "$ES_USER":"$ES_GROUP" "$PID_FILE"

    if [ -n "$MAX_OPEN_FILES" ]; then
      ulimit -n $MAX_OPEN_FILES
    fi

    if [ -n "$MAX_LOCKED_MEMORY" ]; then
      ulimit -l $MAX_LOCKED_MEMORY
    fi

    # Start Daemon
    start-stop-daemon --start -b --user "$ES_USER" -c "$ES_USER" --pidfile "$PID_FILE" --exec /bin/bash -- -c "$DAEMON $DAEMON_OPTS"

    sleep 1
    if start-stop-daemon --test --start --pidfile "$PID_FILE" \
      --user "$ES_USER" --exec "$JAVA_HOME/bin/java" \
      >/dev/null; then
      if [ -f "$PID_FILE" ]; then
        rm -f "$PID_FILE"
      fi
      log_end_msg 1
    else
      log_end_msg 0
    fi

  else
      log_progress_msg "(already running)"
      log_end_msg 0
  fi
  ;;
  stop)
  log_daemon_msg "Stopping $DESC"

  set +e
  if [ -f "$PID_FILE" ]; then
    start-stop-daemon --stop --pidfile "$PID_FILE" \
      --user "$ES_USER" \
      --retry=TERM/20/KILL/5 >/dev/null
    if [ $? -eq 1 ]; then
      log_progress_msg "$DESC is not running but pid file exists, cleaning up"
    elif [ $? -eq 3 ]; then
      PID="`cat $PID_FILE`"
      log_failure_msg "Failed to stop $DESC (pid $PID)"
      exit 1
    fi
    rm -f "$PID_FILE"
  else
    log_progress_msg "(not running)"
  fi
  log_end_msg 0
  set -e
  ;;
  status)
  set +e
  start-stop-daemon --test --start --pidfile "$PID_FILE" \
    --user "$ES_USER" --exec "$JAVA_HOME/bin/java" \
    >/dev/null 2>&1
  if [ "$?" = "0" ]; then

    if [ -f "$PID_FILE" ]; then
        log_success_msg "$DESC is not running, but pid file exists."
      exit 1
    else
        log_success_msg "$DESC is not running."
      exit 3
    fi
  else
    log_success_msg "$DESC is running with pid `cat $PID_FILE`"
  fi
  set -e
    ;;
  restart|force-reload)
  if [ -f "$PID_FILE" ]; then
    $0 stop
    sleep 1
  fi
  $0 start
  ;;
  *)
  log_success_msg "Usage: $0 {start|stop|restart|force-reload|status}"
  exit 1
  ;;
esac

exit 0