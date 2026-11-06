-- Auditoria separada do histórico do cliente. Sem chaves estrangeiras para o domínio (os eventos
-- sobrevivem à exclusão do recurso) e sem exclusão em cascata. A aplicação só insere e consulta.
CREATE TABLE auditoria_eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ocorrido_em DATETIME(6) NOT NULL,            -- UTC
    usuario_id BIGINT NULL,                      -- ator (nulo em ações anônimas, como a ativação por convite)
    ator_login VARCHAR(50) NULL,
    perfil VARCHAR(20) NULL,                     -- perfil no momento da ação
    acao VARCHAR(60) NOT NULL,
    recurso_tipo VARCHAR(40) NOT NULL,
    recurso_id BIGINT NULL,
    resultado VARCHAR(20) NOT NULL,
    alteracoes TEXT NULL,                        -- JSON {campo: {antes, depois}}; nunca segredos
    PRIMARY KEY (id),
    KEY ix_auditoria_ocorrido (ocorrido_em, id),
    KEY ix_auditoria_acao (acao),
    KEY ix_auditoria_recurso (recurso_tipo, recurso_id)
) ENGINE=InnoDB;

-- Histórico paginado e ordenado por (data_envio DESC, id DESC), com recorte por destinatário.
CREATE INDEX ix_notificacoes_data ON notificacoes (data_envio, id_notificacao);
CREATE INDEX ix_notificacoes_dest_data ON notificacoes (destinatario_id, data_envio, id_notificacao);
