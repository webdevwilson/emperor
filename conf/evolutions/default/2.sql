# --- !Ups
 
ALTER TABLE ticket_history ADD CONSTRAINT `ticket_history_assignee_to_users` FOREIGN KEY (assignee_id) REFERENCES users(id);
ALTER TABLE ticket_history ADD CONSTRAINT `ticket_history_attention_to_users` FOREIGN KEY (attention_id) REFERENCES users(id);
 
# --- !Downs
