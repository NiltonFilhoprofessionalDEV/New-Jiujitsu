create table households (
    id                     uuid primary key default gen_random_uuid(),
    academy_id             uuid         not null references academies (id) on delete cascade,
    name                   varchar(120) not null,
    search_name            varchar(120) not null,
    responsible_student_id uuid,
    created_at             timestamptz  not null default now(),
    updated_at             timestamptz  not null default now()
);
create index ix_households_academy on households (academy_id);

create table students (
    id                uuid primary key default gen_random_uuid(),
    academy_id        uuid         not null references academies (id) on delete cascade,
    user_id           uuid references users (id) on delete set null,
    household_id      uuid references households (id) on delete set null,
    kind              varchar(12)  not null default 'TRAINING' check (kind in ('TRAINING', 'RESPONSIBLE')),
    status            varchar(10)  not null default 'ACTIVE' check (status in ('ACTIVE', 'INACTIVE')),
    name              varchar(120) not null,
    nickname          varchar(60),
    search_name       varchar(200) not null,
    email             varchar(254),
    cpf               varchar(11),
    phone             varchar(30),
    birth_date        date,
    gender            varchar(10) check (gender in ('MALE', 'FEMALE')),
    belt              varchar(30) check (belt in (
        'branca', 'cinza-e-branca', 'cinza', 'cinza-e-preta', 'amarela-e-branca', 'amarela', 'amarela-e-preta',
        'laranja-e-branca', 'laranja', 'laranja-e-preta', 'verde-e-branca', 'verde', 'verde-e-preta',
        'azul', 'roxa', 'marrom', 'preta', 'vermelha-e-preta', 'vermelha-e-branca', 'vermelha')),
    degrees           smallint     not null default 0 check (degrees between 0 and 10),
    degree_awarded_at date,
    belt_ruler        varchar(10)  not null default 'AUTO' check (belt_ruler in ('AUTO', 'KIDS', 'ADULT')),
    enrollment_date   date         not null default current_date,
    gi_size           varchar(20),
    belt_size         varchar(20),
    created_at        timestamptz  not null default now(),
    updated_at        timestamptz  not null default now(),
    check (kind = 'TRAINING' or belt is null)
);
create index ix_students_academy_status on students (academy_id, status);
create index ix_students_household on students (household_id);
create unique index ux_students_academy_email on students (academy_id, lower(email)) where email is not null;
create unique index ux_students_academy_cpf on students (academy_id, cpf) where cpf is not null;

alter table households
    add constraint fk_households_responsible
        foreign key (responsible_student_id) references students (id) on delete set null;
