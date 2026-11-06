
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pessoas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cpf` varchar(14) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `endereco` varchar(200) DEFAULT NULL,
  `nome` varchar(150) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cpf` (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funcionarios` (
  `id` bigint NOT NULL,
  `cargo` varchar(50) DEFAULT NULL,
  `login` varchar(50) NOT NULL,
  `matricula` varchar(50) DEFAULT NULL,
  `senha_hash` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_login` (`login`),
  UNIQUE KEY `UK_matricula` (`matricula`),
  CONSTRAINT `fk_funcionarios_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proprietarios` (
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_proprietarios_funcionarios` FOREIGN KEY (`id`) REFERENCES `funcionarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` bigint NOT NULL,
  `limite_credito` decimal(10,2) DEFAULT NULL,
  `prazo_pagamento_padrao_dias` int DEFAULT NULL,
  `fiador_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_clientes_fiadores` (`fiador_id`),
  CONSTRAINT `fk_clientes_fiadores` FOREIGN KEY (`fiador_id`) REFERENCES `fiadores` (`id`),
  CONSTRAINT `fk_clientes_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fiadores` (
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_fiadores_pessoas` FOREIGN KEY (`id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendas` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `itens_venda` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao_produto` varchar(200) NOT NULL,
  `preco_unitario` decimal(10,2) NOT NULL,
  `quantidade` int NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `venda_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_itensvenda_vendas` (`venda_id`),
  CONSTRAINT `fk_itensvenda_vendas` FOREIGN KEY (`venda_id`) REFERENCES `vendas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dividas` (
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
  CONSTRAINT `fk_dividas_clientes` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `fk_dividas_vendas` FOREIGN KEY (`venda_origem_id`) REFERENCES `vendas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagamentos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `data_pagamento` date NOT NULL,
  `metodo_pagamento` enum('BOLETO_BANCARIO','CARTAO_CREDITO','CARTAO_DEBITO','DINHEIRO','PIX','TRANSFERENCIA_BANCARIA') DEFAULT NULL,
  `valor_pago` decimal(12,2) NOT NULL,
  `divida_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pagamentos_dividas` (`divida_id`),
  CONSTRAINT `fk_pagamentos_dividas` FOREIGN KEY (`divida_id`) REFERENCES `dividas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacoes` (
  `id_notificacao` bigint NOT NULL AUTO_INCREMENT,
  `data_envio` datetime(6) NOT NULL,
  `mensagem` text NOT NULL,
  `tipo_notificacao` enum('AVISO_DIVIDA_VENCIDA','CADASTRO_CLIENTE','COMPRA_REALIZADA','CONFIRMACAO_COMPRA','CONFIRMACAO_PAGAMENTO','LEMBRETE_PAGAMENTO','LEMBRETE_VENCIMENTO','PAGAMENTO_RECEBIDO') NOT NULL,
  `destinatario_id` bigint NOT NULL,
  `lida` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id_notificacao`),
  KEY `fk_notificacoes_pessoas` (`destinatario_id`),
  KEY `ix_notificacoes_data` (`data_envio`,`id_notificacao`),
  KEY `ix_notificacoes_dest_data` (`destinatario_id`,`data_envio`,`id_notificacao`),
  CONSTRAINT `fk_notificacoes_pessoas` FOREIGN KEY (`destinatario_id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pessoa_id` bigint NOT NULL,
  `login` varchar(50) NOT NULL,
  `senha_hash` varchar(100) NOT NULL,
  `perfil` enum('GERENTE','FUNCIONARIO','CLIENTE') NOT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1',
  `criado_em` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuarios_pessoa` (`pessoa_id`),
  UNIQUE KEY `uk_usuarios_login` (`login`),
  CONSTRAINT `fk_usuarios_pessoas` FOREIGN KEY (`pessoa_id`) REFERENCES `pessoas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `convites_cliente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint NOT NULL,
  `codigo_hash` varchar(64) NOT NULL,
  `expira_em` datetime(6) NOT NULL,
  `usado_em` datetime(6) DEFAULT NULL,
  `criado_por_usuario_id` bigint NOT NULL,
  `criado_em` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_convites_codigo` (`codigo_hash`),
  KEY `ix_convites_cliente` (`cliente_id`),
  KEY `fk_convites_usuarios` (`criado_por_usuario_id`),
  CONSTRAINT `fk_convites_clientes` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `fk_convites_usuarios` FOREIGN KEY (`criado_por_usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SPRING_SESSION` (
  `PRIMARY_ID` char(36) NOT NULL,
  `SESSION_ID` char(36) NOT NULL,
  `CREATION_TIME` bigint NOT NULL,
  `LAST_ACCESS_TIME` bigint NOT NULL,
  `MAX_INACTIVE_INTERVAL` int NOT NULL,
  `EXPIRY_TIME` bigint NOT NULL,
  `PRINCIPAL_NAME` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`PRIMARY_ID`),
  UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
  KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
  KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SPRING_SESSION_ATTRIBUTES` (
  `SESSION_PRIMARY_ID` char(36) NOT NULL,
  `ATTRIBUTE_NAME` varchar(200) NOT NULL,
  `ATTRIBUTE_BYTES` blob NOT NULL,
  PRIMARY KEY (`SESSION_PRIMARY_ID`,`ATTRIBUTE_NAME`),
  CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `SPRING_SESSION` (`PRIMARY_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria_eventos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ocorrido_em` datetime(6) NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `ator_login` varchar(50) DEFAULT NULL,
  `perfil` varchar(20) DEFAULT NULL,
  `acao` varchar(60) NOT NULL,
  `recurso_tipo` varchar(40) NOT NULL,
  `recurso_id` bigint DEFAULT NULL,
  `resultado` varchar(20) NOT NULL,
  `alteracoes` text,
  PRIMARY KEY (`id`),
  KEY `ix_auditoria_ocorrido` (`ocorrido_em`,`id`),
  KEY `ix_auditoria_acao` (`acao`),
  KEY `ix_auditoria_recurso` (`recurso_tipo`,`recurso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

