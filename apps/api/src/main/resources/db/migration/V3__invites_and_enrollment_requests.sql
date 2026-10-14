-- Link de matrícula da academia: um por academia, divulgado à vontade. Cadastros por ele aguardam aprovação.
create table enrollment_links (
    academy_id uuid primary key references academies (id) on delete cascade,
    token      varchar(64) not null unique,
    enabled    boolean     not null default true,
    expires_on date,
    opens      integer     not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- Convite individual gerado pelo gestor: uso único, expira; o aceite já cria o aluno ativo.
create table invites (
    id                  uuid primary key default gen_random_uuid(),
    academy_id          uuid         not null references academies (id) on delete cascade,
    token               varchar(64)  not null unique,
    kind                varchar(20)  not null check (kind in ('TRAINING', 'RESPONSIBLE')),
    name                varchar(120),
    email               varchar(254),
    phone               varchar(30),
    status              varchar(20)  not null default 'PENDING' check (status in ('PENDING', 'ACCEPTED', 'REVOKED')),
    expires_at          timestamptz  not null,
    opens               integer      not null default 0,
    accepted_student_id uuid references students (id) on delete set null,
    created_by          uuid references users (id) on delete set null,
    created_at          timestamptz  not null default now(),
    updated_at          timestamptz  not null default now()
);
create index ix_invites_academy on invites (academy_id, created_at desc);

-- Pedido de cadastro feito pelo link de matrícula, aguardando o gestor em Solicitações.
create table enrollment_requests (
    id            uuid primary key default gen_random_uuid(),
    academy_id    uuid         not null references academies (id) on delete cascade,
    user_id       uuid         not null references users (id) on delete cascade,
    membership_id uuid         not null references memberships (id) on delete cascade,
    name          varchar(120) not null,
    nickname      varchar(60),
    email         varchar(254) not null,
    cpf           varchar(11),
    phone         varchar(30),
    birth_date    date,
    belt          varchar(30),
    degrees       smallint     not null default 0 check (degrees between 0 and 10),
    status        varchar(20)  not null default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    student_id    uuid references students (id) on delete set null,
    decided_by    uuid references users (id) on delete set null,
    decided_at    timestamptz,
    created_at    timestamptz  not null default now()
);
create index ix_enrollment_requests_academy on enrollment_requests (academy_id, status, created_at);
