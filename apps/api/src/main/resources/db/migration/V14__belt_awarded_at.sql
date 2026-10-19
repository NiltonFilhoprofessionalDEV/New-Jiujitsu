-- Dia em que o aluno recebeu a faixa atual, informado no cadastro.
-- Sem grau, essa data é a última graduação. Com grau, o último grau fica em degree_awarded_at.
alter table students add column belt_awarded_at date;

-- Quem já preencheu "data do grau" sem ter grau estava informando o dia da faixa.
update students
set belt_awarded_at = degree_awarded_at,
    degree_awarded_at = null
where degrees = 0
  and belt is not null
  and belt <> 'branca'
  and degree_awarded_at is not null
  and belt_awarded_at is null;
