# --- !Ups

INSERT INTO ticket_types (name, color, date_created) VALUES ("TICK_TYPE_MILESTONE", "dff0d8", UTC_TIMESTAMP());

# --- !Downs

DELETE FROM ticket_types WHERE name="TICK_TYPE_MILESTONE";
