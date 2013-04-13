# --- !Ups

CREATE TABLE workflows (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO workflows (name) VALUES ('WORK_EMP_DEFAULT');

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    realname VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
	organization VARCHAR(128),
	location VARCHAR(128),
	timezone VARCHAR(64) NOT NULL DEFAULT 'America/Los_Angeles',
	title VARCHAR(128),
	url VARCHAR(128),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, realname, email) VALUES ('admin', '$2a$12$kjx926AcdoK38pJBotfoROSVJxNkIkwxqHVHODiSLhfv94a4KPKuW', 'admin', 'admin@admin.com');
INSERT INTO users (username, password, realname, email) VALUES ('anonymous', 'anonymous', 'EMP_USER_ANONYMOUS', 'anonymous@example.com');

CREATE TABLE user_tokens (
  token VARCHAR(32) NOT NULL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id),
  comment VARCHAR(255),
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE groups (
    id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Put admin into groups
INSERT INTO groups (name) VALUES ('emperor-admins');
INSERT INTO groups (name) VALUES ('emperor-users');

CREATE TABLE group_users (
    id SERIAL PRIMARY KEY,
    group_id INT NOT NULL REFERENCES groups(id),
    user_id INT NOT NULL REFERENCES users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT group_users_grou_user_uniq UNIQUE(group_id, user_id)
);

INSERT INTO group_users (group_id, user_id) VALUES (1, 1);
INSERT INTO group_users (group_id, user_id) VALUES (2, 1);

CREATE TABLE ticket_resolutions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_resolutions (name) VALUES ('TICK_RESO_FIXED');
INSERT INTO ticket_resolutions (name) VALUES ('TICK_RESO_WONTFIX');

CREATE TABLE ticket_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_statuses (name) VALUES ('TICK_STATUS_OPEN');
INSERT INTO ticket_statuses (name) VALUES ('TICK_STATUS_IN_PROG');
INSERT INTO ticket_statuses (name) VALUES ('TICK_STATUS_CLOSED');

CREATE TABLE ticket_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(6) NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_types (name, color) VALUES ('TICK_TYPE_BUG', 'f2dede');
INSERT INTO ticket_types (name, color) VALUES ('TICK_TYPE_IMPROVEMENT', 'd9edf7');
INSERT INTO ticket_types (name, color) VALUES ('TICK_TYPE_MILESTONE', 'dff0d8');

CREATE TABLE ticket_link_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    invertable BOOLEAN DEFAULT true,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_link_types (name, invertable) VALUES ('TICK_LINK_BLOCKS', true);
INSERT INTO ticket_link_types (name, invertable) VALUES ('TICK_LINK_CONTAINS', true);
INSERT INTO ticket_link_types (name, invertable) VALUES ('TICK_LINK_RELATED', false);

CREATE TABLE ticket_severities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(6) NOT NULL,
    position INT NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_severities (name, color, position) VALUES ('TICK_SEV_DIFFICULT', 'f2dede', 100);
INSERT INTO ticket_severities (name, color, position) VALUES ('TICK_SEV_NORMAL', 'd9edf7', 66);
INSERT INTO ticket_severities (name, color, position) VALUES ('TICK_SEV_TRIVIAL', 'dff0d8', 33);

CREATE TABLE ticket_priorities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(6) NOT NULL,
    position INT NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO ticket_priorities (name, color, position) VALUES ('TICK_PRIO_HIGH', 'fcf8e3', 100);
INSERT INTO ticket_priorities (name, color, position) VALUES ('TICK_PRIO_NORMAL', 'd9edf7', 66);
INSERT INTO ticket_priorities (name, color, position) VALUES ('TICK_PRIO_LOW', 'dff0d8', 33);

CREATE TABLE workflow_statuses (
    id SERIAL PRIMARY KEY,
    workflow_id INT NOT NULL REFERENCES workflows(id),
    status_id INT NOT NULL REFERENCES ticket_statuses(id),
    position INT,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 1, 25);
INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 2, 50);
INSERT INTO workflow_statuses (workflow_id, status_id, position) VALUES (1, 3, 75);

