# --- !Ups

ALTER TABLE projects ADD COLUMN owner_id INT UNSIGNED;

ALTER TABLE projects ADD CONSTRAINT `fk_owner_id_users` FOREIGN KEY (owner_id) REFERENCES users (id);

ALTER TABLE projects ADD COLUMN default_priority_id INT UNSIGNED;
ALTER TABLE projects ADD CONSTRAINT `fk_priority_id_priorities` FOREIGN KEY (default_priority_id) REFERENCES ticket_priorities (id);

ALTER TABLE projects ADD COLUMN default_severity_id INT UNSIGNED;
ALTER TABLE projects ADD CONSTRAINT `fk_severity_id_severities` FOREIGN KEY (default_severity_id) REFERENCES ticket_severities (id);

ALTER TABLE projects ADD COLUMN default_ticket_type_id INT UNSIGNED;
ALTER TABLE projects ADD CONSTRAINT `fk_type_id_types` FOREIGN KEY (default_ticket_type_id) REFERENCES ticket_types (id);

ALTER TABLE projects ADD COLUMN default_assignee TINYINT;

# --- !Downs

ALTER TABLE projects DROP FOREIGN KEY `fk_owner_id_users`;
ALTER TABLE projects DROP COLUMN owner_id;

ALTER TABLE projects DROP FOREIGN KEY `fk_priority_id_priorities`;
ALTER TABLE projects DROP COLUMN default_priority_id;

ALTER TABLE projects DROP FOREIGN KEY `fk_severity_id_severities`;
ALTER TABLE projects DROP COLUMN default_severity_id;

ALTER TABLE projects DROP FOREIGN KEY `fk_type_id_types`;
ALTER TABLE projects DROP COLUMN default_ticket_type_id;

ALTER TABLE projects DROP COLUMN default_assignee;