ALTER TABLE vendas ADD COLUMN versao BIGINT NOT NULL DEFAULT 0, ADD COLUMN atualizada_em DATETIME(6) NULL;
ALTER TABLE pagamentos ADD COLUMN operador_id BIGINT NULL;
CREATE TABLE operacoes_idempotentes (
 usuario_id BIGINT NOT NULL, chave VARCHAR(80) NOT NULL, conteudo_hash CHAR(64) NOT NULL,
 resposta LONGTEXT NULL, criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 PRIMARY KEY(usuario_id,chave)
) ENGINE=InnoDB;
ALTER TABLE notificacoes ADD COLUMN chave_evento VARCHAR(120) NULL, ADD UNIQUE KEY uk_evento(chave_evento);
-- Coluna gerada: preserva grafia antiga e impede novas duplicações com outra máscara.
-- Se houver colisão histórica, a migration falha; reconciliação não é silenciosa.
ALTER TABLE pessoas ADD COLUMN cpf_normalizado VARCHAR(14) GENERATED ALWAYS AS
 (REPLACE(REPLACE(REPLACE(cpf,'.',''),'-',''),' ','')) STORED,
 ADD UNIQUE KEY uk_pessoas_cpf_normalizado(cpf_normalizado);
CREATE INDEX ix_vendas_funcionario_data ON vendas(funcionario_id,data_hora,id);
CREATE INDEX ix_dividas_cliente_status ON dividas(cliente_id,status_divida);
