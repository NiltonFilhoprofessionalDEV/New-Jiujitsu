-- Convite de equipe (professor ou co-gestor): uso único, expira. O aceite cria o vínculo ativo.
create table staff_invites (
    id                     uuid primary key default gen_random_uuid(),
    academy_id             uuid         not null references academies (id) on delete cascade,
    token                  varchar(64)  not null unique,
    role                   varchar(20)  not null check (role in ('INSTRUCTOR', 'CO_MANAGER')),
    name                   varchar(120),
    email                  varchar(254),
    status                 varchar(20)  not null default 'PENDING' check (status in ('PENDING', 'ACCEPTED', 'REVOKED')),
    expires_at             timestamptz  not null,
    accepted_membership_id uuid references memberships (id) on delete set null,
    created_by             uuid references users (id) on delete set null,
    created_at             timestamptz  not null default now(),
    updated_at             timestamptz  not null default now()
);
create index ix_staff_invites_academy on staff_invites (academy_id, created_at desc);
