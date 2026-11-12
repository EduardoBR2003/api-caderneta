-- CPF único POR PAPEL em vez de único no sistema inteiro: a mesma pessoa pode ter um cadastro de
-- cliente, um da equipe (funcionário/proprietário) e um de fiador. Antes, um funcionário não podia
-- comprar a prazo e um cliente não podia ser fiador de outro cliente.
-- Um comando DDL por instrução (o TiDB não aceita várias mudanças no mesmo ALTER TABLE).
ALTER TABLE pessoas ADD COLUMN papel VARCHAR(20) NULL;
UPDATE pessoas SET papel = 'CLIENTE' WHERE id IN (SELECT id FROM clientes);
UPDATE pessoas SET papel = 'EQUIPE' WHERE id IN (SELECT id FROM funcionarios);
UPDATE pessoas SET papel = 'FIADOR' WHERE id IN (SELECT id FROM fiadores);
UPDATE pessoas SET papel = 'OUTRO' WHERE papel IS NULL;
ALTER TABLE pessoas MODIFY COLUMN papel VARCHAR(20) NOT NULL;
ALTER TABLE pessoas ADD UNIQUE KEY uk_pessoas_papel_cpf (papel, cpf_normalizado);
ALTER TABLE pessoas DROP INDEX uk_pessoas_cpf_normalizado;
ALTER TABLE pessoas DROP INDEX UK_cpf;
