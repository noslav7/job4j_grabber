create table post (
id serial primary key,
title varchar(255),
link varchar(255) unique,
description	varchar(255),
created date
);