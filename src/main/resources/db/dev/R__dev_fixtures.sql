-- Somente perfil dev-fixtures. Identidades localizadas por login/CPF; não depende de IDs legados.
INSERT INTO pessoas(nome,cpf) SELECT 'Gerente Dev','52998224725' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='52998224725') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.gerente');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.gerente' UNION SELECT id FROM pessoas WHERE cpf_normalizado='52998224725' LIMIT 1);
INSERT IGNORE INTO funcionarios(id,login,senha_hash,cargo,matricula) VALUES(@pessoa,'dev.gerente','{migrado-para-usuarios}','GERENTE',NULL);
INSERT IGNORE INTO proprietarios(id) VALUES(@pessoa);
INSERT IGNORE INTO usuarios(pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(@pessoa,'dev.gerente','$2a$10$HUbWyddT8XKY0tcNLaoPUOEOfyKOK70o.bOEa4kv0NmvEPoyHGoxq','GERENTE',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(nome,cpf) SELECT 'Funcionário Dev','11144477735' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='11144477735') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.funcionario');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.funcionario' UNION SELECT id FROM pessoas WHERE cpf_normalizado='11144477735' LIMIT 1);
INSERT IGNORE INTO funcionarios(id,login,senha_hash,cargo,matricula) VALUES(@pessoa,'dev.funcionario','{migrado-para-usuarios}','FUNCIONARIO',NULL);
INSERT IGNORE INTO usuarios(pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(@pessoa,'dev.funcionario','$2a$10$XFlmct08tkIJsW1FjWt4a.J8iskIBcA4370/oyrkyz5qFNZXiPiiC','FUNCIONARIO',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(nome,cpf) SELECT 'Cliente Dev C','12345678909' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='12345678909') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.cliente1');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.cliente1' UNION SELECT id FROM pessoas WHERE cpf_normalizado='12345678909' LIMIT 1);
INSERT IGNORE INTO clientes(id,limite_credito,prazo_pagamento_padrao_dias) VALUES(@pessoa,1000,30);
INSERT IGNORE INTO usuarios(pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(@pessoa,'dev.cliente1','$2a$10$yH/UEEqqEFsHpA3I2LTaruiNgVHvjeKzfKaynhpC4Z7pxHEFemve.','CLIENTE',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(nome,cpf) SELECT 'Cliente Dev D','98765432100' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='98765432100') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.cliente2');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.cliente2' UNION SELECT id FROM pessoas WHERE cpf_normalizado='98765432100' LIMIT 1);
INSERT IGNORE INTO clientes(id,limite_credito,prazo_pagamento_padrao_dias) VALUES(@pessoa,1000,30);
INSERT IGNORE INTO usuarios(pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(@pessoa,'dev.cliente2','$2a$10$DxEpYiy6mOTNMtf9.gfweui2uvNnKK1no3sSrq1FSgHqu/zN57fHm','CLIENTE',1,UTC_TIMESTAMP(6));
