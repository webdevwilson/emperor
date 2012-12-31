# --- !Ups

CREATE TABLE user_tokens (
  token VARCHAR(32) NOT NULL,
  user_id INT UNSIGNED NOT NULL,
  comment VARCHAR(255),
  date_created DATETIME NOT NULL,
  PRIMARY KEY(token),
  FOREIGN KEY (user_id) REFERENCES users(id)
)ENGINE InnoDB CHARACTER SET utf8 COLLATE utf8_bin;

# --- !Downss

DROP TABLE IF EXISTS user_tokens;