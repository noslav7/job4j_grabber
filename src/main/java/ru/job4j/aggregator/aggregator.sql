create table posts (
id serial primary key,
title varchar(255),
link varchar(255) unique,
description	varchar (6500),
created date
);