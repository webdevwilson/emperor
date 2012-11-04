# --- !Ups

ALTER TABLE users ADD organization VARCHAR(128);
ALTER TABLE users ADD location VARCHAR(128);
ALTER TABLE users ADD title VARCHAR(128);
ALTER TABLE users ADD url VARCHAR(128);

# --- !Downs

ALTER TABLE users DROP COLUMN organization;
ALTER TABLE users DROP COLUMN location;
ALTER TABLE users DROP COLUMN title;
ALTER TABLE users DROP COLUMN url;