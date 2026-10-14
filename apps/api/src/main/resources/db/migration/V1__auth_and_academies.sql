create table users (
    id            uuid primary key default gen_random_uuid(),
    email         varchar(254) not null,
    password_hash varchar(100) not null,
    name          varchar(120) not null,
    phone         varchar(30),
    language      varchar(10)  not null default 'pt-BR',
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now()
);
create unique index ux_users_email on users (lower(email));

create table academies (
    id            uuid primary key default gen_random_uuid(),
    name          varchar(120) not null,
    slug          varchar(80)  not null unique,
    cnpj          varchar(18),
    email         varchar(254),
    phone         varchar(30),
    address       varchar(255),
    city          varchar(120),
    state         varchar(2),
    logo_path     varchar(255),
    banner_path   varchar(255),
    trial_ends_at timestamptz  not null,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now()
);

create table memberships (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid        not null references users (id) on delete cascade,
    academy_id   uuid        not null references academies (id) on delete cascade,
    role         varchar(20) not null check (role in ('OWNER', 'CO_MANAGER', 'INSTRUCTOR', 'STUDENT', 'RESPONSIBLE')),
    permissions  text[]      not null default '{}',
    active       boolean     not null default true,
    last_used_at timestamptz,
    created_at   timestamptz not null default now(),
    unique (user_id, academy_id, role)
);
create index ix_memberships_academy on memberships (academy_id);

create table refresh_tokens (
    id            uuid primary key default gen_random_uuid(),
    user_id       uuid        not null references users (id) on delete cascade,
    membership_id uuid        not null references memberships (id) on delete cascade,
    token_hash    varchar(64) not null unique,
    expires_at    timestamptz not null,
    revoked_at    timestamptz,
    created_at    timestamptz not null default now()
);
create index ix_refresh_tokens_user on refresh_tokens (user_id);
