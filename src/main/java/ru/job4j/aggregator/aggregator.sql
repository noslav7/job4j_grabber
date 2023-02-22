create table posts (
id SERIAL PRIMARY KEY,
name varchar(255),
text text,
link varchar(255) UNIQUE,
created date
);