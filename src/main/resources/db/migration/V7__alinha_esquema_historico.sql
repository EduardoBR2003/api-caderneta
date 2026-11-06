-- A coluna "lida" existia na entidade Notificacao mas só era criada pelo Hibernate (ddl-auto=update),
-- fora do controle do Flyway. Em bancos novos, as migrations isoladas deixavam o esquema incompleto.
-- Condicional: em bancos que já têm a coluna não faz nada.
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notificacoes' AND COLUMN_NAME = 'lida');
SET @ddl := IF(@existe = 0,
               'ALTER TABLE notificacoes ADD COLUMN lida TINYINT(1) NOT NULL DEFAULT 0',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
