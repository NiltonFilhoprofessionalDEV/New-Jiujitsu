-- Telefone de emergência (parente ou contato próximo). Obrigatório para quem treina a partir de agora;
-- quem já estava cadastrado fica sem até o gestor editar o aluno.
ALTER TABLE students ADD COLUMN emergency_phone varchar(30);
ALTER TABLE enrollment_requests ADD COLUMN emergency_phone varchar(30);
