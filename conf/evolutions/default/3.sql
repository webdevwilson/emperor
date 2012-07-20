# --- !Ups

INSERT INTO ticket_types (name, color, date_created) VALUES ("TICK_TYPE_MILESTONE", "dff0d8", UTC_TIMESTAMP());

ALTER TABLE ticket_links ADD UNIQUE link_parent_child_idx (link_type_id, parent_ticket_id, child_ticket_id);

INSERT INTO ticket_link_types (name, date_created) VALUES ("TICK_LINK_BLOCKED_BY", UTC_TIMESTAMP());
INSERT INTO ticket_link_types (name, date_created) VALUES ("TICK_LINK_CONTAINS", UTC_TIMESTAMP());
INSERT INTO ticket_link_types (name, date_created) VALUES ("TICK_LINK_CONTAINED", UTC_TIMESTAMP());
INSERT INTO ticket_link_types (name, date_created) VALUES ("TICK_LINK_RELATED", UTC_TIMESTAMP());

# --- !Downs

DELETE FROM ticket_types WHERE name="TICK_TYPE_MILESTONE";

ALTER TABLE ticket_links DROP INDEX link_parent_child_idx;

DELETE FROM ticket_link_types WHERE name="TICK_LINK_BLOCKED_BY";
DELETE FROM ticket_link_types WHERE name="TICK_LINK_CONTAINS";
DELETE FROM ticket_link_types WHERE name="TICK_LINK_CONTAINED";
DELETE FROM ticket_link_types WHERE name="TICK_LINK_RELATED";
