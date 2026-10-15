# Assinatura das academias — Plano de implementação

> Execução inline nesta sessão (superpowers:executing-plans). Passos com checkbox.

**Goal:** teste de 14 dias que vale de verdade, bloqueio da equipe após a tolerância, painel do admin e
pagamento recorrente pelo Asaas.

**Architecture:** regra de acesso pura (`AccessRules`) calculada das datas da academia; um
`HandlerInterceptor` aplica o bloqueio na API; admin por e-mail configurado; integração Asaas via
`RestClient` e webhook idempotente persistido em `payment_events`.

**Tech Stack:** Java 21, Spring Boot 4, Flyway, Postgres, Testcontainers; React 19, TanStack Query,
i18next.

**Spec:** `docs/superpowers/specs/2026-10-15-assinatura-academias-design.md`

## Global Constraints

- Teste 14 dias; tolerância 5 dias; R$ 100,00/mês — em `app.subscription.*`, sobrescrevíveis por env.
- Admin: `APP_PLATFORM_ADMIN_EMAILS` (lista por vírgula). Nunca no código.
- Segredos do Asaas só em env (`APP_ASAAS_API_KEY`, `APP_ASAAS_WEBHOOK_TOKEN`); nunca em log ou resposta.
- Alunos e responsáveis nunca bloqueados.
- Migração só altera `trial_ends_at` das academias existentes; nada mais em dados existentes.
- Arquivos ≤ ~300 linhas, funções ≤ 50; textos de UI em pt-BR via `locales/pt-BR.json`.
- Sem dependências novas.

---

### Task 1: Regra de acesso + configuração

**Files:**
- Create: `apps/api/src/main/java/br/com/academia/api/subscription/AccessRules.java`
- Create: `apps/api/src/main/java/br/com/academia/api/subscription/AccessStatus.java`
- Modify: `config/AppProperties.java` (records `Subscription`, `Platform`, `Asaas`), `application.yml`,
  `apps/api/.env.example`, `docker-compose.prod.yml`, `TestcontainersConfiguration.java`
- Test: `apps/api/src/test/java/br/com/academia/api/subscription/AccessRulesTest.java`

**Interfaces (Produces):**
```java
public enum AccessStatus { TRIAL, ACTIVE, GRACE, SUSPENDED }
public record Access(AccessStatus status, Instant accessUntil, Instant suspendsAt, long daysLeft) {}
public static Access AccessRules.evaluate(Instant trialEndsAt, Instant paidUntil, Instant now,
    Duration grace, ZoneId zone)
```

- [ ] Teste falhando: bordas (`now` = 1s antes do fim do teste → TRIAL, `daysLeft` 1; fim do teste → GRACE,
  `daysLeft` 5; fim + 5 dias → SUSPENDED, 0; `paidUntil` > `now` → ACTIVE mesmo com teste vencido;
  `paidUntil` < `trialEndsAt` → TRIAL).
- [ ] Implementar; `daysLeft` = dias de calendário no fuso entre `now` e o alvo, arredondado para cima.
- [ ] `./mvnw -q test -Dtest=AccessRulesTest` passa; commit.

### Task 2: Migração V11 + entidade

**Files:**
- Create: `db/migration/V11__academy_subscription.sql`
- Modify: `academy/Academy.java` (`paidUntil`, `asaasCustomerId`, `asaasSubscriptionId`, métodos
  `extendTrial(Instant from, Duration)`, `extendPaidUntil(Instant)`, `linkAsaas(...)`), `auth/AuthService.java`
  (dias de teste vindos da config + `Clock`)

```sql
alter table academies
    add column paid_until timestamptz,
    add column asaas_customer_id varchar(40),
    add column asaas_subscription_id varchar(40) unique;
-- Decisão do produto: academias já cadastradas (todas de teste) ganham 14 dias a partir da publicação
update academies set trial_ends_at = now() + interval '14 days';

create table academy_access_grants (
    id         uuid primary key default gen_random_uuid(),
    academy_id uuid        not null references academies (id) on delete cascade,
    kind       varchar(20) not null check (kind in ('TRIAL_EXTENSION', 'MANUAL_PAYMENT')),
    days       integer     not null check (days between 1 and 366),
    note       varchar(200),
    granted_by uuid        not null references users (id),
    created_at timestamptz not null default now()
);
create index ix_access_grants_academy on academy_access_grants (academy_id, created_at desc);

create table payment_events (
    event_id        varchar(120) primary key,
    event           varchar(60)  not null,
    payment_id      varchar(40),
    subscription_id varchar(40),
    payload         jsonb        not null,
    received_at     timestamptz  not null default now()
);
```

- [ ] Escrever migração e entidade; subir a suíte (`ApiApplicationTests`) para validar `ddl-auto: validate`.

### Task 3: `GET /subscription` + bloqueio da equipe

**Files:**
- Create: `subscription/SubscriptionService.java` (`Access accessOf(UUID academyId)`, `view(CurrentUser)`),
  `subscription/SubscriptionController.java`, `subscription/SubscriptionDtos.java`,
  `subscription/SuspendedAcademyGuard.java` (`HandlerInterceptor`), `config/WebConfig.java`
- Test: `subscription/SubscriptionIntegrationTest.java`

