-- Assinatura das academias na plataforma: período pago e vínculo com o Asaas.
alter table academies
    add column paid_until            timestamptz,
    add column asaas_customer_id     varchar(40),
    add column asaas_subscription_id varchar(40) unique;

-- Decisão do produto: as academias já cadastradas (todas de teste) ganham 14 dias a partir da publicação.
update academies set trial_ends_at = now() + interval '14 days';

-- Liberações feitas pelo admin da plataforma (extensão de teste ou pagamento fora do app), para auditoria.
create table academy_access_grants (
    id         uuid primary key default gen_random_uuid(),
    academy_id uuid         not null references academies (id) on delete cascade,
    kind       varchar(20)  not null check (kind in ('TRIAL_EXTENSION', 'MANUAL_PAYMENT')),
    days       integer      not null check (days between 1 and 366),
    note       varchar(200),
    granted_by uuid         not null references users (id),
    created_at timestamptz  not null default now()
);
create index ix_access_grants_academy on academy_access_grants (academy_id, created_at desc);

-- Eventos do webhook do Asaas. A entrega é "pelo menos uma vez": o id do evento impede reprocessar.
create table payment_events (
    event_id        varchar(120) primary key,
    event           varchar(60)  not null,
    payment_id      varchar(40),
    subscription_id varchar(40),
    payload         jsonb        not null,
    received_at     timestamptz  not null default now()
);
create index ix_payment_events_subscription on payment_events (subscription_id);
