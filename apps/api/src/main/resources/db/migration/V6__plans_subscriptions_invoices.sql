-- Valores em centavos (inteiros) para não haver arredondamento de ponto flutuante.
create table plans (
    id              uuid primary key default gen_random_uuid(),
    academy_id      uuid         not null references academies (id) on delete cascade,
    name            varchar(80)  not null,
    description     varchar(500),
    price_cents     integer      not null check (price_cents between 0 and 10000000),
    interval_months smallint     not null check (interval_months between 1 and 12),
    due_day         smallint     not null check (due_day between 1 and 28),
    active          boolean      not null default true,
    is_default      boolean      not null default false,
    is_kids_default boolean      not null default false,
    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now(),
    check (active or not (is_default or is_kids_default))
);
create index ix_plans_academy on plans (academy_id);
create unique index ux_plans_default on plans (academy_id) where is_default;
create unique index ux_plans_kids_default on plans (academy_id) where is_kids_default;

-- Mensalidade do aluno: next_due_date é o vencimento da próxima fatura a emitir.
create table subscriptions (
    id              uuid primary key default gen_random_uuid(),
    academy_id      uuid         not null references academies (id) on delete cascade,
    student_id      uuid         not null references students (id) on delete cascade,
    plan_id         uuid         not null references plans (id),
    due_day         smallint     not null check (due_day between 1 and 28),
    discount_type   varchar(10) check (discount_type in ('PERCENT', 'FIXED')),
    discount_value  integer check (discount_value >= 0),
    discount_reason varchar(120),
    next_due_date   date         not null,
    started_on      date         not null,
    canceled_at     timestamptz,
    created_at      timestamptz  not null default now(),
    updated_at      timestamptz  not null default now(),
    check ((discount_type is null) = (discount_value is null)),
    check (discount_type is distinct from 'PERCENT' or discount_value <= 100)
);
create unique index ux_subscriptions_active_student on subscriptions (student_id) where canceled_at is null;
create index ix_subscriptions_due on subscriptions (next_due_date) where canceled_at is null;
create index ix_subscriptions_academy on subscriptions (academy_id);

-- "Vencida" não é gravada: é OPEN com due_date anterior a hoje.
-- student_id sem cascade: histórico financeiro não some ao remover o aluno (o service bloqueia antes).
create table invoices (
    id               uuid primary key default gen_random_uuid(),
    academy_id       uuid         not null references academies (id) on delete cascade,
    student_id       uuid         not null references students (id),
    subscription_id  uuid references subscriptions (id) on delete set null,
    description      varchar(120) not null,
    amount_cents     integer      not null check (amount_cents > 0),
    due_date         date         not null,
    status           varchar(10)  not null default 'OPEN' check (status in ('OPEN', 'PAID', 'CANCELED')),
    paid_on          date,
    payment_method   varchar(10) check (payment_method in ('CASH', 'PIX', 'CARD', 'TRANSFER', 'OTHER')),
    paid_recorded_by uuid references memberships (id) on delete set null,
    canceled_at      timestamptz,
    created_at       timestamptz  not null default now(),
    updated_at       timestamptz  not null default now(),
    check ((status = 'PAID') = (paid_on is not null and payment_method is not null)),
    check ((status = 'CANCELED') = (canceled_at is not null)),
    unique (subscription_id, due_date)
);
create index ix_invoices_academy_due on invoices (academy_id, due_date);
create index ix_invoices_student on invoices (student_id);
