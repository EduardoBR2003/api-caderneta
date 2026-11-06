-- FIXTURES DE DESENVOLVIMENTO. Carregadas somente com o perfil "dev-fixtures". Nunca em produção.
-- Contas fictícias com senhas de desenvolvimento (documentadas em docs/backend-integracao.md):
--   dev.gerente / dev.funcionario / dev.cliente1 (João Silva, pessoa 3) / dev.cliente2 (Mariana Oliveira, pessoa 4)
-- Os dois clientes já possuem vendas, dívidas e eventos distintos vindos de V2/V3/V4.
-- Idempotente (INSERT IGNORE): reexecutar não duplica nada.

INSERT IGNORE INTO pessoas (id, nome, cpf, email, telefone, endereco) VALUES
    (1001, 'Gerente Dev', '000.000.001-01', 'gerente.dev@exemplo.local', '37900000001', 'Endereço de desenvolvimento'),
    (1002, 'Funcionário Dev', '000.000.002-02', 'funcionario.dev@exemplo.local', '37900000002', 'Endereço de desenvolvimento');

INSERT IGNORE INTO funcionarios (id, login, senha_hash, cargo, matricula) VALUES
    (1001, 'dev.gerente', '{migrado-para-usuarios}', 'Gerente', 'DEV-001'),
    (1002, 'dev.funcionario', '{migrado-para-usuarios}', 'Vendedor', 'DEV-002');

INSERT IGNORE INTO proprietarios (id) VALUES (1001);

INSERT IGNORE INTO usuarios (pessoa_id, login, senha_hash, perfil, ativo, criado_em) VALUES
    (1001, 'dev.gerente',      '$2a$10$HUbWyddT8XKY0tcNLaoPUOEOfyKOK70o.bOEa4kv0NmvEPoyHGoxq', 'GERENTE',     1, UTC_TIMESTAMP(6)),
    (1002, 'dev.funcionario',  '$2a$10$XFlmct08tkIJsW1FjWt4a.J8iskIBcA4370/oyrkyz5qFNZXiPiiC', 'FUNCIONARIO', 1, UTC_TIMESTAMP(6)),
    (3,    'dev.cliente1',     '$2a$10$yH/UEEqqEFsHpA3I2LTaruiNgVHvjeKzfKaynhpC4Z7pxHEFemve.', 'CLIENTE',     1, UTC_TIMESTAMP(6)),
    (4,    'dev.cliente2',     '$2a$10$DxEpYiy6mOTNMtf9.gfweui2uvNnKK1no3sSrq1FSgHqu/zN57fHm', 'CLIENTE',     1, UTC_TIMESTAMP(6));
