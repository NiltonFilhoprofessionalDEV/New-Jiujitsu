# Assinatura das academias: teste grátis, bloqueio e painel do admin

Data: 2026-10-15 · Status: aguardando revisão

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

- `academies.paid_until timestamptz null`.
- `academies.trial_ends_at = now() + interval '14 days'` para as academias que já existem (decisão acima).
  Nenhuma outra coluna ou tabela existente é alterada.
- Tabela `academy_access_grants` (auditoria do que o admin libera): `id`, `academy_id`, `kind`
  (`TRIAL_EXTENSION` | `MANUAL_PAYMENT`), `days`, `note varchar(200) null`, `granted_by uuid` (usuário),
  `created_at`. Liberações mexem em dinheiro: precisam de rastro.

### Bloqueio no servidor (a regra que vale)

`HandlerInterceptor` depois da autenticação: se o papel for de equipe (`OWNER`, `CO_MANAGER`,
`INSTRUCTOR`) e a academia estiver `SUSPENDED`, responde **403** com `code: ACADEMY_SUSPENDED`. Liberado
mesmo bloqueado: `/me`, `/auth/**`, `/subscription/**` e `/platform/**`. Aluno e responsável nunca são
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
- **Tela Assinatura** (`/assinatura`, gestor): situação, datas e "R$ 100,00/mês". Na etapa 1 o botão de
  pagar mostra "Pagamento online em breve" + o contato de suporte (ver Pendências).
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

## Etapa 2 (resumo; detalhar quando houver conta no Asaas)

Gestor informa CPF/CNPJ → servidor cria cliente e assinatura mensal no Asaas (`billingType`
indefinido: Pix, boleto ou cartão na página do Asaas) → abre o `invoiceUrl` → webhook
`PAYMENT_CONFIRMED`/`PAYMENT_RECEIVED` estende `paid_until`. Webhook validado pelo cabeçalho
`asaas-access-token`, eventos persistidos e idempotentes. Chave da API e token só em variáveis de
ambiente. O cartão nunca passa pelo nosso servidor.

## Fora do escopo

Planos por faixa de alunos, cupons, nota fiscal, cancelamento pelo app (etapa 2), e-mails automáticos.

## Pendências

- Contato de suporte exibido na tela Assinatura durante a etapa 1 (WhatsApp ou e-mail).
