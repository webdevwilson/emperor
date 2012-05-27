# --- !Ups

CREATE TABLE workflows (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO workflows (name) VALUES ('WORK_EMP_DEFAULT');

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(username)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO users (username, password, realname, email) VALUES ('admin', '$2a$12$kjx926AcdoK38pJBotfoROSVJxNkIkwxqHVHODiSLhfv94a4KPKuW', 'admin', 'admin@admin.com');

CREATE TABLE groups (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE group_users (
    id INT UNSIGNED AUTO_INCREMENT,
    group_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE roles (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO roles (name) VALUES ('ROLE_QA');
INSERT INTO roles (name) VALUES ('ROLE_DEVELOPER');

CREATE TABLE ticket_resolutions (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_resolutions (name) VALUES ("TICK_RESO_FIXED");
INSERT INTO ticket_resolutions (name) VALUES ("TICK_RESO_WONTFIX");

CREATE TABLE ticket_statuses (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_statuses (name) VALUES ("TICK_STATUS_OPEN");
INSERT INTO ticket_statuses (name) VALUES ("TICK_STATUS_IN_PROG");
INSERT INTO ticket_statuses (name) VALUES ("TICK_STATUS_CLOSED");

CREATE TABLE ticket_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_BUG");
INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_IMPROVEMENT");
INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_MILESTONE");

CREATE TABLE ticket_link_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_link_types (name) VALUES ("TICK_LINK_BLOCKS");

CREATE TABLE ticket_severities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_severities (name, position) VALUES ("TICK_SEV_DIFFICULT", 100);
INSERT INTO ticket_severities (name, position) VALUES ("TICK_SEV_NORMAL", 66);
INSERT INTO ticket_severities (name, position) VALUES ("TICK_SEV_TRIVIAL", 33);

CREATE TABLE ticket_priorities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_priorities (name, position) VALUES ("TICK_PRIO_HIGH", 100);
INSERT INTO ticket_priorities (name, position) VALUES ("TICK_PRIO_NORMAL", 66);
INSERT INTO ticket_priorities (name, position) VALUES ("TICK_PRIO_LOW", 33);

CREATE TABLE workflow_statuses (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    workflow_id INT UNSIGNED NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    position INT,
    PRIMARY KEY(id),
    FOREIGN KEY(workflow_id) REFERENCES workflows(id),
    FOREIGN KEY(status_id) REFERENCES ticket_statuses(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 1, 25);
INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 2, 50);
INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 3, 75);

CREATE TABLE projects (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    pkey VARCHAR(16) NOT NULL UNIQUE,
    workflow_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (workflow_id) REFERENCES workflows(id),
    PRIMARY KEY(id),
    UNIQUE KEY(pkey)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE project_role_users (
    id INT UNSIGNED AUTO_INCREMENT,
    project_id INT UNSIGNED NOT NULL,
    role_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE tickets (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    project_id INT UNSIGNED NOT NULL,
    priority_id INT UNSIGNED NOT NULL,
    resolution_id INT UNSIGNED,
    proposed_resolution_id INT UNSIGNED,
    reporter_id INT UNSIGNED NOT NULL,
    severity_id INT UNSIGNED NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    type_id INT UNSIGNED NOT NULL,
    position INT,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (priority_id) REFERENCES ticket_priorities(id),
    FOREIGN KEY (resolution_id) REFERENCES ticket_resolutions(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (severity_id) REFERENCES ticket_severities(id),
    FOREIGN KEY (status_id) REFERENCES workflow_statuses(id),
    FOREIGN KEY (type_id) REFERENCES ticket_types(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_links (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    link_type_id INT UNSIGNED NOT NULL,
    parent_ticket_id INT UNSIGNED NOT NULL,
    child_ticket_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(link_type_id) REFERENCES ticket_link_types(id),
    FOREIGN KEY(parent_ticket_id) REFERENCES tickets(id),
    FOREIGN KEY(child_ticket_id) REFERENCES tickets(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_comments (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  ticket_id INT UNSIGNED NOT NULL,
  user_id INT UNSIGNED NOT NULL,
  content TEXT,
  PRIMARY KEY(id),
  FOREIGN KEY(ticket_id) REFERENCES tickets(id),
  FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_history (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    ticket_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    project_id INT UNSIGNED NOT NULL,
    priority_id INT UNSIGNED NOT NULL,
    resolution_id INT UNSIGNED,
    proposed_resolution_id INT UNSIGNED,
    reporter_id INT UNSIGNED NOT NULL,
    severity_id INT UNSIGNED NOT NULL,
    status_id INT UNSIGNED NOT NULL,
    type_id INT UNSIGNED NOT NULL,
    position INT,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (priority_id) REFERENCES ticket_priorities(id),
    FOREIGN KEY (resolution_id) REFERENCES ticket_resolutions(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (severity_id) REFERENCES ticket_severities(id),
    FOREIGN KEY (status_id) REFERENCES workflow_statuses(id),
    FOREIGN KEY (type_id) REFERENCES ticket_types(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;
