create table training_classes (
    id                       uuid primary key default gen_random_uuid(),
    academy_id               uuid         not null references academies (id) on delete cascade,
    title                    varchar(80)  not null,
    description              varchar(500),
    weekdays                 integer[]    not null
        check (cardinality(weekdays) between 1 and 7 and weekdays <@ array [1, 2, 3, 4, 5, 6, 7]),
    start_time               time         not null,
    end_time                 time,
    location                 varchar(80),
    instructor_membership_id uuid references memberships (id) on delete set null,
    capacity                 integer check (capacity is null or capacity between 1 and 1000),
    archived_at              timestamptz,
    created_at               timestamptz  not null default now(),
    updated_at               timestamptz  not null default now(),
    check (end_time is null or end_time > start_time)
);
create index ix_training_classes_academy on training_classes (academy_id) where archived_at is null;

create table class_enrollments (
    id         uuid primary key default gen_random_uuid(),
    class_id   uuid        not null references training_classes (id) on delete cascade,
    student_id uuid        not null references students (id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (class_id, student_id)
);
create index ix_class_enrollments_student on class_enrollments (student_id);

create table class_sessions (
    id           uuid primary key default gen_random_uuid(),
    academy_id   uuid        not null references academies (id) on delete cascade,
    class_id     uuid        not null references training_classes (id) on delete cascade,
    session_date date        not null,
    notes        varchar(2000),
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    unique (class_id, session_date)
);
create index ix_class_sessions_academy_date on class_sessions (academy_id, session_date);

create table check_ins (
    id         uuid primary key default gen_random_uuid(),
    session_id uuid        not null references class_sessions (id) on delete cascade,
    student_id uuid        not null references students (id) on delete cascade,
    marked_by  uuid references memberships (id) on delete set null,
    created_at timestamptz not null default now(),
    unique (session_id, student_id)
);
create index ix_check_ins_student on check_ins (student_id);
