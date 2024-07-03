create table user_entity_source_application_ids
(
    user_entity_id         int8 not null,
    source_application_ids int8
);
create table user_entity
(
    id                bigserial not null,
    email             varchar(255),
    name              varchar(255),
    object_identifier uuid      not null,
    primary key (id)
);
alter table user_entity_source_application_ids
    add constraint UKo3tx39i7fw3q0sr3f0rjp679f unique (user_entity_id, source_application_ids);
alter table user_entity
    add constraint UK_td2dvdf4t2le4cydfk7a1x17i unique (object_identifier);
alter table user_entity_source_application_ids
    add constraint FKisf0n8x0vsfohl0wkenll537s foreign key (user_entity_id) references user_entity;
