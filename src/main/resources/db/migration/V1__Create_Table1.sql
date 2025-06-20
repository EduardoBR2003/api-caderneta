-- Database: api_caderneta_database

-- Criação do banco de dados (opcional, se não existir)
CREATE DATABASE IF NOT EXISTS `api_caderneta_database` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `api_caderneta_database`;

-- Tabela: pessoas
CREATE TABLE IF NOT EXISTS `pessoas` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `cpf` varchar(14) NOT NULL,
    `email` varchar(100) DEFAULT NULL,
    `endereco` varchar(200) DEFAULT NULL,
    `nome` varchar(150) NOT NULL,
    `telefone` varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_cpf` (`cpf`)
    ) ENGINE=InnoDB;

-- Tabela: fiadores
CREATE TABLE IF NOT EXISTS `fiadores` (
                                          `id` bigint NOT NULL,
                                          PRIMARY KEY (`id`),
    CONSTRAINT `fk_fiadores_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: funcionarios
CREATE TABLE IF NOT EXISTS `funcionarios` (
                                              `id` bigint NOT NULL,
                                              `cargo` varchar(50) DEFAULT NULL,
    `login` varchar(50) NOT NULL,
    `matricula` varchar(50) DEFAULT NULL,
    `senha_hash` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_login` (`login`),
    UNIQUE KEY `UK_matricula` (`matricula`),
    CONSTRAINT `fk_funcionarios_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: proprietarios
CREATE TABLE IF NOT EXISTS `proprietarios` (
                                               `id` bigint NOT NULL,
                                               PRIMARY KEY (`id`),
    CONSTRAINT `fk_proprietarios_funcionarios` FOREIGN KEY (`id`) REFERENCES `funcionarios` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: clientes
CREATE TABLE IF NOT EXISTS `clientes` (
                                          `id` bigint NOT NULL,
                                          `limite_credito` decimal(10,2) DEFAULT NULL,
    `prazo_pagamento_padrao_dias` int DEFAULT NULL,
    `fiador_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_clientes_fiadores` (`fiador_id`),
    CONSTRAINT `fk_clientes_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`),
    CONSTRAINT `fk_clientes_fiadores` FOREIGN KEY (`fiador_id`) REFERENCES `fiadores` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: vendas
CREATE TABLE IF NOT EXISTS `vendas` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `data_hora` datetime(6) NOT NULL,
    `valor_total` decimal(12,2) NOT NULL,
    `cliente_id` bigint NOT NULL,
    `funcionario_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_vendas_clientes` (`cliente_id`),
    KEY `fk_vendas_funcionarios` (`funcionario_id`),
    CONSTRAINT `fk_vendas_clientes` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
    CONSTRAINT `fk_vendas_funcionarios` FOREIGN KEY (`funcionario_id`) REFERENCES `funcionarios` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: itens_venda
CREATE TABLE IF NOT EXISTS `itens_venda` (
                                             `id` bigint NOT NULL AUTO_INCREMENT,
                                             `descricao_produto` varchar(200) NOT NULL,
    `preco_unitario` decimal(10,2) NOT NULL,
    `quantidade` int NOT NULL,
    `subtotal` decimal(12,2) NOT NULL,
    `venda_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_itensvenda_vendas` (`venda_id`),
    CONSTRAINT `fk_itensvenda_vendas` FOREIGN KEY (`venda_id`) REFERENCES `vendas` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: dividas
CREATE TABLE IF NOT EXISTS `dividas` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `data_emissao` date NOT NULL,
                                         `data_vencimento` date NOT NULL,
                                         `status_divida` enum('ABERTA','CANCELADA','PAGA_PARCIALMENTE','PAGA_TOTALMENTE','VENCIDA') NOT NULL,
    `valor_original` decimal(12,2) NOT NULL,
    `valor_pendente` decimal(12,2) NOT NULL,
    `cliente_id` bigint NOT NULL,
    `venda_origem_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_venda_origem_id` (`venda_origem_id`),
    KEY `fk_dividas_clientes` (`cliente_id`),
    CONSTRAINT `fk_dividas_vendas` FOREIGN KEY (`venda_origem_id`) REFERENCES `vendas` (`id`),
    CONSTRAINT `fk_dividas_clientes` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: pagamentos
CREATE TABLE IF NOT EXISTS `pagamentos` (
                                            `id` bigint NOT NULL AUTO_INCREMENT,
                                            `data_pagamento` date NOT NULL,
                                            `metodo_pagamento` enum('BOLETO_BANCARIO','CARTAO_CREDITO','CARTAO_DEBITO','DINHEIRO','PIX','TRANSFERENCIA_BANCARIA') DEFAULT NULL,
    `valor_pago` decimal(12,2) NOT NULL,
    `divida_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_pagamentos_dividas` (`divida_id`),
    CONSTRAINT `fk_pagamentos_dividas` FOREIGN KEY (`divida_id`) REFERENCES `dividas` (`id`)
    ) ENGINE=InnoDB;

-- Tabela: notificacoes
CREATE TABLE IF NOT EXISTS `notificacoes` (
                                              `id_notificacao` bigint NOT NULL AUTO_INCREMENT,
                                              `data_envio` datetime(6) NOT NULL,
    `mensagem` text NOT NULL,
    `tipo_notificacao` enum('AVISO_DIVIDA_VENCIDA','CADASTRO_CLIENTE','COMPRA_REALIZADA','CONFIRMACAO_COMPRA','CONFIRMACAO_PAGAMENTO','LEMBRETE_PAGAMENTO','LEMBRETE_VENCIMENTO','PAGAMENTO_RECEBIDO') NOT NULL,
    `destinatario_id` bigint NOT NULL,
    PRIMARY KEY (`id_notificacao`),
    KEY `fk_notificacoes_pessoas` (`destinatario_id`),
    CONSTRAINT `fk_notificacoes_pessoas` FOREIGN KEY (`destinatario_id`) REFERENCES `pessoas` (`id`)
    ) ENGINE=InnoDB;
