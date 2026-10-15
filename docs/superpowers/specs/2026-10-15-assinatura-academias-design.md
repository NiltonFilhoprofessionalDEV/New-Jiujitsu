# Assinatura das academias: teste grátis, bloqueio e painel do admin

Data: 2026-10-15 · Status: aprovado (etapas 1 e 2)

## Contexto

Hoje qualquer pessoa cadastra uma academia e usa o BJJ Pulse de graça para sempre. O cadastro grava
`academies.trial_ends_at` (cadastro + 14 dias) e a tela promete "14 dias grátis", mas nada lê essa data.

## Decisões do dono do produto

| Tema | Decisão |
|---|---|
| Teste grátis | 14 dias, sem cartão |
| Preço | Valor único: R$ 100,00/mês (configurável) |
| Vencido sem pagar | 5 dias de tolerância com avisos; depois a **equipe** fica bloqueada, os **alunos** seguem usando |
| Provedor de pagamento | Asaas (etapa 2) |
| Academias existentes | Ganham 14 dias novos a partir da publicação |
| Admin da plataforma | `cabralsussa@gmail.com` (via variável de ambiente, nunca no código) |
| Ordem | Etapa 1 agora; etapa 2 quando houver conta no Asaas (sandbox) |

## Etapa 1 (este documento detalha): o teste passa a valer

### Situação da academia

Calculada, nunca gravada, a partir de duas datas: `trial_ends_at` e a nova `paid_until` (nula até o
primeiro pagamento ou liberação manual). Função pura `AccessRules.status(trialEndsAt, paidUntil, now)`:

```
accessUntil = max(trialEndsAt, paidUntil ?? trialEndsAt)
now <  accessUntil, paidUntil cobre now   → ACTIVE
now <  accessUntil, só o teste cobre      → TRIAL
now <  accessUntil + 5 dias               → GRACE      (tolerância)
caso contrário                            → SUSPENDED
```

Também devolve `accessUntil`, `suspendsAt` (`accessUntil` + tolerância) e `daysLeft` para os avisos:
dias inteiros, no fuso `app.time-zone`, até `accessUntil` em `TRIAL`/`ACTIVE` e até `suspendsAt` em `GRACE`. Tolerância, dias de teste e preço vêm de `app.subscription.*` (valores padrão: 5, 14, 100.00).

### Banco (migração V11)

- `academies.paid_until timestamptz null`, `asaas_customer_id varchar(40) null`,
  `asaas_subscription_id varchar(40) null unique` (os dois últimos usados na etapa 2).
- Tabela `payment_events` (etapa 2, abaixo).
- `academies.trial_ends_at = now() + interval '14 days'` para as academias que já existem (decisão acima).
  Nenhuma outra coluna ou tabela existente é alterada.
- Tabela `academy_access_grants` (auditoria do que o admin libera): `id`, `academy_id`, `kind`
  (`TRIAL_EXTENSION` | `MANUAL_PAYMENT`), `days`, `note varchar(200) null`, `granted_by uuid` (usuário),
  `created_at`. Liberações mexem em dinheiro: precisam de rastro.

### Bloqueio no servidor (a regra que vale)

`HandlerInterceptor` depois da autenticação: se o papel for de equipe (`OWNER`, `CO_MANAGER`,
`INSTRUCTOR`) e a academia estiver `SUSPENDED`, responde **403** com `code: ACADEMY_SUSPENDED`. Liberado
mesmo bloqueado: `/me`, `/auth/**`, `/subscription/**`, `/platform/**`, `/public/**` e `/webhooks/**`. Aluno e responsável nunca são
bloqueados. Custo: uma leitura por chave primária por requisição da equipe (aceitável; sem cache por ora).

### API nova

- `GET /subscription` (equipe): `status`, `trialEndsAt`, `paidUntil`, `accessUntil`, `suspendsAt`,
  `daysLeft`, `monthlyPrice`. O professor recebe o mesmo, para saber se a academia está suspensa.
- `GET /platform/access` (qualquer logado): `{ admin: boolean }`. Admin = e-mail do usuário do token está
  em `app.platform.admin-emails` (comparação sem diferenciar maiúsculas).
- `GET /platform/academies` (admin): nome, cidade, data de cadastro, situação, `accessUntil`, nº de alunos
  ativos, e-mail do dono.
- `POST /platform/academies/{id}/grants` (admin): `{ kind, days (1–366), note? }`.
  `TRIAL_EXTENSION` estende `trial_ends_at` a partir de `max(trial_ends_at, agora)`; `MANUAL_PAYMENT`
  estende `paid_until` a partir de `max(paid_until, agora)`. Grava em `academy_access_grants`.
  Não-admin recebe 403 (`PLATFORM_ADMIN_ONLY`).

### Telas

