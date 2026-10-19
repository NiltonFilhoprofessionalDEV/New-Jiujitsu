-- Convite do admin da plataforma para abrir uma academia. Uso único, expira em 7 dias.
create table academy_creation_invites (
    id                  uuid primary key default gen_random_uuid(),
    token               varchar(64)  not null unique,
    status              varchar(20)  not null default 'PENDING'
                        check (status in ('PENDING', 'ACCEPTED', 'REVOKED')),
    expires_at          timestamptz  not null,
    created_by          uuid references users (id) on delete set null,
    accepted_academy_id uuid references academies (id) on delete set null,
    created_at          timestamptz  not null default now(),
    updated_at          timestamptz  not null default now()
);
create index ix_academy_creation_invites_pending
    on academy_creation_invites (created_at desc)
    where status = 'PENDING';
