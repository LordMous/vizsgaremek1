create table user (
    id              int             not null        primary key         auto_increment,
    email           varchar(50)     not null UNIQUE ,
    age             int             not null  ,
    username        varchar(30)     not null UNIQUE,
    password        varchar(255)     not null,
    phonenumber     varchar(13)     not null,
    picture         varchar(255)
);