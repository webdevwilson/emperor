# --- !Ups

ALTER TABLE users ADD organization VARCHAR(128);
ALTER TABLE users ADD location VARCHAR(128);
ALTER TABLE users ADD timezone VARCHAR(12) NOT NULL;
ALTER TABLE users ADD title VARCHAR(128);
ALTER TABLE users ADD url VARCHAR(128);

UPDATE users SET timezone='GMT-8:00';

# --- !Downs

ALTER TABLE users DROP COLUMN organization;
ALTER TABLE users DROP COLUMN location;
ALTER TABLE users DROP COLUMN timezone;
ALTER TABLE users DROP COLUMN title;
ALTER TABLE users DROP COLUMN url;