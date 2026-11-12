-- Limite de tentativas de login/ativação, compartilhado entre instâncias da API.
-- A chave guarda só o hash SHA-256 do login ou do IP (sem dado pessoal em claro).
CREATE TABLE tentativas_acesso (
    chave CHAR(70) NOT NULL,                 -- "l:" ou "i:" + SHA-256 hex
    falhas INT NOT NULL,
    janela_inicio DATETIME(6) NOT NULL,      -- UTC
    bloqueado_ate DATETIME(6) NULL,          -- UTC
    PRIMARY KEY (chave),
    KEY ix_tentativas_janela (janela_inicio)
) ENGINE=InnoDB;
