-- Somente perfil dev-fixtures. Tudo na Loja Principal (id 1, V12); CPF único por loja e papel. Identidades localizadas por login/CPF; não depende de IDs legados.
INSERT INTO pessoas(loja_id,nome,cpf,papel) SELECT 1,'Gerente Dev','52998224725','EQUIPE' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='52998224725' AND papel='EQUIPE') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.gerente');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.gerente' UNION SELECT id FROM pessoas WHERE cpf_normalizado='52998224725' AND papel='EQUIPE' LIMIT 1);
INSERT IGNORE INTO funcionarios(id,login,senha_hash,cargo,matricula) VALUES(@pessoa,'dev.gerente','{migrado-para-usuarios}','GERENTE',NULL);
INSERT IGNORE INTO proprietarios(id) VALUES(@pessoa);
INSERT IGNORE INTO usuarios(loja_id,pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(1,@pessoa,'dev.gerente','$2a$10$HUbWyddT8XKY0tcNLaoPUOEOfyKOK70o.bOEa4kv0NmvEPoyHGoxq','GERENTE',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(loja_id,nome,cpf,papel) SELECT 1,'Funcionário Dev','11144477735','EQUIPE' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='11144477735' AND papel='EQUIPE') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.funcionario');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.funcionario' UNION SELECT id FROM pessoas WHERE cpf_normalizado='11144477735' AND papel='EQUIPE' LIMIT 1);
INSERT IGNORE INTO funcionarios(id,login,senha_hash,cargo,matricula) VALUES(@pessoa,'dev.funcionario','{migrado-para-usuarios}','FUNCIONARIO',NULL);
INSERT IGNORE INTO usuarios(loja_id,pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(1,@pessoa,'dev.funcionario','$2a$10$XFlmct08tkIJsW1FjWt4a.J8iskIBcA4370/oyrkyz5qFNZXiPiiC','FUNCIONARIO',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(loja_id,nome,cpf,papel) SELECT 1,'Cliente Dev C','12345678909','CLIENTE' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='12345678909' AND papel='CLIENTE') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.cliente1');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.cliente1' UNION SELECT id FROM pessoas WHERE cpf_normalizado='12345678909' AND papel='CLIENTE' LIMIT 1);
INSERT IGNORE INTO clientes(id,limite_credito,prazo_pagamento_padrao_dias) VALUES(@pessoa,1000,30);
INSERT IGNORE INTO usuarios(loja_id,pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(1,@pessoa,'dev.cliente1','$2a$10$yH/UEEqqEFsHpA3I2LTaruiNgVHvjeKzfKaynhpC4Z7pxHEFemve.','CLIENTE',1,UTC_TIMESTAMP(6));
INSERT INTO pessoas(loja_id,nome,cpf,papel) SELECT 1,'Cliente Dev D','98765432100','CLIENTE' WHERE NOT EXISTS(SELECT 1 FROM pessoas WHERE cpf_normalizado='98765432100' AND papel='CLIENTE') AND NOT EXISTS(SELECT 1 FROM usuarios WHERE login='dev.cliente2');
SET @pessoa=(SELECT pessoa_id FROM usuarios WHERE login='dev.cliente2' UNION SELECT id FROM pessoas WHERE cpf_normalizado='98765432100' AND papel='CLIENTE' LIMIT 1);
INSERT IGNORE INTO clientes(id,limite_credito,prazo_pagamento_padrao_dias) VALUES(@pessoa,1000,30);
INSERT IGNORE INTO usuarios(loja_id,pessoa_id,login,senha_hash,perfil,ativo,criado_em) VALUES(1,@pessoa,'dev.cliente2','$2a$10$DxEpYiy6mOTNMtf9.gfweui2uvNnKK1no3sSrq1FSgHqu/zN57fHm','CLIENTE',1,UTC_TIMESTAMP(6));