Guard: só age com `JwtAuthenticationToken`; papel de equipe; caminho fora da lista liberada
(`/me`, `/auth/`, `/subscription`, `/platform/`, `/public/`, `/webhooks/`); `accessOf(...).status() == SUSPENDED`
→ lança `ApiException(403, "ACADEMY_SUSPENDED")` (o handler existente formata).

- [ ] Testes: nova academia → `TRIAL`, `daysLeft` 14, preço 100.00; com `trial_ends_at` 10 dias atrás
  (JdbcTemplate) → `SUSPENDED`, gestor recebe 403 `ACADEMY_SUSPENDED` em `/students/page` mas 200 em `/me`
  e `/subscription`; professor também bloqueado; aluno (convite aceito) acessa `/portal/...` e `/videos`;
  com 2 dias atrás → `GRACE` e equipe liberada.
- [ ] Implementar; testes passam; commit.

### Task 4: Painel do admin (API)

**Files:**
- Create: `platform/PlatformAdmins.java` (`boolean isAdmin(UUID userId)`; compara e-mail do usuário com a
  config), `platform/PlatformController.java`, `platform/PlatformService.java`, `platform/PlatformDtos.java`,
  `platform/AccessGrant.java`, `platform/AccessGrantRepository.java`, `platform/PlatformAcademyQuery.java`
  (JdbcTemplate: academias + e-mail do dono + alunos ativos)
- Test: `platform/PlatformIntegrationTest.java`

- [ ] Testes: `GET /platform/access` → `admin:false` para gestor comum e `true` para o e-mail configurado
  (`admin@teste.dev` nos testes); não-admin → 403 `PLATFORM_ADMIN_ONLY` em `/platform/academies`;
  `TRIAL_EXTENSION` 30 dias em academia suspensa → volta a `TRIAL`; `MANUAL_PAYMENT` → `ACTIVE`; grant
  gravado; `days` fora de 1–366 → 400.
- [ ] Implementar; commit.

### Task 5: Cliente Asaas + checkout

**Files:**
- Create: `subscription/asaas/AsaasClient.java` (`createCustomer`, `createSubscription`,
  `openPayments(subscriptionId)`), `subscription/asaas/AsaasDtos.java`, `common/Cnpj.java`,
  `subscription/CheckoutService.java`; Modify `SubscriptionController` (`POST /subscription/checkout`)
- Test: `common/CnpjTest.java`; `subscription/CheckoutIntegrationTest.java` com `AsaasClient` mockado
  (`@MockitoBean`)

- [ ] Testes: CNPJ válido/inválido; checkout sem documento na primeira vez → 400 `TAX_ID_REQUIRED`;
  documento inválido → 400 `INVALID_TAX_ID`; primeira vez cria cliente e assinatura com
  `nextDueDate` = fim do teste e devolve `invoiceUrl`; segunda vez não recria; Asaas desligado → 503
  `PAYMENT_UNAVAILABLE`; professor → 403.
- [ ] Implementar; commit.

### Task 6: Webhook

**Files:**
- Create: `subscription/webhook/AsaasWebhookController.java`, `subscription/webhook/PaymentEvent.java`,
  `subscription/webhook/PaymentEventRepository.java`, `subscription/webhook/PaymentWebhookService.java`;
  Modify `SecurityConfig` (`POST /webhooks/asaas` público)
- Test: `subscription/webhook/AsaasWebhookIntegrationTest.java`

- [ ] Testes: sem token / token errado → 401; `PAYMENT_RECEIVED` com vencimento D → `paid_until` = D + 1 mês
  (fim do dia) e status `ACTIVE`; mesmo `id` reenviado → 200 e uma linha só; `PAYMENT_CONFIRMED` +
  `PAYMENT_RECEIVED` da mesma cobrança → mesma data; assinatura desconhecida → 200 e só gravado.
- [ ] Implementar; suíte inteira passa; commit.

### Task 7: Frontend — assinatura, faixa e bloqueio

**Files:**
- Create: `web/src/subscription/{types.ts,subscriptionApi.ts,SubscriptionBanner.tsx,SubscriptionPage.tsx,
  CheckoutForm.tsx,SuspendedScreen.tsx}`, `web/src/styles/subscription.css`
- Modify: `app/router.tsx` (`/assinatura`), `layout/AppShell.tsx` (faixa + tela de bloqueio),
  `layout/navItems.ts` (item "Assinatura" para gestor), `main.tsx` (erro `ACADEMY_SUSPENDED` invalida
  `subscription`), `locales/pt-BR.json`

- [ ] Implementar; `npm run lint` e `npm run build`; conferir no navegador (teste, tolerância, suspensa
  como gestor e professor; aluno sem aviso); commit.

### Task 8: Frontend — admin

**Files:**
- Create: `web/src/platform/{types.ts,platformApi.ts,AdminPage.tsx,GrantForm.tsx}`
- Modify: `app/router.tsx` (`/admin`), `layout/Sidebar.tsx`/`navItems.ts` (item "Admin" só para admin),
  `locales/pt-BR.json`

- [ ] Implementar; lint/build; conferir no navegador com o admin local; commit e push.

### Task 9: Configuração local e guia

- [ ] `.env.example` e `docker-compose.prod.yml` com as variáveis novas (sem valores secretos).
- [ ] Guia curto para o dono: criar conta sandbox, gerar chave, cadastrar webhook com token gerado
  localmente, preencher `.env` (ele mesmo), testar com cartão de teste.
