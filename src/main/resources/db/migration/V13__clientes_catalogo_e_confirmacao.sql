-- Evolução incremental: nenhum registro histórico é apagado ou confirmado artificialmente.
ALTER TABLE clientes ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE lojas ADD COLUMN limite_credito_padrao DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE lojas ADD COLUMN email_contato VARCHAR(100) NULL;
ALTER TABLE lojas ADD COLUMN email_confirmado BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE itens_catalogo (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 loja_id BIGINT NOT NULL,
 descricao VARCHAR(200) NOT NULL,
 preco_sugerido DECIMAL(10,2) NULL,
 ativo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT fk_catalogo_loja FOREIGN KEY(loja_id) REFERENCES lojas(id),
 INDEX ix_catalogo_loja_descricao(loja_id,descricao,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE confirmacoes_email (
 loja_id BIGINT NOT NULL PRIMARY KEY,
 token_hash CHAR(64) NOT NULL,
 email VARCHAR(100) NOT NULL,
 expira_em DATETIME(6) NOT NULL,
 enviado_em DATETIME(6) NOT NULL,
 CONSTRAINT fk_confirmacao_loja FOREIGN KEY(loja_id) REFERENCES lojas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
