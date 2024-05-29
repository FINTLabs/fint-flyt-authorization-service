create table user_permission_source_application_ids
(
    user_permission_id     int8 not null,
    source_application_ids int4
);
create table user_permission
(
    id                bigserial    not null,
    object_identifier varchar(255) not null,
    primary key (id)
);
alter table user_permission_source_application_ids
    add constraint UK5w20jnmqeej62rhqsgyjahown unique (user_permission_id, source_application_ids);
alter table user_permission
    add constraint UK_3j9pvu47o30sbeqolxqgjsnj unique (object_identifier);
alter table user_permission_source_application_ids
    add constraint FKjpup6xop1xa25bcs8333uhsm8 foreign key (user_permission_id) references user_permission;
