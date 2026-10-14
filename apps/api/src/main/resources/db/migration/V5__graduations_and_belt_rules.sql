-- Cada linha guarda também o estado anterior do aluno: remover a graduação mais recente o restaura.
create table graduations (
    id                  uuid primary key default gen_random_uuid(),
    academy_id          uuid        not null references academies (id) on delete cascade,
    student_id          uuid        not null references students (id) on delete cascade,
    kind                varchar(10) not null check (kind in ('BELT', 'DEGREE')),
    belt                varchar(30) not null,
    degrees             smallint    not null check (degrees between 0 and 10),
    previous_belt       varchar(30),
    previous_degrees    smallint    not null,
    previous_awarded_at date,
    graduated_on        date        not null,
    prior_trainings     integer check (prior_trainings is null or prior_trainings between 0 and 10000),
    exception           boolean     not null default false,
    note                varchar(500),
    recorded_by         uuid references memberships (id) on delete set null,
    created_at          timestamptz not null default now()
);
create index ix_graduations_student on graduations (student_id, graduated_on desc, created_at desc);
create index ix_graduations_academy on graduations (academy_id);

-- Sem linha para a faixa, valem os padrões do código (BeltRuleDefaults).
create table belt_rules (
    id                   uuid primary key default gen_random_uuid(),
    academy_id           uuid        not null references academies (id) on delete cascade,
    belt                 varchar(30) not null,
    min_months           integer check (min_months is null or min_months between 0 and 240),
    min_trainings        integer check (min_trainings is null or min_trainings between 0 and 5000),
    trainings_per_degree integer check (trainings_per_degree is null or trainings_per_degree between 1 and 1000),
    updated_at           timestamptz not null default now(),
    unique (academy_id, belt)
);
