# api-caderneta

API Spring Boot, JDK 21 e MySQL 8.0.46. O integrador mantém os [contratos e permissões atuais](../docs/contratos-e-permissoes.md), [preparação e backup](../README.md) e [relatório de homologação](../docs/entrega-homologacao-2026-11-05.md).

Use Maven 3.9.9/JDK 21 para testar. A aplicação exige credenciais externas distintas para uso normal e migrations; Hibernate valida o esquema. Instalações novas usam B7 sem demonstrações e V8 incremental. Não reescreva migrations já aplicadas nem aplique cargas históricas pendentes sobre dados existentes sem reconciliar uma cópia isolada.

Venda tem total próprio e itens opcionais. Funcionário consulta/edita apenas suas vendas; cobranças são compartilhadas. Operações financeiras usam bloqueio por cliente/dívida, versão e idempotência persistente. O Dockerfile.local empacota o JAR testado pelo script do integrador.

## Diagrama de classe

![image](https://github.com/user-attachments/assets/1df3b005-7c0e-4dd3-8d83-4e1fe2acd7c4)

