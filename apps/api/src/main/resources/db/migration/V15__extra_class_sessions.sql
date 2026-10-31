-- Sessão avulsa: o professor abre a chamada de uma turma num dia fora da grade.
alter table class_sessions
    add column extra boolean not null default false;
