-- Sessão no servidor (Spring Session JDBC). Esquema controlado pelo Flyway:
-- spring.session.jdbc.initialize-schema=never.
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID)
        REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

-- Contas de acesso. Uma conta por pessoa; perfis fixos.
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pessoa_id BIGINT NOT NULL,
    login VARCHAR(50) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil ENUM('GERENTE','FUNCIONARIO','CLIENTE') NOT NULL,
    ativo TINYINT(1) NOT NULL DEFAULT 1,
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_pessoa (pessoa_id),
    UNIQUE KEY uk_usuarios_login (login),
    CONSTRAINT fk_usuarios_pessoas FOREIGN KEY (pessoa_id) REFERENCES pessoas (id)
) ENGINE=InnoDB;

-- Migra as credenciais existentes sem duplicar pessoas. Só hash BCrypt válido ($2a/$2b/$2y, 60 caracteres)
-- é aproveitado e a conta fica ativa. Qualquer outro valor (por exemplo, senha em texto) NÃO é copiado:
-- a conta é criada INATIVA com hash inutilizável '!', e o gerente deve cadastrá-la novamente.
-- Proprietários viram GERENTE; demais funcionários, FUNCIONARIO.
INSERT INTO usuarios (pessoa_id, login, senha_hash, perfil, ativo, criado_em)
SELECT f.id,
       f.login,
       CASE WHEN LEFT(f.senha_hash, 4) IN ('$2a$', '$2b$', '$2y$') AND CHAR_LENGTH(f.senha_hash) = 60
            THEN f.senha_hash ELSE '!' END,
       CASE WHEN pr.id IS NOT NULL THEN 'GERENTE' ELSE 'FUNCIONARIO' END,
       CASE WHEN LEFT(f.senha_hash, 4) IN ('$2a$', '$2b$', '$2y$') AND CHAR_LENGTH(f.senha_hash) = 60
            THEN 1 ELSE 0 END,
       UTC_TIMESTAMP(6)
FROM funcionarios f
LEFT JOIN proprietarios pr ON pr.id = f.id;

-- A credencial agora vive somente em usuarios. A coluna legada deixa de guardar segredo.
UPDATE funcionarios SET senha_hash = '{migrado-para-usuarios}';

-- Convites de ativação de acesso do cliente. Guarda apenas o hash SHA-256 do código.
CREATE TABLE convites_cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    codigo_hash CHAR(64) NOT NULL,
    expira_em DATETIME(6) NOT NULL,
    usado_em DATETIME(6) NULL,
    criado_por_usuario_id BIGINT NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_convites_codigo (codigo_hash),
    KEY ix_convites_cliente (cliente_id),
    CONSTRAINT fk_convites_clientes FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_convites_usuarios FOREIGN KEY (criado_por_usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;
