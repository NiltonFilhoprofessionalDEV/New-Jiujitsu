-- Nome (e parentesco, se a pessoa quiser) de quem atende o telefone de emergência.
ALTER TABLE students ADD COLUMN emergency_contact varchar(120);
ALTER TABLE enrollment_requests ADD COLUMN emergency_contact varchar(120);