- **Faixa de aviso** (só gestor, no topo do painel): `TRIAL` com `daysLeft ≤ 7` ("Seu teste termina em
  N dias · Assinar") e `GRACE` sempre ("Assinatura vencida: o acesso da equipe será suspenso em N dias").
- **Tela Assinatura** (`/assinatura`, gestor): situação, datas e "R$ 100,00/mês"; pagamento na etapa 2.
- **Suspensa**: o painel da equipe troca o conteúdo por uma tela de bloqueio. Gestor vê o botão para a
  tela Assinatura; professor vê "O acesso da academia está suspenso. Fale com o gestor." O erro
  `ACADEMY_SUSPENDED` vindo de qualquer chamada também leva a essa tela.
- **Admin** (`/admin`, só quem tem `admin: true`): lista de academias com situação e as ações "Estender
  teste" e "Registrar pagamento manual" (dias + observação). Item "Admin" no menu só para o admin.

### Testes

- Unitário: `AccessRules` nas bordas (último segundo do teste, primeiro dia de tolerância, fim da
  tolerância, `paid_until` maior/menor que o teste), com relógio fixo.
- Integração: equipe bloqueada recebe `ACADEMY_SUSPENDED` e ainda acessa `/me` e `/subscription`; aluno
  da academia bloqueada continua acessando; não-admin recebe 403 em `/platform/**`; liberação estende as
  datas certas e grava a auditoria; academia nova nasce com 14 dias.

## Etapa 2: pagamento pelo Asaas (o dono já tem conta)

### Configuração (só variáveis de ambiente; nada no código nem no repositório)

| Variável | Uso |
|---|---|
| `APP_ASAAS_BASE_URL` | `https://api-sandbox.asaas.com/v3` (padrão) ou `https://api.asaas.com/v3` |
| `APP_ASAAS_API_KEY` | Chave do mesmo ambiente da URL. Vazia = pagamento online desligado |
| `APP_ASAAS_WEBHOOK_TOKEN` | 32–255 caracteres, igual ao cadastrado no webhook do painel do Asaas |

Chamadas ao Asaas com `access_token`, `User-Agent: BJJPulse/1.0` e `Content-Type: application/json`
(`RestClient` do Spring; nenhuma dependência nova). Tempo limite de 60 s.

### Assinar (gestor)

1. `POST /subscription/checkout` com `{ "cpfCnpj": "..." }` (só na primeira vez; CPF ou CNPJ válido,
   validado aqui antes de ir ao Asaas).
2. Sem cliente ainda: `POST /customers` (`name` = academia, `email` = dono, `cpfCnpj`,
   `externalReference` = id da academia) e guarda `academies.asaas_customer_id`.
3. Sem assinatura ainda: `POST /subscriptions` com `billingType: UNDEFINED`, `cycle: MONTHLY`,
   `value` = preço, `nextDueDate` = maior entre hoje e o fim do teste (o gestor não perde dias grátis),
   `externalReference` = id da academia; guarda `academies.asaas_subscription_id`.
4. Responde `{ invoiceUrl }` da cobrança em aberto mais antiga da assinatura
   (`GET /subscriptions/{id}/payments`, status `PENDING` ou `OVERDUE`). O app abre essa página, onde o
   gestor paga com Pix, boleto ou cartão. Já assinada e sem cobrança em aberto: `409 NOTHING_TO_PAY`.
5. Falha do Asaas vira `502 PAYMENT_PROVIDER_ERROR` (sem vazar a resposta do Asaas para o cliente).
   Pagamento desligado vira `503 PAYMENT_UNAVAILABLE`.

### Webhook

`POST /webhooks/asaas` (público, fora do bloqueio):

- Cabeçalho `asaas-access-token` comparado em tempo constante com o token configurado; diferente ou
  token não configurado: 401.
- Tabela `payment_events` (`event_id` único, `event`, `payment_id`, `subscription_id`, `payload jsonb`,
  `received_at`). Evento repetido: 200 sem reprocessar. O processamento acontece na mesma transação da
  gravação: se falhar, nada é gravado e o Asaas reenvia.
- `PAYMENT_CONFIRMED` ou `PAYMENT_RECEIVED` de uma assinatura conhecida: `paid_until` =
  maior entre o atual e (vencimento da cobrança + 1 mês, fim do dia no fuso da academia). Os dois eventos
  da mesma cobrança dão o mesmo resultado.
- Demais eventos: só gravados (estorno e cancelamento ficam para depois).

### Tela Assinatura (etapa 2)

Mostra situação e "R$ 100,00/mês". Botão "Assinar" pede CPF/CNPJ na primeira vez; depois vira
"Pagar fatura em aberto". Pagamento desligado: aviso "Pagamento online indisponível no momento".

## Fora do escopo

Planos por faixa de alunos, cupons, nota fiscal, cancelamento pelo app, estorno automático, e-mails
automáticos.
