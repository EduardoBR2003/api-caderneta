-- V4: Dados Reais para Relatórios (Mensal e Semanal)

-- 1. SEMANA 4 (HOJE/ONTEM) - Dinheiro e Pix
INSERT INTO `vendas` (`data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES (NOW(), 1500.00, 3, 2);
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`)
VALUES (LAST_INSERT_ID(), 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1500.00, 1500.00, 'ABERTA');

INSERT INTO `vendas` (`data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES (DATE_SUB(NOW(), INTERVAL 1 DAY), 2000.00, 4, 2);
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`)
VALUES (LAST_INSERT_ID(), 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 2000.00, 0.00, 'PAGA_TOTALMENTE');
INSERT INTO `pagamentos` (`data_pagamento`, `valor_pago`, `metodo_pagamento`, `divida_id`)
VALUES (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 2000.00, 'DINHEIRO', LAST_INSERT_ID());

-- 2. SEMANA 3 (7 DIAS ATRÁS) - Cartão
INSERT INTO `vendas` (`data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES (DATE_SUB(NOW(), INTERVAL 8 DAY), 3500.00, 5, 1);
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`)
VALUES (LAST_INSERT_ID(), 5, DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 3500.00, 1000.00, 'PAGA_PARCIALMENTE');
INSERT INTO `pagamentos` (`data_pagamento`, `valor_pago`, `metodo_pagamento`, `divida_id`)
VALUES (DATE_SUB(CURDATE(), INTERVAL 8 DAY), 2500.00, 'CARTAO_CREDITO', LAST_INSERT_ID());

-- 3. SEMANA 2 (15 DIAS ATRÁS) - Pix
INSERT INTO `vendas` (`data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES (DATE_SUB(NOW(), INTERVAL 15 DAY), 4200.00, 3, 2);
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`)
VALUES (LAST_INSERT_ID(), 3, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 4200.00, 0.00, 'PAGA_TOTALMENTE');
INSERT INTO `pagamentos` (`data_pagamento`, `valor_pago`, `metodo_pagamento`, `divida_id`)
VALUES (DATE_SUB(CURDATE(), INTERVAL 15 DAY), 4200.00, 'PIX', LAST_INSERT_ID());

-- 4. SEMANA 1 (22 DIAS ATRÁS) - Transferência
INSERT INTO `vendas` (`data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES (DATE_SUB(NOW(), INTERVAL 22 DAY), 1200.00, 4, 1);
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`)
VALUES (LAST_INSERT_ID(), 4, DATE_SUB(CURDATE(), INTERVAL 22 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1200.00, 1200.00, 'VENCIDA');