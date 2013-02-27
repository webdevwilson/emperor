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
    color VARCHAR(6) NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_types (name, color, date_created) VALUES ("TICK_TYPE_BUG", "f2dede", UTC_TIMESTAMP());
INSERT INTO ticket_types (name, color, date_created) VALUES ("TICK_TYPE_IMPROVEMENT", "d9edf7", UTC_TIMESTAMP());
INSERT INTO ticket_types (name, color, date_created) VALUES ("TICK_TYPE_MILESTONE", "dff0d8", UTC_TIMESTAMP());

CREATE TABLE ticket_link_types (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    invertable BOOLEAN DEFAULT 1,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_link_types (name, invertable, date_created) VALUES ("TICK_LINK_BLOCKS", 1, UTC_TIMESTAMP());
INSERT INTO ticket_link_types (name, invertable, date_created) VALUES ("TICK_LINK_CONTAINS", 1, UTC_TIMESTAMP());
INSERT INTO ticket_link_types (name, invertable, date_created) VALUES ("TICK_LINK_RELATED", 0, UTC_TIMESTAMP());

CREATE TABLE ticket_severities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(6) NOT NULL,
    position INT NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_severities (name, color, position, date_created) VALUES ("TICK_SEV_DIFFICULT", "f2dede", 100, UTC_TIMESTAMP());
INSERT INTO ticket_severities (name, color, position, date_created) VALUES ("TICK_SEV_NORMAL", "d9edf7", 66, UTC_TIMESTAMP());
INSERT INTO ticket_severities (name, color, position, date_created) VALUES ("TICK_SEV_TRIVIAL", "dff0d8", 33, UTC_TIMESTAMP());

CREATE TABLE ticket_priorities (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(6) NOT NULL,
    position INT NOT NULL,
    date_created DATETIME NOT NULL,
    PRIMARY KEY(id),
    UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_priorities (name, color, position, date_created) VALUES ("TICK_PRIO_HIGH", "fcf8e3", 100, UTC_TIMESTAMP());
INSERT INTO ticket_priorities (name, color, position, date_created) VALUES ("TICK_PRIO_NORMAL", "d9edf7", 66, UTC_TIMESTAMP());
INSERT INTO ticket_priorities (name, color, position, date_created) VALUES ("TICK_PRIO_LOW", "dff0d8", 33, UTC_TIMESTAMP());

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

ALTER TABLE ticket_links ADD UNIQUE link_parent_child_idx (link_type_id, parent_ticket_id, child_ticket_id);

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

CREATE VIEW full_tickets AS
SELECT
  t.id      AS id,
  t.ticket_id   AS ticket_id,
  t.user_id     AS user_id,
  uc.realname   AS user_realname,
  t.project_id  AS project_id,
  p.name        AS project_name,
  t.priority_id AS priority_id,
  tp.name       AS priority_name,
  tp.color      AS priority_color,
  t.resolution_id AS resolution_id,
  tr.name       AS resolution_name,
  t.assignee_id AS assignee_id,
  uass.realname AS assignee_realname,
  t.attention_id  AS attention_id,
  uatt.realname AS attention_realname,
  t.reporter_id AS reporter_id,
  urep.realname AS reporter_realname,
  t.severity_id AS severity_id,
  sevs.name     AS severity_name,
  sevs.color    AS severity_color,
  t.status_id   AS status_id,
  ts.name       AS status_name,
  t.type_id     AS type_id,
  tt.name       AS type_name,
  tt.color      AS type_color,
  ws.status_id  AS workflow_status_id,
  t.position    AS position,
  t.summary     AS summary,
  t.description AS description,
  t.date_created  AS date_created
FROM tickets t
  JOIN projects p ON p.id = t.project_id
  JOIN ticket_priorities tp ON tp.id = t.priority_id
  JOIN ticket_severities sevs ON sevs.id = t.severity_id
  JOIN workflow_statuses ws ON ws.id = t.status_id
  JOIN ticket_statuses ts ON ts.id = ws.status_id
  JOIN ticket_types tt ON tt.id = t.type_id
  JOIN users uc ON uc.id = t.user_id
  JOIN users urep ON urep.id = t.reporter_id
  LEFT JOIN users uass ON uass.id = t.assignee_id
  LEFT JOIN users uatt ON uatt.id = t.attention_id
  LEFT JOIN ticket_resolutions tr ON tr.id = t.resolution_id
WHERE t.id IN (
  SELECT MAX(id) FROM tickets GROUP BY ticket_id
);

CREATE VIEW full_all_tickets AS
SELECT
  t.id      AS id,
  t.ticket_id   AS ticket_id,
  t.user_id     AS user_id,
  uc.realname   AS user_realname,
  t.project_id  AS project_id,
  p.name        AS project_name,
  t.priority_id AS priority_id,
  tp.name       AS priority_name,
  tp.color      AS priority_color,
  t.resolution_id AS resolution_id,
  tr.name       AS resolution_name,
  t.assignee_id AS assignee_id,
  uass.realname AS assignee_realname,
  t.attention_id  AS attention_id,
  uatt.realname AS attention_realname,
  t.reporter_id AS reporter_id,
  urep.realname AS reporter_realname,
  t.severity_id AS severity_id,
  sevs.name     AS severity_name,
  sevs.color    AS severity_color,
  t.status_id   AS status_id,
  ts.name       AS status_name,
  t.type_id     AS type_id,
  tt.name       AS type_name,
  tt.color      AS type_color,
  ws.status_id  AS workflow_status_id,
  t.position    AS position,
  t.summary     AS summary,
  t.description AS description,
  t.date_created  AS date_created
FROM tickets t
  JOIN projects p ON p.id = t.project_id
  JOIN ticket_priorities tp ON tp.id = t.priority_id
  JOIN ticket_severities sevs ON sevs.id = t.severity_id
  JOIN workflow_statuses ws ON ws.id = t.status_id
  JOIN ticket_statuses ts ON ts.id = ws.status_id
  JOIN ticket_types tt ON tt.id = t.type_id
  JOIN users uc ON uc.id = t.user_id
  JOIN users urep ON urep.id = t.reporter_id
  LEFT JOIN users uass ON uass.id = t.assignee_id
  LEFT JOIN users uatt ON uatt.id = t.attention_id
  LEFT JOIN ticket_resolutions tr ON tr.id = t.resolution_id;
