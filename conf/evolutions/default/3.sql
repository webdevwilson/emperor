# --- !Ups

# Make the new emperor-users group
INSERT INTO groups (name, date_created) VALUES ('emperor-users', UTC_TIMESTAMP());

# And the new emperor-admins group
INSERT INTO groups (name, date_created) VALUES ('emperor-admins', UTC_TIMESTAMP());

# Add everyone to the new group
SELECT id INTO @emp_user_group_id FROM groups WHERE name='emperor-users';
INSERT INTO group_users (group_id, user_id, date_created)
  SELECT @emp_user_group_id, id, UTC_TIMESTAMP() FROM users;

# Add admin to the new group
SELECT id INTO @emp_admin_group_id FROM groups WHERE name='emperor-admins';
INSERT INTO group_users (group_id, user_id, date_created)
  SELECT @emp_admin_group_id, id, UTC_TIMESTAMP() FROM users WHERE username='admin';

# Make the new anonymous user (after we setup the other group, anonymous is in no groups!)
INSERT INTO users (username, password, realname, email, date_created) VALUES ('anonymous', 'anonymous', 'EMP_USER_ANONYMOUS', 'anonymous@example.com', UTC_TIMESTAMP());

# Start permissions stuff
CREATE TABLE permissions (
  name VARCHAR(32) NOT NULL,
  PRIMARY KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

INSERT INTO permissions (name) VALUES ('PERM_OVERALL_ADMIN');
INSERT INTO permissions (name) VALUES ('PERM_OVERALL_LOGIN');

INSERT INTO permissions (name) VALUES ('PERM_PROJECT_ADMIN');
INSERT INTO permissions (name) VALUES ('PERM_PROJECT_BROWSE');
INSERT INTO permissions (name) VALUES ('PERM_PROJECT_CREATE');

INSERT INTO permissions (name) VALUES ('PERM_TICKET_CREATE');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_EDIT');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_LINK');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_RESOLVE');

CREATE TABLE permission_schemes (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  date_created DATETIME NOT NULL,
  PRIMARY KEY(id),
  UNIQUE KEY(name)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE permission_scheme_users (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  permission_scheme_id INT UNSIGNED NOT NULL,
  permission_id VARCHAR(32) NOT NULL,
  user_id INT UNSIGNED NOT NULL,
  date_created DATETIME NOT NULL,
  PRIMARY KEY(id),
  UNIQUE KEY(permission_scheme_id, user_id, permission_id),
  FOREIGN KEY (permission_id) REFERENCES permissions(name),
  FOREIGN KEY (permission_scheme_id) REFERENCES permission_schemes(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

CREATE TABLE permission_scheme_groups (
  id INT UNSIGNED AUTO_INCREMENT NOT NULL,
  permission_scheme_id INT UNSIGNED NOT NULL,
  permission_id VARCHAR(32) NOT NULL,
  group_id INT UNSIGNED NOT NULL,
  date_created DATETIME NOT NULL,
  PRIMARY KEY(id),
  UNIQUE KEY(permission_scheme_id, group_id, permission_id),
  FOREIGN KEY (permission_id) REFERENCES permissions(name),
  FOREIGN KEY (permission_scheme_id) REFERENCES permission_schemes(id),
  FOREIGN KEY (group_id) REFERENCES groups(id)
) ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

# Setup the default scheme
INSERT INTO permission_schemes (name, date_created) VALUES ('EMP_PERM_SCHEME_DEFAULT', UTC_TIMESTAMP());
# Get the scheme's id ready for use
SELECT id INTO @emp_default_scheme FROM permission_schemes WHERE name='EMP_PERM_SCHEME_DEFAULT';

# Give emperor-admins admin permission
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_OVERALL_ADMIN', @emp_admin_group_id, UTC_TIMESTAMP());

# Give emperor-users login permission
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_OVERALL_LOGIN', @emp_user_group_id, UTC_TIMESTAMP());
# Give all other permissions to emperor-users
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_PROJECT_BROWSE', @emp_user_group_id, UTC_TIMESTAMP());
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_TICKET_CREATE', @emp_user_group_id, UTC_TIMESTAMP());
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_TICKET_EDIT', @emp_user_group_id, UTC_TIMESTAMP());
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_TICKET_LINK', @emp_user_group_id, UTC_TIMESTAMP());
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id, date_created) VALUES (@emp_default_scheme, 'PERM_TICKET_RESOLVE', @emp_user_group_id, UTC_TIMESTAMP());

# Add permission_scheme_id to projects
ALTER TABLE projects ADD COLUMN permission_scheme_id INT UNSIGNED NOT NULL DEFAULT '1';
# Set all projects to use this default scheme, since it's the only one.
UPDATE projects SET permission_scheme_id=@emp_default_scheme;
# Drop the default
ALTER TABLE projects MODIFY permission_scheme_id INT UNSIGNED NOT NULL;
# Add FK constraint
ALTER TABLE projects ADD CONSTRAINT `fk_permission_scheme_id_permission_schemes` FOREIGN KEY (permission_scheme_id) REFERENCES permission_schemes(id);

# Create a view of all the permissions for easy selection!
CREATE VIEW full_permissions AS
  SELECT p.id as project_id, psg.permission_id as permission_id, gu.user_id as user_id, CONCAT('permission_scheme_groups:',psg.id) AS source FROM projects p JOIN permission_scheme_groups psg ON psg.permission_scheme_id=p.permission_scheme_id JOIN group_users gu ON gu.group_id = psg.group_id
  UNION
  SELECT p.id as project_id, psu.permission_id as permission_id, psu.user_id as user_id,CONCAT('permission_scheme_users:',psu.id) AS source FROM projects p JOIN permission_scheme_users psu ON psu.permission_scheme_id=p.permission_scheme_id;

# --- !Downs

DROP VIEW full_permissions;

ALTER TABLE projects DROP FOREIGN KEY `fk_permission_scheme_id_permission_schemes`;
ALTER TABLE projects DROP COLUMN permission_scheme_id;

DELETE FROM users where realname='EMP_USER_ANONYMOUS';

DROP TABLE permission_scheme_groups;
DROP TABLE permission_scheme_users;
DROP TABLE permission_schemes;
DROP TABLE permissions;

SELECT id INTO @emp_user_group_id FROM groups WHERE name='emperor-users';
DELETE FROM group_users WHERE group_id=@emp_user_group_id;
DELETE FROM groups WHERE id=@emp_user_group_id;

SELECT id INTO @emp_admin_group_id FROM groups WHERE name='emperor-admins';
DELETE FROM group_users WHERE group_id=@emp_admin_group_id;
DELETE FROM groups WHERE id=@emp_admin_group_id;

