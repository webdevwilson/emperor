# --- !Ups

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
  t.proposed_resolution_id AS proposed_resolution_id,
  ptr.name      AS proposed_resolution_name,
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
  LEFT JOIN ticket_resolutions ptr ON ptr.id = t.proposed_resolution_id
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
  t.proposed_resolution_id AS proposed_resolution_id,
  ptr.name      AS proposed_resolution_name,
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
  LEFT JOIN ticket_resolutions ptr ON ptr.id = t.proposed_resolution_id;

# --- !Downs

DROP VIEW full_all_tickets;
DROP VIEW full_tickets;
