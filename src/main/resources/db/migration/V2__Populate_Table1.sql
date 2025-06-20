-- Limpa as tabelas antes de inserir novos dados (ordem reversa de criação para respeitar as FKs)
DELETE FROM `notificacoes`;
DELETE FROM `pagamentos`;
DELETE FROM `dividas`;
DELETE FROM `itens_venda`;
DELETE FROM `vendas`;
DELETE FROM `proprietarios`;
DELETE FROM `clientes`;
DELETE FROM `fiadores`;
DELETE FROM `funcionarios`;
DELETE FROM `pessoas`;

-- Reseta o auto-incremento das tabelas
ALTER TABLE `pessoas` AUTO_INCREMENT = 1;
ALTER TABLE `vendas` AUTO_INCREMENT = 1;
ALTER TABLE `itens_venda` AUTO_INCREMENT = 1;
ALTER TABLE `dividas` AUTO_INCREMENT = 1;
ALTER TABLE `pagamentos` AUTO_INCREMENT = 1;
ALTER TABLE `notificacoes` AUTO_INCREMENT = 1;

-- Carga de dados para a tabela: pessoas
-- (Proprietário, Funcionários, Clientes, Fiador)
INSERT INTO `pessoas` (`id`, `nome`, `cpf`, `email`, `telefone`, `endereco`) VALUES
                                                                                 (1, 'Carlos Almeida', '111.111.111-11', 'carlos.almeida@email.com', '37999991111', 'Rua das Flores, 123, Centro'),
                                                                                 (2, 'Beatriz Costa', '222.222.222-22', 'beatriz.costa@email.com', '37999992222', 'Avenida Principal, 456, Bairro Novo'),
                                                                                 (3, 'João Silva', '333.333.333-33', 'joao.silva@email.com', '37999993333', 'Rua da Matriz, 789, Vila Rica'),
                                                                                 (4, 'Mariana Oliveira', '444.444.444-44', 'mariana.oliveira@email.com', '37999994444', 'Travessa das Pedras, 101, Centro'),
                                                                                 (5, 'Ricardo Pereira', '555.555.555-55', 'ricardo.pereira@email.com', '37999995555', 'Avenida do Comércio, 202, Bairro Industrial'),
                                                                                 (6, 'Fernanda Lima', '666.666.666-66', 'fernanda.lima@email.com', '37999996666', 'Rua dos Girassóis, 303, Jardim das Flores');

-- Carga de dados para a tabela: funcionarios
INSERT INTO `funcionarios` (`id`, `login`, `senha_hash`, `cargo`, `matricula`) VALUES
                                                                                   (1, 'carlos.almeida', 'senha_super_segura_123', 'Proprietário', 'PROP-001'),
                                                                                   (2, 'beatriz.costa', 'senha_muito_forte_456', 'Vendedor', 'VEND-001');

-- Carga de dados para a tabela: proprietarios
INSERT INTO `proprietarios` (`id`) VALUES
    (1);

-- Carga de dados para a tabela: fiadores
INSERT INTO `fiadores` (`id`) VALUES
    (6); -- Fernanda Lima será fiadora

-- Carga de dados para a tabela: clientes
INSERT INTO `clientes` (`id`, `limite_credito`, `prazo_pagamento_padrao_dias`, `fiador_id`) VALUES
                                                                                                (3, 1000.00, 30, NULL), -- João Silva
                                                                                                (4, 500.00, 15, 6),   -- Mariana Oliveira, com fiador
                                                                                                (5, 2500.00, 45, NULL); -- Ricardo Pereira

