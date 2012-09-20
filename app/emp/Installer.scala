package emp

import java.io.{BufferedWriter,File,FileWriter}
import java.sql.{Connection,DriverManager}
import java.util.UUID
import play.api._
import play.api.db.evolutions._
import scala.collection.mutable.HashMap

object Installer {

  val prompts = Map(
    "installdir"  -> "Installation directory",
    "dbhost"      -> "Database host",
    "dbport"      -> "Database port",
    "dbname"      -> "Database name",
    "dbuser"      -> "Database username",
    "dbpass"      -> "Database password"
  )

  val defaults = Map(
    "installdir" -> "/opt/emperor",
    "dbhost"  -> "localhost",
    "dbport"  -> "3306",
    "dbname"  -> "emperor",
    "dbuser"  -> "root",
    "dbpass"  -> ""
  )

  def main(args: Array[String]) {

    var config = getConfig(defaults)

    println()
    println(config)
    var confirm = Console.readLine("Are these settings correct?: ")
    if(!confirm.equalsIgnoreCase("y")) {
      println("Exiting, re-run installer")
      return
    }

    println("Settings confirmed.")

    writeConfig(config)
    // XXX confirm settings, loop until confirmed
    val conn = getConnection(config)

    conn match {
      case Some(c) => {
        createDatabase(c, config("dbname"))
      }
      case None => throw new RuntimeException("Unable to connect to the database")
    }
    // Close the connection (if we have one)
    conn.map { c => c.close }
  }

  /**
   * Create the database and play the evolutions against it.
   */
  def createDatabase(conn: Connection, dbname: String) {
    val st = conn.prepareStatement("CREATE DATABASE " + dbname + " CHARACTER SET utf8 COLLATE utf8_unicode_ci;")
    st.execute
    val path = new File("/Users/gphat/src/emperor/conf/test.conf")
    // val application = new Application(path, this.getClass.getClassLoader, None, Mode.Prod)
    // Play.start(application)
    OfflineEvolutions.applyScript(
      appPath     = path,
      classloader = this.getClass().getClassLoader(),
      dbName      = "default"
    )
  }

  /**
   * Connect to the database
   */
  def getConnection(config: Map[String,String]): Option[Connection] = {
    try {
      Class.forName("com.mysql.jdbc.Driver")
      Some(
        DriverManager.getConnection(
          makeDSN(config), config("dbuser"), config("dbpass")
        )
      )
    } catch {
      case ex: Exception => {
        ex.printStackTrace
        None
      }
    }
  }

  /**
   * Prompt for various configuration tidbits.
   */
  def getConfig(defaults: Map[String,String]): Map[String,String] = {

    defaults.map { v =>
      val input = Console.readLine(prompts(v._1) + " [" + defaults(v._1) + "]: ")
      val realValue = input match {
        case "" => defaults(v._1)
        case iv => iv
      }
      (v._1 -> realValue)
    }
  }

  /**
   * Generate a DSN from configuration.
   */
  def makeDSN(config: Map[String,String]): String = {

    "jdbc:mysql://" + config("dbhost") + ":" + config("dbport") + "/" + config("dbpass") + "?useUnicode=yes&characterEncoding=UTF-8&useOldAliasMetadataBehavior=true"
  }

  /**
   * Write out configuration information
   */
  def writeConfig(config: Map[String,String]) {

    val installdir = new File(config("installdir"))
    installdir.mkdir
    val file = new FileWriter(config("installdir") + "/emperor.conf")
    val writer = new BufferedWriter(file)
    val secret = UUID.randomUUID.toString.replace("-","")
    writer.write("application.secret=\"" + secret + "\"")
    writer.newLine
    writer.write("db.default.driver=com.mysql.jdbc.Driver")
    writer.newLine
    writer.write("db.default.url=" + makeDSN(config))
    writer.newLine
    writer.write("db.default.user=" + config("dbuser"))
    writer.newLine
    writer.write("db.default.password=\"" + config("dbpass") + "\"")
    writer.newLine
    writer.write("emperor.directory=\"" + config("installdir") + "\"")
    writer.newLine
    writer.close
  }
}