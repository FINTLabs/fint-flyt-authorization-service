-- Variant D: enkle audit-felt på user_entity (Spring Data JPA Auditing) og overgang fra egen
-- string-basert revisjonsaktør til fint-flyt-audit-starter sin JSONB Actor-modell.

alter table user_entity
    add column created_at       timestamptz null,
    add column created_by       jsonb not null default '{"type":"UNKNOWN"}'::jsonb,
    add column last_modified_at timestamptz null,
    add column last_modified_by jsonb not null default '{"type":"UNKNOWN"}'::jsonb;

alter table user_entity_aud
    drop constraint FK_user_entity_aud_rev;
alter table user_entity_source_application_ids_aud
    drop constraint FK_user_entity_source_application_ids_aud_rev;

alter table revinfo
    alter column rev drop identity if exists;

alter table revinfo
    alter column rev type bigint;
alter table user_entity_aud
    alter column rev type bigint;
alter table user_entity_source_application_ids_aud
    alter column rev type bigint;

create sequence revinfo_seq increment by 50 start with 1;
-- Start sekvensen trygt over eksisterende rev slik at pooled-optimizer-blokker ikke kolliderer.
select setval('revinfo_seq', (select coalesce(max(rev), 0) from revinfo) + 50, false);
alter table revinfo
    alter column rev set default nextval('revinfo_seq');

-- SecurityRevisionListener lagret primært email-claim. Konverter til JSONB Actor i et eget
-- mellomlager (Postgres tillater ikke subquery i ALTER ... USING, så oppslaget gjøres med UPDATE).
alter table revinfo
    add column actor_jsonb jsonb;

update revinfo
set actor_jsonb = jsonb_build_object('type', 'USER', 'oid', actor)
where actor ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Resolv kjente e-poster til ekte OID via user_entity, kun ved nøyaktig én match
-- (email er verken unik eller garantert satt).
update revinfo r
set actor_jsonb = jsonb_build_object('type', 'USER', 'oid', u.object_identifier)
from user_entity u
where r.actor_jsonb is null
  and u.email = r.actor
  and (select count(*) from user_entity u2 where u2.email = r.actor) = 1;

update revinfo
set actor_jsonb = '{"type":"SYSTEM"}'::jsonb
where actor_jsonb is null and lower(actor) in ('system', '');

update revinfo
set actor_jsonb = '{"type":"UNKNOWN"}'::jsonb
where actor_jsonb is null;

alter table revinfo
    drop column actor;
alter table revinfo
    rename column actor_jsonb to actor;
alter table revinfo
    alter column actor set not null,
    alter column actor set default '{"type":"UNKNOWN"}'::jsonb;

alter table user_entity_aud
    add constraint FK_user_entity_aud_rev foreign key (rev) references revinfo;
alter table user_entity_source_application_ids_aud
    add constraint FK_user_entity_source_application_ids_aud_rev foreign key (rev) references revinfo;