-- Carga de dados para a tabela: vendas
-- Venda 1 (Cliente: João Silva, Vendedor: Beatriz Costa)
INSERT INTO `vendas` (`id`, `data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES
    (1, '2025-06-01 10:30:00.000000', 150.00, 3, 2);
-- Venda 2 (Cliente: Mariana Oliveira, Vendedor: Beatriz Costa)
INSERT INTO `vendas` (`id`, `data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES
    (2, '2025-06-05 14:00:00.000000', 75.50, 4, 2);
-- Venda 3 (Cliente: Ricardo Pereira, Vendedor: Carlos Almeida)
INSERT INTO `vendas` (`id`, `data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES
    (3, '2025-06-10 18:00:00.000000', 320.00, 5, 1);
-- Venda 4 (Cliente: João Silva, Vendedor: Beatriz Costa) - Será paga totalmente
INSERT INTO `vendas` (`id`, `data_hora`, `valor_total`, `cliente_id`, `funcionario_id`) VALUES
    (4, '2025-06-12 11:00:00.000000', 50.00, 3, 2);


-- Carga de dados para a tabela: itens_venda
-- Itens da Venda 1
INSERT INTO `itens_venda` (`venda_id`, `descricao_produto`, `quantidade`, `preco_unitario`, `subtotal`) VALUES
                                                                                                            (1, 'Arroz 5kg', 2, 25.00, 50.00),
                                                                                                            (1, 'Feijão 1kg', 5, 8.00, 40.00),
                                                                                                            (1, 'Óleo de Soja 900ml', 10, 6.00, 60.00);
-- Itens da Venda 2
INSERT INTO `itens_venda` (`venda_id`, `descricao_produto`, `quantidade`, `preco_unitario`, `subtotal`) VALUES
                                                                                                            (2, 'Sabonete', 10, 2.55, 25.50),
                                                                                                            (2, 'Shampoo 400ml', 2, 25.00, 50.00);
-- Itens da Venda 3
INSERT INTO `itens_venda` (`venda_id`, `descricao_produto`, `quantidade`, `preco_unitario`, `subtotal`) VALUES
    (3, 'Pneu Aro 15', 1, 320.00, 320.00);
-- Itens da Venda 4
INSERT INTO `itens_venda` (`venda_id`, `descricao_produto`, `quantidade`, `preco_unitario`, `subtotal`) VALUES
    (4, 'Chocolate em Barra', 10, 5.00, 50.00);

-- Carga de dados para a tabela: dividas
INSERT INTO `dividas` (`venda_origem_id`, `cliente_id`, `data_emissao`, `data_vencimento`, `valor_original`, `valor_pendente`, `status_divida`) VALUES
                                                                                                                                                    (1, 3, '2025-06-01', '2025-07-01', 150.00, 100.00, 'PAGA_PARCIALMENTE'),
                                                                                                                                                    (2, 4, '2025-06-05', '2025-06-20', 75.50, 75.50, 'ABERTA'),
                                                                                                                                                    (3, 5, '2025-06-10', '2025-07-25', 320.00, 320.00, 'ABERTA'),
                                                                                                                                                    (4, 3, '2025-06-12', '2025-07-12', 50.00, 0.00, 'PAGA_TOTALMENTE');


-- Carga de dados para a tabela: pagamentos
-- Pagamento parcial da divida 1
INSERT INTO `pagamentos` (`divida_id`, `data_pagamento`, `valor_pago`, `metodo_pagamento`) VALUES
    (1, '2025-06-15', 50.00, 'PIX');
-- Pagamento total da divida 4
INSERT INTO `pagamentos` (`divida_id`, `data_pagamento`, `valor_pago`, `metodo_pagamento`) VALUES
    (4, '2025-06-20', 50.00, 'DINHEIRO');

-- Carga de dados para a tabela: notificacoes
INSERT INTO `notificacoes` (`destinatario_id`, `data_envio`, `mensagem`, `tipo_notificacao`) VALUES
                                                                                                 (3, '2025-06-15 11:00:00.000000', 'Seu pagamento de R$ 50,00 foi recebido com sucesso.', 'PAGAMENTO_RECEBIDO'),
                                                                                                 (4, '2025-06-18 09:00:00.000000', 'Lembrete: sua dívida no valor de R$ 75,50 vence em 2 dias.', 'LEMBRETE_VENCIMENTO'),
                                                                                                 (5, '2025-06-10 18:05:00.000000', 'Sua compra de R$ 320,00 foi registrada. Obrigado pela preferência!', 'COMPRA_REALIZADA');
