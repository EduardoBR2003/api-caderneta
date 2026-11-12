-- Várias lojas no mesmo sistema (multi-inquilino por coluna loja_id).
-- Os dados existentes passam para a "Loja Principal" (id 1). O isolamento é aplicado pela API
-- (@TenantId do Hibernate + trava ao carregar + filtros explícitos no SQL escrito à mão).
-- Um comando DDL por instrução (o TiDB não aceita várias mudanças no mesmo ALTER TABLE).

CREATE TABLE lojas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  ativa TINYINT(1) NOT NULL DEFAULT 1,
  criada_em DATETIME(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO lojas (id, nome, ativa, criada_em) VALUES (1, 'Loja Principal', 1, UTC_TIMESTAMP(6));


-- pessoas
ALTER TABLE pessoas ADD COLUMN loja_id BIGINT NULL;
UPDATE pessoas SET loja_id = 1;
ALTER TABLE pessoas MODIFY COLUMN loja_id BIGINT NOT NULL;
-- CPF único por loja e papel (antes: por papel no sistema inteiro).
ALTER TABLE pessoas ADD UNIQUE KEY uk_pessoas_loja_papel_cpf (loja_id, papel, cpf_normalizado);
ALTER TABLE pessoas DROP INDEX uk_pessoas_papel_cpf;
ALTER TABLE pessoas ADD CONSTRAINT fk_pessoas_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- usuarios
ALTER TABLE usuarios ADD COLUMN loja_id BIGINT NULL;
UPDATE usuarios SET loja_id = 1;
ALTER TABLE usuarios MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE usuarios ADD INDEX ix_usuarios_loja (loja_id);
ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- vendas
ALTER TABLE vendas ADD COLUMN loja_id BIGINT NULL;
UPDATE vendas SET loja_id = 1;
ALTER TABLE vendas MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE vendas ADD INDEX ix_vendas_loja (loja_id);
ALTER TABLE vendas ADD CONSTRAINT fk_vendas_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- dividas
ALTER TABLE dividas ADD COLUMN loja_id BIGINT NULL;
UPDATE dividas SET loja_id = 1;
ALTER TABLE dividas MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE dividas ADD INDEX ix_dividas_loja (loja_id);
ALTER TABLE dividas ADD CONSTRAINT fk_dividas_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- pagamentos
ALTER TABLE pagamentos ADD COLUMN loja_id BIGINT NULL;
UPDATE pagamentos SET loja_id = 1;
ALTER TABLE pagamentos MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE pagamentos ADD INDEX ix_pagamentos_loja (loja_id);
ALTER TABLE pagamentos ADD CONSTRAINT fk_pagamentos_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- notificacoes
ALTER TABLE notificacoes ADD COLUMN loja_id BIGINT NULL;
UPDATE notificacoes SET loja_id = 1;
ALTER TABLE notificacoes MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE notificacoes ADD INDEX ix_notificacoes_loja (loja_id);
ALTER TABLE notificacoes ADD CONSTRAINT fk_notificacoes_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- convites_cliente
ALTER TABLE convites_cliente ADD COLUMN loja_id BIGINT NULL;
UPDATE convites_cliente SET loja_id = 1;
ALTER TABLE convites_cliente MODIFY COLUMN loja_id BIGINT NOT NULL;
ALTER TABLE convites_cliente ADD INDEX ix_convites_cliente_loja (loja_id);
ALTER TABLE convites_cliente ADD CONSTRAINT fk_convites_cliente_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- auditoria_eventos (nulo permitido: eventos sem loja, ex.: falhas antes do login)
ALTER TABLE auditoria_eventos ADD COLUMN loja_id BIGINT NULL;
UPDATE auditoria_eventos SET loja_id = 1;
ALTER TABLE auditoria_eventos ADD INDEX ix_auditoria_eventos_loja (loja_id);
ALTER TABLE auditoria_eventos ADD CONSTRAINT fk_auditoria_eventos_loja FOREIGN KEY (loja_id) REFERENCES lojas (id);

-- Matrícula passa a ser única por loja (verificada pela API, que filtra por loja).
ALTER TABLE funcionarios DROP INDEX UK_matricula;

-- Sessões abertas não sabem a qual loja pertencem: todos entram de novo uma vez.
DELETE FROM SPRING_SESSION_ATTRIBUTES;
DELETE FROM SPRING_SESSION;
