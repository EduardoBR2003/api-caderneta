# api-caderneta — API da Caderneta

API REST do **Caderneta**, sistema web para comércios registrarem **vendas a prazo** ("fiado"):
clientes com limite de crédito e prazo, vendas que geram dívidas, pagamentos parciais ou totais,
lembretes de vencimento, relatórios, histórico e auditoria. **Várias lojas** usam o mesmo sistema,
e cada uma só enxerga os próprios dados.

| | |
|---|---|
| **Linguagem / runtime** | Java 21 (Eclipse Temurin) |
| **Framework** | Spring Boot 4.1.2 (Spring Security 7.1, Spring Session JDBC 4.1, Spring Data JPA) |
| **Persistência** | Hibernate 7.4, Flyway 12, HikariCP 7 |
| **Banco** | MySQL 8.4 (desenvolvimento) e **TiDB Cloud Starter** (produção, compatível com MySQL) |
| **JSON** | Jackson 3 (com os padrões do Jackson 2 para manter o contrato) |
| **Documentação da API** | springdoc-openapi 3 (Swagger UI, só para gerente) |
| **Produção** | Google Cloud Run (`southamerica-east1`), atrás do Firebase Hosting |

Este repositório é usado como submódulo do projeto integrador
[`Projeto_Final_Caderneta`](https://github.com/LuisHRodrigues/Projeto_Final_Caderneta), que tem o
Docker Compose, os testes de navegador e a documentação de operação.

---

## Sumário

1. [Funcionalidades](#1-funcionalidades)
2. [Perfis e permissões](#2-perfis-e-permissões)
3. [Várias lojas (multi-inquilino)](#3-várias-lojas-multi-inquilino)
4. [Segurança](#4-segurança)
5. [Arquitetura e código](#5-arquitetura-e-código)
6. [Banco de dados e migrations](#6-banco-de-dados-e-migrations)
7. [Configuração (variáveis de ambiente)](#7-configuração-variáveis-de-ambiente)
8. [Como executar](#8-como-executar)
9. [Testes](#9-testes)
10. [Implantação em produção](#10-implantação-em-produção)
11. [Referência da API](#11-referência-da-api)
12. [Limitações conhecidas](#12-limitações-conhecidas)

---

## 1. Funcionalidades

**Lojas e contas**
- Cadastro público de loja (`POST /api/auth/lojas`): cria a loja e o dono dela, que vira **gerente**.
- Login por sessão (cookie), logout e consulta da sessão atual (com o nome da loja).
- Gerente cria contas de **funcionário**, ativa e desativa contas, **promove funcionário a gerente**
  (e o contrário) mantendo o mesmo login, **exclui** (exclusão lógica) e **restaura** contas.
- Recadastrar o CPF de um funcionário excluído **reativa a conta antiga** (mesmo login, senha nova).
- **Convite de cliente:** a loja gera um código de uso único; o cliente ativa o próprio acesso com
  CPF + código e passa a acompanhar suas compras e dívidas ("Minha caderneta").

**Operação**
- **Clientes:** cadastro, edição, busca, limite de crédito e prazo padrão de pagamento (só gerente
  altera limite e prazo), fiador opcional. O cliente não pode ser fiador de si mesmo.
- **Vendas a prazo:** total próprio e itens opcionais (até 200); cada venda gera uma **dívida** com
  vencimento calculado pelo prazo do cliente. A venda é recusada se ultrapassar o **limite de
  crédito** (saldo em aberto + nova venda). Funcionário vê e edita **só as próprias vendas**.
- **Pagamentos:** parciais ou totais (dinheiro, PIX, cartão de crédito/débito, transferência,
  boleto); o saldo e a situação da dívida (aberta, paga parcialmente, paga, vencida, cancelada) se
  atualizam sozinhos. Gerente pode **renegociar o vencimento** e cancelar dívidas.
- **Comprovantes** de venda e de pagamento (HTML para impressão), para a loja e para o cliente.
- **Lembretes de vencimento:** tarefa diária gera um aviso para cada dívida com saldo que vence no
  dia ou já venceu, sem duplicar.
- **Painel (Visão Geral), relatórios** (vendas a prazo, débitos pendentes, pagamentos, clientes,
  resumo do período), **histórico de eventos** e **auditoria** (quem fez o quê, antes/depois).

---

## 2. Perfis e permissões

Três perfis, verificados **no servidor** em toda requisição (o frontend só esconde o que não se
pode usar):

| Recurso | Gerente | Funcionário | Cliente |
|---|:-:|:-:|:-:|
| Clientes, fiadores, vendas, dívidas, pagamentos, histórico, painel | ✅ | ✅ | — |
| Limite de crédito e prazo do cliente; editar dívida; excluir venda/dívida/cliente | ✅ | — | — |
| Ver/editar vendas de **outros** vendedores | ✅ | — (só as próprias) | — |
| Relatórios, auditoria, contas (Acessos), Swagger | ✅ | — | — |
| Criar funcionário, promover/rebaixar, excluir/restaurar contas | ✅ | — | — |
| Gerar convite de cliente | ✅ | ✅ | — |
| "Minha caderneta" (`/api/me/**`): só os próprios dados | — | — | ✅ |

Rotas públicas: `GET /api/auth/csrf`, `GET /api/saude`, `GET /api/saude/vivo`,
`POST /api/auth/login`, `POST /api/auth/registro-cliente`, `POST /api/auth/lojas`. Qualquer rota
não listada na configuração é **negada** (`anyRequest().denyAll()`).

Regras de proteção da administração: não é possível desativar, rebaixar ou excluir o **último
gerente ativo da loja**, nem excluir a própria conta. Rotas legadas que gravavam senha fora da tabela
de contas (`POST /api/funcionarios`, `POST /api/v1/proprietarios`) estão **bloqueadas**.

---

## 3. Várias lojas (multi-inquilino)

O sistema é **um só para todas as lojas**, com banco único e a coluna `loja_id` em cada registro
(pessoas, contas, vendas, dívidas, pagamentos, notificações, convites e auditoria). O isolamento é
aplicado em **camadas**, para que um esquecimento numa camada não vaze dados:

1. **Filtro automático do Hibernate (`@TenantId`)** — toda consulta JPA às entidades com loja recebe
   `loja_id = loja do usuário logado`, e todo INSERT recebe a loja automaticamente
   ([`lojas/LojaTenantResolver.java`](src/main/java/br/com/api_caderneta/lojas/LojaTenantResolver.java)).
2. **Trava ao carregar (`@PostLoad`)** — se qualquer registro de outra loja for carregado (por id ou
   por associação), a API responde **404 "não encontrado"**, sem revelar que ele existe
   ([`lojas/GuardaLoja.java`](src/main/java/br/com/api_caderneta/lojas/GuardaLoja.java)).
3. **Filtro explícito** no SQL escrito à mão (painel, relatório resumido, lembretes), nas contas de
   acesso (o login é global e procurado antes de se saber a loja) e na auditoria.

Outras regras: o **login é único no sistema inteiro**; cada conta pertence a **uma** loja (quem
trabalha em duas lojas tem dois logins); o **CPF é único por loja e papel** (a mesma pessoa pode ser
cliente em várias lojas, e ser cliente, funcionário e fiador na mesma loja); a regra do último
gerente, a matrícula e o CPF valem **por loja**. Tarefas do sistema (migração, lembretes) rodam como
"raiz" e enxergam todas as lojas. Os dados anteriores às lojas estão na **"Loja Principal"** (id 1).

Isolamento testado de ponta a ponta em MySQL e TiDB (`ApiIT.lojasNaoEnxergamNemAlteramDadosUmasDasOutras`):
com duas lojas criadas pelo cadastro público, a loja B recebe 404 ou lista vazia ao **ler, alterar,
pagar, excluir, convidar ou gerenciar contas** da loja A, e o painel e os relatórios de cada loja
somam só os próprios dados.

---

## 4. Segurança

### 4.1 Criptografia em trânsito

| Trecho | Proteção |
|---|---|
| Navegador → Firebase Hosting → API | **HTTPS obrigatório**. HTTP é redirecionado; só **TLS 1.2 e 1.3** com cifras de sigilo encaminhado (ECDHE / TLS 1.3). TLS 1.0 e 1.1 são recusados (verificado em produção). |
| Endereço direto do Cloud Run (`*.run.app`) | Mesmo TLS 1.2/1.3; o domínio `.app` está na lista de **HSTS pré-carregado** dos navegadores (nunca é acessado por HTTP). A API também envia `Strict-Transport-Security`. |
| Firebase Hosting → Cloud Run | Rede interna do Google, criptografada em trânsito. |
| API → banco (TiDB Cloud) | **TLS obrigatório com verificação do certificado e do nome do servidor** (`sslMode=VERIFY_IDENTITY` na URL JDBC). |

### 4.2 Senhas e segredos

- Senhas de usuários guardadas com **BCrypt** (`BCryptPasswordEncoder`, custo 10, com *salt*
  aleatório por senha). A senha nunca é registrada em log nem devolvida pela API, e o hash é apagado
  da sessão após a autenticação (`CredentialsContainer`).
- Senha: mínimo de 8 caracteres e máximo de 72 bytes em UTF-8 (limite do BCrypt; acentos e emojis usam mais de um byte). Login: `^[A-Za-z0-9._-]{3,50}$`.
- **Sem senha padrão** em lugar nenhum: o primeiro gerente só é criado se as variáveis de bootstrap
  forem informadas, e elas devem ser removidas depois.
- **Segredos fora do código e da imagem:** em produção, as senhas do banco ficam no **Google Secret
  Manager** e são injetadas como variáveis no Cloud Run; localmente, no `.env` (ignorado pelo Git).
- **Dois usuários de banco**: `caderneta_app` (só `SELECT/INSERT/UPDATE/DELETE`, usado pela API) e
  `caderneta_migracao` (DDL, usado só pela tarefa de migração). A API em execução **não consegue
  alterar a estrutura do banco**.
- **Códigos de convite**: 20 caracteres de um alfabeto de 32 símbolos (~100 bits, `SecureRandom`);
  o banco guarda só o **SHA-256** do código. Uso único, validade de 7 dias, travado contra uso
  concorrente e conferido com o CPF do cliente; qualquer falha devolve a mesma mensagem (não revela
  qual dado estava errado).
- Varredura de segredos no histórico do Git (gitleaks) no CI e antes de cada publicação.

### 4.3 Sessão, cookie e CSRF

- Sessão no **servidor** (Spring Session JDBC, tabelas `SPRING_SESSION*`): funciona com várias
  instâncias da API e pode ser encerrada pelo servidor a qualquer momento.
- Cookie de sessão **`HttpOnly`** (o JavaScript da página não lê), **`Secure`** (só trafega em
  HTTPS) e **`SameSite=Lax`**; nome `__session` em produção (único cookie que o Firebase Hosting
  repassa ao Cloud Run). Configurado explicitamente em
  [`config/SessaoConfig.java`](src/main/java/br/com/api_caderneta/config/SessaoConfig.java) — no
  Boot 4.1 as propriedades `server.servlet.session.cookie.*` deixaram de valer para o Spring Session,
  regressão encontrada e corrigida durante a migração.
- Expira após **30 minutos sem uso**. Logout invalida a sessão no servidor.
- **Proteção contra fixação de sessão:** o id da sessão e o token CSRF são **renovados no login**.
- **CSRF:** toda requisição que altera dados exige o cabeçalho `X-CSRF-TOKEN` (token guardado na
  sessão do servidor, obtido em `GET /api/auth/csrf`). Sem ele: `403 CSRF_INVALID`.
- Desativar, excluir, promover ou rebaixar uma conta **encerra as sessões dela** imediatamente.
- Sessões criadas antes das lojas (sem loja) são recusadas e exigem novo login.

### 4.4 Proteção contra abuso

| Proteção | Regra |
|---|---|
| Tentativas de login | Bloqueio de 15 min após **5 falhas por login** ou **30 por IP** em 15 min — vale **mesmo com a senha certa** durante o bloqueio; verificado **antes** do BCrypt. Resposta `429 MUITAS_TENTATIVAS` com `Retry-After`. Estado persistido no banco (vale para várias instâncias); as chaves guardam só o **SHA-256** do login/IP. |
| Ativação de cliente (convite) | Falhas contam no limite por IP (impede tentativa e erro de códigos). |
| Cadastro de loja | Máximo de **5 cadastros por IP** na janela (configurável). |
| Mensagem de login | Conta inexistente, senha errada e conta inativa recebem a **mesma** resposta (`401 CREDENCIAIS_INVALIDAS`). |
| Ambiente local | O nginx do frontend limita `login` e `registro-cliente` a 10 requisições/min por IP. |

### 4.5 Integridade dos dados financeiros

- **Idempotência obrigatória** em `POST /api/vendas` e `POST /api/dividas/{id}/pagamentos`
  (cabeçalho `Idempotency-Key`): repetir a mesma requisição (rede instável, clique duplo) devolve o
  mesmo resultado, sem duplicar; reutilizar a chave com outro conteúdo é recusado (`409`).
- **Travas pessimistas** (`SELECT ... FOR UPDATE`) no cliente (venda × limite de crédito), na dívida
  (pagamentos concorrentes nunca ultrapassam o saldo) e no convite (uso único).
- **Controle de versão** (`@Version`) na venda: editar exige a versão atual (evita sobrescrever a
  alteração de outra pessoa).
- Valores em `BigDecimal`, com limites e casas decimais validados; datas em UTC.
- No TiDB Cloud é usado o `TiDBDialect` do Hibernate (o `MySQLDialect` gera `FOR UPDATE OF
  <apelido>`, recusado pelo TiDB Cloud).

### 4.6 Validação de entrada

- Bean Validation nos DTOs (`@NotBlank`, `@Size`, `@Email`, `@Pattern`) e regras de negócio no
  servidor (CPF com dígitos verificadores, nome 3–150, valores, prazo 0–3650 dias, itens).
- A API **não confia no frontend**: o responsável pela venda vem da sessão (não do corpo), perfis
  não podem ser escolhidos no cadastro, e todo id recebido passa pelo isolamento por loja.
- Hibernate em modo `validate`: a aplicação não sobe se o esquema do banco divergir das entidades.

### 4.7 Privacidade e respostas de erro

- **Logs sem dados pessoais:** CPF removido das mensagens; violações de integridade registram só o
  nome da constraint (a mensagem do banco poderia conter o valor duplicado).
- Erros em JSON padronizado (`code`, `message`), **sem stack trace nem SQL**; 404 para registros de
  outra loja (não revela existência).
- Respostas da API com `Cache-Control: no-store` (navegador e CDN não guardam dados de clientes),
  `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY`.
- `/api/saude` responde só `{"status":"UP"}`; `/actuator`, Swagger e `/v3/api-docs` exigem login de
  gerente.
- **CORS desligado** por padrão (frontend e API na mesma origem); só é habilitado em desenvolvimento
  com `caderneta.cors.allowed-origins`.

### 4.8 Auditoria

Toda ação administrativa e financeira relevante grava um evento em `auditoria_eventos`: quando,
quem (conta, login, perfil), ação (ex.: `VENDA_CRIADA`, `PAGAMENTO_REGISTRADO`, `CREDITO_ALTERADO`,
`PERFIL_ALTERADO`, `ACESSO_EXCLUIDO`, `LOJA_CRIADA`), recurso e o **antes/depois** dos campos
alterados. Cada loja vê só a própria auditoria.

### 4.9 Infraestrutura

- Imagem Docker em duas etapas; execução em `eclipse-temurin:21-jre-alpine` com `apk upgrade`, como
  **usuário sem privilégios** (`10001`).
- Cloud Run com **conta de serviço própria** (`caderneta-api-run`) que só lê os 3 segredos da
  aplicação; o agendador de lembretes usa outra conta que só pode executar o Job de lembretes.
- `-XX:+ExitOnOutOfMemoryError` (sem memória, a instância reinicia em vez de seguir degradada) e
  encerramento gracioso (termina as requisições em andamento ao receber `SIGTERM`).
- CI: testes, **gitleaks** e **Trivy** (dependências e imagem). Última varredura antes da publicação:
  0 vulnerabilidades altas ou críticas.

---

## 5. Arquitetura e código

```
src/main/java/br/com/api_caderneta/
├── Startup.java                  # ponto de entrada (Spring Boot)
├── auth/                         # login, sessão, convites, contas, gerente inicial, limite de tentativas
├── lojas/                        # várias lojas: LojaAtual, resolvedor do Hibernate, GuardaLoja, cadastro
├── security/                     # SecurityConfig (rotas/perfis), principal da sessão, erros 401/403
├── controller/                   # REST: clientes, vendas, dívidas, fiadores, relatórios, painel, saúde
├── me/                           # área do cliente (/api/me/**)
├── historico/                    # linha do tempo de eventos
├── audit/                        # auditoria (registro e consulta)
├── services/                     # regras de negócio (vendas, dívidas, clientes, notificações, idempotência)
├── tarefas/                      # tarefas únicas (migração, lembretes) e cron interno opcional
├── model/  dto/  mapper/         # entidades JPA, DTOs e conversão
├── repository/                   # Spring Data JPA (com travas pessimistas onde necessário)
├── exceptions/                   # exceções de negócio e tratamento global de erros
├── config/                       # sessão (cookie), Jackson, OpenAPI
└── util/                         # regras de validação (Regras), fuso horário, moeda
src/main/resources/
├── application.yml               # configuração base = PRODUÇÃO
├── application-dev.yml           # desenvolvimento local em HTTP
├── application-migracao.yml      # tarefa única: aplica as migrations e encerra
├── application-tarefa-lembretes.yml
└── db/migration/                 # Flyway (B7, B9, V1..V12); db/dev/ = dados fictícios locais
```

**Modelo de domínio:** `Pessoa` (base, herança JOINED) → `Cliente`, `Fiador`, `Funcionario` →
`Proprietario`; `Usuario` (conta de acesso, perfil GERENTE/FUNCIONARIO/CLIENTE); `Loja`; `Venda` →
`ItemVenda`; `Divida` (gerada pela venda) → `Pagamento`; `Notificacao`; `ConviteCliente`;
`AuditoriaEvento`.

**Perfis do Spring:** padrão = produção; `dev` (HTTP local, cookie sem `Secure`, migra ao subir);
`dev-fixtures` (dados fictícios); `migracao`; `tarefa-lembretes`.

![Diagrama de classes](https://github.com/user-attachments/assets/1df3b005-7c0e-4dd3-8d83-4e1fe2acd7c4)

*(diagrama original do projeto; não inclui `Loja`, `Usuario`, `ConviteCliente` nem
`AuditoriaEvento`)*

---

## 6. Banco de dados e migrations

- **A API não migra o banco.** As migrations rodam antes, na tarefa `migracao` (Cloud Run Job em
  produção, serviço `migracao` no Docker Compose), com o usuário de DDL.
- Bancos **novos** recebem só a base `B9__esquema_limpo_v9.sql` (equivalente a B7+V8+V9, sem dados de
  demonstração, compatível com TiDB) e depois V10..V12. Bancos **existentes** seguem versão a versão.
- Migrations recentes: `V9` limite de tentativas · `V10` exclusão lógica de contas · `V11` CPF único
  por papel · `V12` várias lojas (tabela `lojas`, `loja_id`, "Loja Principal", CPF único por loja e
  papel, encerra as sessões abertas).
- Um comando DDL por instrução (o TiDB não aceita várias alterações no mesmo `ALTER TABLE`).
- **Não reescreva migrations já aplicadas.** Faça backup antes de migrar produção (no TiDB, use
  `mysqldump` **sem** `--single-transaction`, pois o TiDB não tem `SAVEPOINT`).
- Script idempotente para criar/atualizar os dois usuários do MySQL:
  [`scripts/criar-usuarios-mysql.sh`](scripts/criar-usuarios-mysql.sh).

---

## 7. Configuração (variáveis de ambiente)

A configuração base é a de produção; tudo que muda por ambiente vem de variáveis. Lista completa no
integrador: [`docs/variaveis-de-ambiente.md`](https://github.com/LuisHRodrigues/Projeto_Final_Caderneta/blob/main/docs/variaveis-de-ambiente.md).

| Variável | Segredo? | Uso |
|---|:-:|---|
| `CADERNETA_DB_URL` | não | URL JDBC; em banco gerenciado com `?sslMode=VERIFY_IDENTITY&serverTimezone=UTC` |
| `CADERNETA_DB_USER` / `CADERNETA_DB_PASSWORD` | senha sim | usuário só de DML (API e tarefa de lembretes) |
| `CADERNETA_MIGRATION_USER` / `CADERNETA_MIGRATION_PASSWORD` | senha sim | usuário de DDL, **só** na tarefa de migração |
| `SPRING_PROFILES_ACTIVE` | não | vazio = produção; `migracao`; `tarefa-lembretes`; `dev` só local |
| `SPRING_JPA_DATABASE_PLATFORM` | não | **TiDB Cloud:** `org.hibernate.community.dialect.TiDBDialect` |
| `CADERNETA_SESSAO_COOKIE` | não | `__session` atrás do Firebase Hosting; local `SESSION` |
| `CADERNETA_AGENDAMENTO_INTERNO` | não | `false` no Cloud Run (lembretes pela tarefa agendada) |
| `CADERNETA_LOGIN_MAX_FALHAS` / `_MAX_FALHAS_IP` / `_JANELA` / `_BLOQUEIO` | não | limites de login (5 / 30 / 15m / 15m) |
| `CADERNETA_CADASTRO_LOJA_MAX_POR_IP` | não | cadastros de loja por IP (padrão 5) |
| `CADERNETA_DB_POOL_MAX` / `_POOL_MIN` / `_MAX_LIFETIME_MS` | não | pool de conexões (5 / 1 / 280000) |
| `CADERNETA_BOOTSTRAP_LOGIN` / `_SENHA` / `_NOME` / `_CPF` | senha sim | primeiro gerente da Loja Principal; só na migração, uma vez |
| `PORT` | não | injetada pelo Cloud Run (padrão 8080) |

---

## 8. Como executar

**Pela pilha completa (recomendado):** no integrador, `cp .env.example .env`, preencha as senhas e
`docker compose up -d --build` → frontend em http://localhost:3000, API em http://localhost:8082.
Veja o README do integrador.

**Só a API, com Maven** (JDK 21 + Maven 3.9 + MySQL 8.4 acessível):

```bash
export CADERNETA_DB_URL='jdbc:mysql://localhost:3307/api_caderneta_database?serverTimezone=UTC'
export CADERNETA_DB_PASSWORD='...' CADERNETA_MIGRATION_PASSWORD='...'
SPRING_PROFILES_ACTIVE=migracao mvn spring-boot:run   # aplica as migrations e encerra
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run        # sobe a API em http://localhost:8080
```

**Imagem Docker:** `docker build -t caderneta-api .` (compila e roda os testes unitários no build).
O `Dockerfile.local` empacota um JAR já construído.

**Swagger UI:** `/swagger-ui.html` (exige login de gerente).

---

## 9. Testes

```bash
mvn test     # unitários (80)
mvn verify   # + integração com MySQL 8.4 em container (Testcontainers; exige Docker)
mvn verify -Dcaderneta.it.imagem-banco=pingcap/tidb:v8.5.7 \
  -Dspring.jpa.database-platform=org.hibernate.community.dialect.TiDBDialect   # mesma suíte no TiDB
```

| Suíte | Cobertura | Resultado |
|---|---|---|
| Unitários | regras de vendas, dívidas, clientes, contas (promoção, exclusão, último gerente, conta de outra loja), auditoria, notificações | 80/80 |
| `ApiIT` (MySQL 8.4 e TiDB 8.5) | cookie `__session` e atributos, renovação da sessão no login, CSRF, logout, matriz de permissões, bloqueio de login, lembretes sem duplicar, pagamentos concorrentes, idempotência, contrato do frontend, promoção a gerente, exclusão/restauração/reativação, CPF por papel, **isolamento entre lojas** | 12/12 |
| `MigracaoIT` | banco novo pela base B9 até a V12, banco legado V1..V8 até a V12, idempotência, usuário da API sem DDL | 5/5 (1 pulado no TiDB) |

---

## 10. Implantação em produção

Topologia: **Firebase Hosting** (frontend, `/api/**` reencaminhado) → **Cloud Run** `caderneta-api`
(`southamerica-east1`, mínimo 0 / máximo 2 instâncias, 1 vCPU, 512 MiB) → **TiDB Cloud Starter**
(AWS São Paulo). Segredos no Secret Manager; imagens no Artifact Registry (`caderneta`).

Ordem de uma publicação com mudança de banco:
1. backup do banco;
2. imagem nova no Artifact Registry;
3. atualizar e executar o Job **`caderneta-migracao`** (`--wait`);
4. publicar o serviço **`caderneta-api`** e atualizar o Job **`caderneta-lembretes`**
   (agendado pelo Cloud Scheduler todo dia à 01:00, horário de Brasília).

**Deploy automático:** o workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda os
testes (MySQL e TiDB), o gitleaks e o Trivy em todo push e pull request; **a cada push na `main`**,
se tudo passar, publica sozinho: imagem no Artifact Registry → API no Cloud Run → Job de lembretes,
e verifica `/api/saude`. Autenticação por **Workload Identity Federation** (sem chave JSON; o
provedor só aceita este repositório na `main`; a conta `github-deployer` só publica no Cloud Run).

**Migrations:** se a nova versão trouxer migrations que a versão em produção não tem, o deploy
automático **para** de propósito. Faça o backup do banco e rode o workflow manualmente
(*Actions → backend → Run workflow*, branch `main`) marcando **`confirmo_migracao`**.

---

## 11. Referência da API

Base: `/api`. Respostas em JSON (exceto comprovantes, em HTML). Mutações exigem `X-CSRF-TOKEN`;
venda e pagamento exigem também `Idempotency-Key`. Paginação com `page` e `size`.

| Grupo | Rotas |
|---|---|
| Autenticação | `GET /auth/csrf` · `POST /auth/login` · `POST /auth/logout` · `GET /auth/me` · `POST /auth/registro-cliente` · `POST /auth/lojas` · `POST /auth/funcionarios` |
| Contas (gerente) | `GET /usuarios?excluidas=` · `PATCH /usuarios/{id}/ativo` · `PATCH /usuarios/{id}/perfil` · `DELETE /usuarios/{id}` · `POST /usuarios/{id}/restaurar` · `POST /clientes/{id}/convite` |
| Clientes | `GET /clientes` · `POST /clientes` · `GET/PUT/DELETE /clientes/{id}` · `PATCH /clientes/{id}/limite-credito` · `PATCH /clientes/{id}/prazo-pagamento` |
| Fiadores | `GET /fiadores` · `GET /fiadores/{id}` · `POST /fiadores` · `POST /fiadores/associar/cliente/{clienteId}/fiador/{fiadorId}` |
| Vendas | `GET /vendas` · `POST /vendas` · `GET/PUT/DELETE /vendas/{id}` · `GET /vendas/{id}/comprovante` |
| Dívidas e pagamentos | `GET /dividas` · `GET /dividas/{id}` · `GET /dividas/cliente/{clienteId}` · `PUT/DELETE /dividas/{id}` · `POST /dividas/{id}/pagamentos` · `GET /dividas/{dividaId}/pagamentos/{id}/comprovante` |
| Painel e relatórios | `GET /resumo` · `GET /relatorios/resumo` · `GET /relatorios/vendas-a-prazo` · `GET /relatorios/debitos-pendentes` · `GET /relatorios/pagamentos` · `GET /relatorios/clientes` |
| Histórico e auditoria | `GET /historico` · `GET /auditoria` |
| Área do cliente | `GET /me/resumo` · `GET /me/vendas` · `GET /me/dividas` · `GET /me/pagamentos` · `GET /me/historico` · comprovantes em `/me/...` |
| Saúde | `GET /saude` (prontidão, consulta o banco) · `GET /saude/vivo` (vivacidade) |
| Legado (gerente) | `/funcionarios/**`, `/v1/proprietarios/**`, `/notificacoes/**` (criação bloqueada; substituídos pelas rotas acima) |

A referência completa e testável está no Swagger UI.

---

## 12. Limitações conhecidas

- Ainda não há tela/rota para renomear a loja nem administração da plataforma (listar/desativar lojas).
- O cliente precisa de uma conta por loja (a "conta única do cliente" foi planejada e adiada).
- Notificações ficam no sistema (Histórico); não há envio por e-mail, SMS ou WhatsApp.
- O formato do telefone é validado só no frontend (o backend limita apenas o tamanho).
- A busca de clientes vai na URL (`?q=`): criptografada em trânsito, mas registrada nos logs de
  requisição do Cloud Run (acessíveis só aos administradores do projeto).
