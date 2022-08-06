create table post (
id SERIAL PRIMARY KEY,
name varchar(255),
text text,
link varchar(255) UNIQUE,
created date
);