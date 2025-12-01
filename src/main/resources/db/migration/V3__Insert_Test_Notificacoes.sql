-- Recria tabela com coluna LIDA
DROP TABLE IF EXISTS `notificacoes`;

CREATE TABLE `notificacoes` (
                                `id_notificacao` bigint NOT NULL AUTO_INCREMENT,
                                `data_envio` datetime(6) NOT NULL,
                                `mensagem` text NOT NULL,
                                `tipo_notificacao` enum('AVISO_DIVIDA_VENCIDA','CADASTRO_CLIENTE','COMPRA_REALIZADA','CONFIRMACAO_COMPRA','CONFIRMACAO_PAGAMENTO','LEMBRETE_PAGAMENTO','LEMBRETE_VENCIMENTO','PAGAMENTO_RECEBIDO') NOT NULL,
                                `destinatario_id` bigint NOT NULL,
                                `lida` bit(1) NOT NULL DEFAULT 0,
                                PRIMARY KEY (`id_notificacao`),
                                KEY `fk_notificacoes_pessoas` (`destinatario_id`),
                                CONSTRAINT `fk_notificacoes_pessoas` FOREIGN KEY (`destinatario_id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB;

-- DADOS DE TESTE (Datas variadas para testar filtros)
INSERT INTO `notificacoes` (`destinatario_id`, `data_envio`, `mensagem`, `tipo_notificacao`, `lida`)
VALUES (3, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'Pagamento de R$ 50,00 recebido via PIX.', 'PAGAMENTO_RECEBIDO', 0);

INSERT INTO `notificacoes` (`destinatario_id`, `data_envio`, `mensagem`, `tipo_notificacao`, `lida`)
VALUES (3, DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Lembrete: Sua fatura vence hoje.', 'LEMBRETE_VENCIMENTO', 0);

INSERT INTO `notificacoes` (`destinatario_id`, `data_envio`, `mensagem`, `tipo_notificacao`, `lida`)
VALUES (3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Bem-vindo ao sistema Caderneta!', 'CADASTRO_CLIENTE', 1); -- LIDA

INSERT INTO `notificacoes` (`destinatario_id`, `data_envio`, `mensagem`, `tipo_notificacao`, `lida`)
VALUES (3, DATE_SUB(NOW(), INTERVAL 8 DAY), 'URGENTE: Dívida em atraso há 5 dias.', 'AVISO_DIVIDA_VENCIDA', 0);