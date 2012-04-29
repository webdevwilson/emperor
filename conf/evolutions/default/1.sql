# --- !Ups

CREATE TABLE projects (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE groups (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE group_users (
    id INT UNSIGNED AUTO_INCREMENT,
    group_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_resolutions (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_resolutions (name) VALUES ("TICK_RESO_FIXED");
INSERT INTO ticket_resolutions (name) VALUES ("TICK_RESO_WONTFIX");

CREATE TABLE ticket_statuses (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_statuses (name) VALUES ("TICK_STATUS_OPEN");
INSERT INTO ticket_statuses (name) VALUES ("TICK_STATUS_CLOSED");

CREATE TABLE ticket_types (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_BUG");
INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_IMPROVEMENT");
INSERT INTO ticket_types (name) VALUES ("TICK_TYPE_MILESTONE");

CREATE TABLE ticket_link_types (
    id INT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO ticket_link_types (name) VALUES ("TICK_LINK_BLOCKS");

CREATE TABLE tickets (
    id INT UNSIGNED AUTO_INCREMENT,
    ticket_resolution_id INT UNSIGNED NOT NULL,
    ticket_status_id INT UNSIGNED NOT NULL,
    ticket_type_id INT UNSIGNED NOT NULL,
    position INT,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    PRIMARY KEY(id),
    FOREIGN KEY (ticket_resolution_id) REFERENCES ticket_resolutions(id),
    FOREIGN KEY (ticket_status_id) REFERENCES ticket_statuses(id),
    FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE project_tickets (
    id INT UNSIGNED AUTO_INCREMENT,
    ticket_id INT UNSIGNED NOT NULL,
    project_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(project_id) REFERENCES projects(id),
    FOREIGN KEY(ticket_id) REFERENCES tickets(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE ticket_links (
    id INT UNSIGNED AUTO_INCREMENT,
    link_type_id INT UNSIGNED NOT NULL,
    parent_ticket_id INT UNSIGNED NOT NULL,
    child_ticket_id INT UNSIGNED NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(link_type_id) REFERENCES ticket_link_types(id),
    FOREIGN KEY(parent_ticket_id) REFERENCES tickets(id),
    FOREIGN KEY(child_ticket_id) REFERENCES tickets(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;