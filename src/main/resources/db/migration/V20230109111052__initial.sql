CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table users
(
    id            uuid primary key DEFAULT uuid_generate_v4(),
    name          varchar(255),
    email         varchar(255),
    age           numeric(3, 0),
    phone         varchar(30),
    created_date  timestamp with time zone NOT NULL,
    modified_date timestamp with time zone NOT NULL
);