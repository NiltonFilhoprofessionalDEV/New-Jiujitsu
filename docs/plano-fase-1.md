# Plano da fase 1: núcleo do app de gestão de academias

Base: `docs/analise-rollflow.md`. Escopo: login multi-academia, alunos e famílias,
aulas e calendário, presença (manual e QR), graduações, planos de mensalidade e
faturas (controle manual, sem gateway), área do aluno e configurações.

Fora da fase 1: superadmin, Asaas/carteira/saque, NFS-e, WhatsApp, reconhecimento
facial, loja, competições, CRM, gamificação, pesquisa NPS, portal público.

## Arquitetura

```
apps/
  api/   Java 21 + Spring Boot 4.1 + Spring Data JPA + Flyway + PostgreSQL
  web/   React 19 + Vite 8 + React Router 8 + TanStack Query 5 + Axios + i18next
docker-compose.yml   PostgreSQL 17 para desenvolvimento
```

- API REST em `/api/v2`, paginação no formato Spring Data `Page`, valores em centavos.
- Multi-tenant por academia: toda entidade de negócio tem `academy_id`; a
  academia ativa vem do token, nunca do corpo da requisição.
- Autenticação: access token JWT curto (15 min) em memória no frontend e refresh
  token opaco em cookie `HttpOnly; Secure; SameSite=Strict`, com rotação e
  hash no banco. Senhas com BCrypt.
- Autorização por papel na academia (`OWNER`, `CO_MANAGER`, `INSTRUCTOR`,
  `STUDENT`, `RESPONSIBLE`) mais permissões por área para co-gestor.
- Em desenvolvimento o Vite faz proxy de `/api` para `localhost:8080`, então o
  cookie fica no mesmo domínio e não há CORS. Em produção, CORS restrito ao
  domínio do frontend via variável de ambiente.
- Arquivos (logo, banner, fotos) no Supabase Storage na fase de deploy; local
  em disco durante o desenvolvimento.
- Tema: CSS puro com variáveis (como o RollFlow). Modo claro padrão (fundo
  branco, texto preto, vermelho de destaque) e modo escuro opcional.

## Modelo de dados (fase 1)

| Tabela | Campos principais |
| --- | --- |
| `users` | email único, password_hash, name, phone, language |
| `refresh_tokens` | user_id, token_hash, expires_at, revoked_at |
| `academies` | name, slug, cnpj, phone, address, logo_path, banner_path, trial_ends_at |
| `memberships` | user_id, academy_id, role, permissions (jsonb), active |
| `households` | academy_id, name, responsible_student_id |
| `students` | academy_id, user_id?, household_id?, name, email, cpf, birth_date, phone, belt, degrees, status, kind, enrollment_date |
| `invites` | academy_id, email, role, token_hash, expires_at, status |
| `enrollment_requests` | academy_id, name, email, phone, status |
| `training_classes` | academy_id, name, instructor_membership_id, weekdays (int[]), start_time, end_time, capacity, audience |
| `class_sessions` | class_id, date, status, qr_nonce |
| `check_ins` | session_id, student_id, method, created_at |
| `belt_rules` | academy_id, belt, min_months, min_classes |
| `graduations` | student_id, belt, degrees, graduated_at, instructor_membership_id, notes |
| `billing_plans` | academy_id, name, price_cents, interval, is_default, active |
| `subscriptions` | student_id, plan_id, billing_day, discount_cents, status, started_at |
| `invoices` | academy_id, student_id, household_id?, amount_cents, due_date, status, paid_at, payment_method, reference |
| `expenses` | academy_id, description, category, amount_cents, due_date, paid_at |
| `messages` | academy_id, student_id, sender_membership_id, body, read_at |
| `calendar_notes` | academy_id, date, title, body |

## Entregas

1. **Fundação**: monorepo, Postgres no Docker, esqueleto da API (segurança, erros,
   Flyway), shell do frontend (layout com sidebar e topbar, tema claro/escuro,
   i18n), cadastro de academia, login, troca de academia.
2. **Alunos**: lista paginada com filtros, ficha, famílias, convites, solicitações.
3. **Aulas e presença**: grade semanal, sessões, check-in manual e por QR, calendário.
4. **Graduação**: faixas e graus, regras de elegibilidade, histórico, certificado.
5. **Financeiro**: planos de mensalidade, assinaturas, geração de faturas,
   baixa manual, despesas, movimentações.
6. **Área do aluno e comunicação**: `/me/*`, mensagens, configurações.
7. **Relatórios, testes E2E e preparação de deploy** (Railway + Supabase).

Cada entrega termina com build, testes e verificação no navegador.
