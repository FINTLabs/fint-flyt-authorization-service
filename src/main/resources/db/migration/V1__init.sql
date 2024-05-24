create table user_permission
(
    id  bigserial not null,
    sub varchar(255),
    primary key (id)
);
create table user_permission_source_application_ids
(
    user_permission_id     int8 not null,
    source_application_ids int4
);
alter table user_permission_source_application_ids
    add constraint FKjpup6xop1xa25bcs8333uhsm8 foreign key (user_permission_id) references user_permission;
