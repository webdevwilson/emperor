# --- !Ups

CREATE TABLE workflows (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO workflows (name, date_created) VALUES ('WORK_EMP_DEFAULT', UTC_TIMESTAMP());

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(username)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO users (username, password, realname, email, date_created) VALUES ('admin', '$2a$12$kjx926AcdoK38pJBotfoROSVJxNkIkwxqHVHODiSLhfv94a4KPKuW', 'admin', 'admin@admin.com', UTC_TIMESTAMP());

CREATE TABLE groups (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE group_users (
    id INT UNSIGNED AUTO_INCREMENT,
    group_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE roles (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO roles (name, date_created) VALUES ('ROLE_QA', UTC_TIMESTAMP());
INSERT INTO roles (name, date_created) VALUES ('ROLE_DEVELOPER', UTC_TIMESTAMP());

CREATE TABLE ticket_resolutions (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_resolutions (name, date_created) VALUES ("TICK_RESO_FIXED", UTC_TIMESTAMP());
INSERT INTO ticket_resolutions (name, date_created) VALUES ("TICK_RESO_WONTFIX", UTC_TIMESTAMP());

CREATE TABLE ticket_statuses (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_statuses (name, date_created) VALUES ("TICK_STATUS_OPEN", UTC_TIMESTAMP());
INSERT INTO ticket_statuses (name, date_created) VALUES ("TICK_STATUS_IN_PROG", UTC_TIMESTAMP());
INSERT INTO ticket_statuses (name, date_created) VALUES ("TICK_STATUS_CLOSED", UTC_TIMESTAMP());

CREATE TABLE ticket_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_types (name, date_created) VALUES ("TICK_TYPE_BUG", UTC_TIMESTAMP());
INSERT INTO ticket_types (name, date_created) VALUES ("TICK_TYPE_IMPROVEMENT", UTC_TIMESTAMP());
INSERT INTO ticket_types (name, date_created) VALUES ("TICK_TYPE_MILESTONE", UTC_TIMESTAMP());

CREATE TABLE ticket_link_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_link_types (name, date_created) VALUES ("TICK_LINK_BLOCKS", UTC_TIMESTAMP());

CREATE TABLE ticket_severities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_severities (name, position, date_created) VALUES ("TICK_SEV_DIFFICULT", 100, UTC_TIMESTAMP());
INSERT INTO ticket_severities (name, position, date_created) VALUES ("TICK_SEV_NORMAL", 66, UTC_TIMESTAMP());
INSERT INTO ticket_severities (name, position, date_created) VALUES ("TICK_SEV_TRIVIAL", 33, UTC_TIMESTAMP());

CREATE TABLE ticket_priorities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_priorities (name, position, date_created) VALUES ("TICK_PRIO_HIGH", 100, UTC_TIMESTAMP());
INSERT INTO ticket_priorities (name, position, date_created) VALUES ("TICK_PRIO_NORMAL", 66, UTC_TIMESTAMP());
INSERT INTO ticket_priorities (name, position, date_created) VALUES ("TICK_PRIO_LOW", 33, UTC_TIMESTAMP());

CREATE TABLE workflow_statuses (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    workflow_id INT UNSIGNED NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    position INT,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(workflow_id) REFERENCES workflows(id),
    FOREIGN KEY(status_id) REFERENCES ticket_statuses(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO workflow_statuses (workflow_id, status_id, position, date_created) VALUES (1, 1, 25, UTC_TIMESTAMP());
INSERT INTO workflow_statuses (workflow_id, status_id, position, date_created) VALUES (1, 2, 50, UTC_TIMESTAMP());
INSERT INTO workflow_statuses (workflow_id, status_id, position, date_created) VALUES (1, 3, 75, UTC_TIMESTAMP());

CREATE TABLE projects (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    pkey VARCHAR(16) NOT NULL UNIQUE,
    sequence_current INT UNSIGNED NOT NULL DEFAULT 0,
    workflow_id INT UNSIGNED NOT NULL,
    date_created DATETIME NOT NULL,
    FOREIGN KEY (workflow_id) REFERENCES workflows(id),
    PRIMARY KEY(id),
    UNIQUE KEY(pkey)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE project_role_users (
    id INT UNSIGNED AUTO_INCREMENT,
    project_id INT UNSIGNED NOT NULL,
    role_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE tickets (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    ticket_id VARCHAR(255) NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    project_id INT UNSIGNED NOT NULL,
    priority_id INT UNSIGNED NOT NULL,
    resolution_id INT UNSIGNED,
    proposed_resolution_id INT UNSIGNED,
    assignee_id INT UNSIGNED,
    attention_id INT UNSIGNED,
    reporter_id INT UNSIGNED NOT NULL,
    severity_id INT UNSIGNED NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    type_id INT UNSIGNED NOT NULL,
    position INT,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    INDEX(ticket_id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (priority_id) REFERENCES ticket_priorities(id),
    FOREIGN KEY (resolution_id) REFERENCES ticket_resolutions(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (assignee_id) REFERENCES users(id),
    FOREIGN KEY (attention_id) REFERENCES users(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (severity_id) REFERENCES ticket_severities(id),
    FOREIGN KEY (status_id) REFERENCES workflow_statuses(id),
    FOREIGN KEY (type_id) REFERENCES ticket_types(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_links (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    link_type_id INT UNSIGNED NOT NULL,
    parent_ticket_id VARCHAR(64) NOT NULL,
    child_ticket_id VARCHAR(64) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(link_type_id) REFERENCES ticket_link_types(id),
    FOREIGN KEY(parent_ticket_id) REFERENCES tickets(ticket_id),
    FOREIGN KEY(child_ticket_id) REFERENCES tickets(ticket_id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_comments (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  ticket_id VARCHAR(64) NOT NULL,
  user_id INT UNSIGNED NOT NULL,
  content TEXT,
  date_created DATETIME NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(ticket_id) REFERENCES tickets(ticket_id),
  FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_stacks (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    ticket_id VARCHAR(64) NOT NULL,
    position INT NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(ticket_id) REFERENCES tickets(ticket_id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;
