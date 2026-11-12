# api-caderneta

API Spring Boot 4.1, JDK 21 e MySQL 8.4 (testada também com TiDB 8.5). O integrador mantém os [contratos e permissões atuais](../docs/contratos-e-permissoes.md), [preparação e backup](../README.md) e [relatório de homologação](../docs/entrega-homologacao-2026-11-05.md).

Use Maven 3.9.9/JDK 21 para testar: `mvn test` (unitários) e `mvn verify` (integração com MySQL 8.4 em container; exige Docker). A aplicação exige credenciais externas distintas para uso normal e migrations (`scripts/criar-usuarios-mysql.sh`); Hibernate valida o esquema. A API não migra o banco: as migrations rodam antes, no perfil `migracao` (tarefa única), e os lembretes diários podem rodar no perfil `tarefa-lembretes` (agendador externo). Instalações novas usam só a base `B9` (equivalente a B7+V8+V9, sem demonstrações, compatível com TiDB); bancos existentes seguem pelas versões incrementais. Não reescreva migrations já aplicadas nem aplique cargas históricas pendentes sobre dados existentes sem reconciliar uma cópia isolada. Variáveis: `../docs/variaveis-de-ambiente.md`.

Venda tem total próprio e itens opcionais. Funcionário consulta/edita apenas suas vendas; cobranças são compartilhadas. Operações financeiras usam bloqueio por cliente/dívida, versão e idempotência persistente. O Dockerfile.local empacota o JAR testado pelo script do integrador.

## Diagrama de classe

![image](https://github.com/user-attachments/assets/1df3b005-7c0e-4dd3-8d83-4e1fe2acd7c4)

