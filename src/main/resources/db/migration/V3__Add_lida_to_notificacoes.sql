-- Migration: Adiciona coluna 'lida' à tabela notificacoes
ALTER TABLE notificacoes
    ADD COLUMN lida TINYINT(1) NOT NULL DEFAULT 0;
