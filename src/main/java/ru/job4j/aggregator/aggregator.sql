create schema agregator;

create table post (
                      id int PRIMARY KEY UNIQUE,
                      name varchar(255),
                      text text,
                      link varchar(255) UNIQUE,
                      created date
);