-- Exclusão lógica de contas da equipe: "excluir" desativa e esconde a conta da tela Acessos sem
-- apagar o cadastro, o login nem o histórico (vendas continuam ligadas à pessoa).
-- NULL = conta visível. Um comando por instrução (compatível com TiDB).
ALTER TABLE usuarios ADD COLUMN excluido_em DATETIME(6) NULL;