CREATE TABLE permission_schemes (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Core scheme
INSERT INTO permission_schemes (name) VALUES ('EMP_PERM_SCHEME_CORE');
-- Default scheme
INSERT INTO permission_schemes (name) VALUES ('EMP_PERM_SCHEME_DEFAULT');

CREATE TABLE projects (
  id SERIAL PRIMARY KEY,
  owner_id INT REFERENCES users (id),
  default_priority_id INT REFERENCES ticket_priorities (id),
  default_severity_id INT REFERENCES ticket_severities (id),
  default_ticket_type_id INT REFERENCES ticket_types (id),
  default_assignee SMALLINT,
	permission_scheme_id INT NOT NULL REFERENCES permission_schemes(id),
  name VARCHAR(255) NOT NULL,
  pkey VARCHAR(16) NOT NULL UNIQUE,
  workflow_id INT NOT NULL REFERENCES workflows(id),
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tickets (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id),
  project_id INT NOT NULL REFERENCES projects(id),
  project_ticket_id INT NOT NULL,
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT tickets_project_ticket_id_project_uniq UNIQUE(project_ticket_id, project_id)
);

CREATE TABLE ticket_data (
	id SERIAL PRIMARY KEY,
	ticket_id INT NOT NULL REFERENCES tickets(id),
  user_id INT NOT NULL REFERENCES users(id),
  priority_id INT NOT NULL REFERENCES ticket_priorities(id),
  resolution_id INT REFERENCES ticket_resolutions(id),
  assignee_id INT REFERENCES users(id),
  attention_id INT REFERENCES users(id),
  severity_id INT NOT NULL REFERENCES ticket_severities(id),
  status_id INT NOT NULL REFERENCES workflow_statuses(id),
  type_id INT NOT NULL REFERENCES ticket_types(id),
  position INT,
  summary VARCHAR(255) NOT NULL,
  description TEXT,
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_links (
    id SERIAL PRIMARY KEY,
    link_type_id INT NOT NULL REFERENCES ticket_link_types(id),
    parent_ticket_id INT NOT NULL REFERENCES tickets(id),
    child_ticket_id INT NOT NULL REFERENCES tickets(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ticket_links_link_uniq UNIQUE(link_type_id, parent_ticket_id, child_ticket_id)
);

CREATE TABLE ticket_comments (
  id SERIAL PRIMARY KEY,
  ticket_id INT NOT NULL REFERENCES tickets(id),
  type VARCHAR(32) DEFAULT 'comment',
  user_id INT NOT NULL REFERENCES users(id),
  content TEXT,
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW full_tickets AS
SELECT
  t.id      AS id,
  td.id     AS data_id,
  t.project_ticket_id AS project_ticket_id,
  t.user_id     AS user_id,
  uc.realname   AS user_realname,
  t.project_id  AS project_id,
  p.name        AS project_name,
  p.pkey        AS project_key,
  td.priority_id AS priority_id,
  tp.name       AS priority_name,
  tp.color      AS priority_color,
  tp.position   AS priority_position,
  td.resolution_id AS resolution_id,
  tr.name       AS resolution_name,
  td.assignee_id AS assignee_id,
  uass.realname AS assignee_realname,
  td.attention_id  AS attention_id,
  uatt.realname AS attention_realname,
  td.severity_id AS severity_id,
  sevs.name     AS severity_name,
  sevs.color    AS severity_color,
  sevs.position AS severity_position,
  td.status_id   AS status_id,
  ts.name       AS status_name,
  td.type_id     AS type_id,
  tt.name       AS type_name,
  tt.color      AS type_color,
  ws.status_id  AS workflow_status_id,
  td.position    AS position,
  td.summary     AS summary,
  td.description AS description,
  t.date_created  AS date_created
FROM ticket_data td
  JOIN tickets t ON t.id = td.ticket_id
  JOIN projects p ON p.id = t.project_id
  JOIN ticket_priorities tp ON tp.id = td.priority_id
  JOIN ticket_severities sevs ON sevs.id = td.severity_id
  JOIN workflow_statuses ws ON ws.id = td.status_id
  JOIN ticket_statuses ts ON ts.id = ws.status_id
  JOIN ticket_types tt ON tt.id = td.type_id
  JOIN users uc ON uc.id = t.user_id
  JOIN users urep ON urep.id = t.user_id
  LEFT JOIN users uass ON uass.id = td.assignee_id
  LEFT JOIN users uatt ON uatt.id = td.attention_id
  LEFT JOIN ticket_resolutions tr ON tr.id = td.resolution_id
WHERE td.id IN (
  SELECT MAX(id) FROM ticket_data GROUP BY ticket_id
);

CREATE VIEW full_all_tickets AS
SELECT
  t.id      AS id,
  td.id     AS data_id,
  t.project_ticket_id AS project_ticket_id,
  t.user_id     AS user_id,
  uc.realname   AS user_realname,
  t.project_id  AS project_id,
  p.name        AS project_name,
  p.pkey        AS project_key,
  td.priority_id AS priority_id,
  tp.name       AS priority_name,
  tp.color      AS priority_color,
  tp.position   AS priority_position,
  td.resolution_id AS resolution_id,
  tr.name       AS resolution_name,
  td.assignee_id AS assignee_id,
  uass.realname AS assignee_realname,
  td.attention_id  AS attention_id,
  uatt.realname AS attention_realname,
  td.severity_id AS severity_id,
  sevs.name     AS severity_name,
  sevs.color    AS severity_color,
  sevs.position AS severity_position,
  td.status_id   AS status_id,
  ts.name       AS status_name,
  td.type_id     AS type_id,
  tt.name       AS type_name,
  tt.color      AS type_color,
  ws.status_id  AS workflow_status_id,
  td.position    AS position,
  td.summary     AS summary,
  td.description AS description,
  t.date_created  AS date_created
FROM tickets t
  JOIN ticket_data td ON td.ticket_id = t.id
  JOIN projects p ON p.id = t.project_id
  JOIN ticket_priorities tp ON tp.id = td.priority_id
  JOIN ticket_severities sevs ON sevs.id = td.severity_id
  JOIN workflow_statuses ws ON ws.id = td.status_id
  JOIN ticket_statuses ts ON ts.id = ws.status_id
  JOIN ticket_types tt ON tt.id = td.type_id
  JOIN users uc ON uc.id = t.user_id
  JOIN users urep ON urep.id = t.user_id
  LEFT JOIN users uass ON uass.id = td.assignee_id
  LEFT JOIN users uatt ON uatt.id = td.attention_id
  LEFT JOIN ticket_resolutions tr ON tr.id = td.resolution_id;

CREATE TABLE permissions (
  name VARCHAR(32) NOT NULL PRIMARY KEY UNIQUE,
  global BOOLEAN NOT NULL DEFAULT false
);

INSERT INTO permissions (name, global) VALUES ('PERM_GLOBAL_ADMIN', true);
INSERT INTO permissions (name, global) VALUES ('PERM_GLOBAL_LOGIN', true);
INSERT INTO permissions (name, global) VALUES ('PERM_GLOBAL_PROJECT_CREATE', true);

INSERT INTO permissions (name) VALUES ('PERM_PROJECT_ADMIN');
INSERT INTO permissions (name) VALUES ('PERM_PROJECT_BROWSE');

INSERT INTO permissions (name) VALUES ('PERM_TICKET_COMMENT');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_CREATE');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_EDIT');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_LINK');
INSERT INTO permissions (name) VALUES ('PERM_TICKET_RESOLVE');

CREATE TABLE permission_scheme_users (
  id SERIAL PRIMARY KEY,
  permission_scheme_id INT NOT NULL REFERENCES permission_schemes(id),
  permission_id VARCHAR(32) NOT NULL REFERENCES permissions(name),
  user_id INT NOT NULL REFERENCES users(id),
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT permission_scheme_users_permission_user_uniq UNIQUE(permission_scheme_id, user_id, permission_id)
);

CREATE TABLE permission_scheme_groups (
  id SERIAL PRIMARY KEY,
  permission_scheme_id INT REFERENCES permission_schemes(id),
  permission_id VARCHAR(32) NOT NULL REFERENCES permissions(name),
  group_id INT NOT NULL REFERENCES groups(id),
  date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT permission_scheme_groups_permission_group_uniq UNIQUE(permission_scheme_id, group_id, permission_id)
);

-- Give emperor-admins admin permission
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (1, 'PERM_GLOBAL_ADMIN', 1);
-- Give emperor-users login permission
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (1, 'PERM_GLOBAL_LOGIN', 2);

-- Give all other permissions to emperor-users
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_PROJECT_BROWSE', 2);
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_TICKET_COMMENT', 2);
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_TICKET_CREATE', 2);
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_TICKET_EDIT', 2);
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_TICKET_LINK', 2);
INSERT INTO permission_scheme_groups (permission_scheme_id, permission_id, group_id) VALUES (2, 'PERM_TICKET_RESOLVE', 2);

-- Create a view of all the permissions for easy selection!
CREATE VIEW full_permissions AS
  SELECT p.id as project_id, p.pkey AS project_key, psg.permission_id as permission_id, gu.user_id as user_id, CONCAT('permission_scheme_groups:',psg.id) AS source FROM projects p JOIN permission_scheme_groups psg ON psg.permission_scheme_id=p.permission_scheme_id JOIN group_users gu ON gu.group_id = psg.group_id
  UNION
  SELECT p.id as project_id, p.pkey AS project_key, psu.permission_id as permission_id, psu.user_id as user_id,CONCAT('permission_scheme_users:',psu.id) AS source FROM projects p JOIN permission_scheme_users psu ON psu.permission_scheme_id=p.permission_scheme_id;

INSERT INTO projects (name, pkey, workflow_id, permission_scheme_id) VALUES ('Emperor Core', 'EMPCORE', 1, 1);


# --- !Downs