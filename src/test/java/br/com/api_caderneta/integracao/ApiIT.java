package br.com.api_caderneta.integracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.api_caderneta.services.NotificacaoService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * API completa (configuração de produção) contra MySQL 8.4 real: cookie de sessão "__session" (Firebase
 * Hosting), CSRF, permissões por perfil, limite de login, agendamento idempotente e pagamentos
 * concorrentes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIT {

  static final String COOKIE = "__session";
  static final String GERENTE = "gerente.it";
  static final String SENHA_GERENTE = "Senha-Gerente-IT-1";

  @BeforeAll
  static void subir() {
    BancoMySql.iniciar();
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    BancoMySql.iniciar();
    r.add("CADERNETA_DB_URL", BancoMySql::url);
    r.add("CADERNETA_DB_PASSWORD", () -> BancoMySql.SENHA_APP);
    r.add("CADERNETA_SESSAO_COOKIE", () -> COOKIE);
    r.add("CADERNETA_AGENDAMENTO_INTERNO", () -> "false");
    r.add("caderneta.bootstrap.login", () -> GERENTE);
    r.add("caderneta.bootstrap.senha", () -> SENHA_GERENTE);
    r.add("caderneta.bootstrap.cpf", () -> "52998224725");
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired NotificacaoService notificacoes;

  /** Simula o navegador: guarda o cookie de sessão e o token CSRF. */
  class Navegador {
    String sessao;
    String csrf;
    String setCookie;

    MvcResult enviar(MockHttpServletRequestBuilder req) throws Exception {
      if (sessao != null) req.cookie(new Cookie(COOKIE, sessao));
      if (csrf != null) req.header("X-CSRF-TOKEN", csrf);
      MvcResult r = mvc.perform(req).andReturn();
      for (String h : r.getResponse().getHeaders("Set-Cookie")) {
        if (h.startsWith(COOKIE + "=")) {
          setCookie = h;
          String v = h.substring(COOKIE.length() + 1, h.indexOf(';'));
          sessao = v.isEmpty() ? null : v;
        }
      }
      return r;
    }

    MvcResult obterCsrf() throws Exception {
      MvcResult r = enviar(get("/api/auth/csrf"));
      csrf = json.readTree(r.getResponse().getContentAsString()).get("token").asText();
      return r;
    }

    MvcResult login(String login, String senha) throws Exception {
      obterCsrf();
      MvcResult r =
          enviar(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json.writeValueAsString(java.util.Map.of("login", login, "senha", senha))));
      if (r.getResponse().getStatus() == 200) obterCsrf(); // o token muda no login
      return r;
    }

    int status(MockHttpServletRequestBuilder req) throws Exception {
      var r = enviar(req).getResponse();
      if (r.getStatus() >= 400) System.out.println("RESPOSTA " + r.getStatus() + " " + r.getContentAsString());
      return r.getStatus();
    }
  }

  @Test
  void cookieDeSessaoUsaNomeDoFirebaseEAtributosSeguros() throws Exception {
    var n = new Navegador();
    n.obterCsrf();
    assertThat(n.sessao).as("cookie %s emitido", COOKIE).isNotNull();
    assertThat(n.setCookie).contains("HttpOnly").contains("Secure").containsIgnoringCase("SameSite=Lax");
    String antes = n.sessao;
    assertThat(n.login(GERENTE, SENHA_GERENTE).getResponse().getStatus()).isEqualTo(200);
    assertThat(n.sessao).as("id renovado no login (fixação de sessão)").isNotEqualTo(antes);
    JsonNode me = json.readTree(n.enviar(get("/api/auth/me")).getResponse().getContentAsString());
    assertThat(me.at("/usuario/perfil").asText()).isEqualTo("GERENTE");
  }

  @Test
  void sondasPublicasEAcessoAnonimo() throws Exception {
    var n = new Navegador();
    assertThat(n.status(get("/api/saude"))).isEqualTo(200);
    assertThat(n.status(get("/api/saude/vivo"))).isEqualTo(200);
    assertThat(n.status(get("/api/resumo"))).isEqualTo(401);
    assertThat(n.status(get("/api/auth/me"))).isEqualTo(401);
    assertThat(n.status(get("/v3/api-docs"))).isEqualTo(401);
  }

  @Test
  void mutacaoSemCsrfEhRecusadaELogoutEncerraSessao() throws Exception {
    var n = new Navegador();
    n.login(GERENTE, SENHA_GERENTE);
    String token = n.csrf;
    n.csrf = null;
    MvcResult semToken = n.enviar(post("/api/auth/logout"));
    assertThat(semToken.getResponse().getStatus()).isEqualTo(403);
    assertThat(semToken.getResponse().getContentAsString()).contains("CSRF_INVALID");
    n.csrf = token;
    assertThat(n.status(post("/api/auth/logout"))).isEqualTo(204);
    assertThat(n.status(get("/api/auth/me"))).isEqualTo(401);
  }

  @Test
  void matrizDePermissoes() throws Exception {
    var g = new Navegador();
    g.login(GERENTE, SENHA_GERENTE);
    String login = "func.it." + System.nanoTime() % 100000;
    MvcResult criado =
        g.enviar(
            post("/api/auth/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        java.util.Map.of(
                            "nome", "Funcionario IT",
                            "cpf", "111.444.777-35",
                            "cargo", "Vendedor",
                            "login", login,
                            "senha", "Senha-Func-IT-1"))));
    assertThat(criado.getResponse().getStatus()).isEqualTo(201);

    assertThat(g.status(get("/api/resumo"))).isEqualTo(200);
    assertThat(g.status(get("/api/relatorios/resumo?inicio=2026-01-01&fim=2026-12-31"))).isEqualTo(200);
    assertThat(g.status(get("/v3/api-docs"))).isEqualTo(200);
    assertThat(g.status(get("/api/me/vendas"))).isEqualTo(403);

    var f = new Navegador();
    assertThat(f.login(login, "Senha-Func-IT-1").getResponse().getStatus()).isEqualTo(200);
    assertThat(f.status(get("/api/resumo"))).isEqualTo(200);
    assertThat(f.status(get("/api/relatorios/resumo?inicio=2026-01-01&fim=2026-12-31"))).isEqualTo(403);
    assertThat(f.status(get("/api/auditoria"))).isEqualTo(403);
    assertThat(f.status(get("/v3/api-docs"))).isEqualTo(403);
    assertThat(f.status(get("/api/me/vendas"))).isEqualTo(403);
    assertThat(
            f.status(
                post("/api/auth/funcionarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")))
        .isEqualTo(403);
  }

  /** CPF válido (dígitos verificadores corretos) a partir de 9 dígitos. */
  static String cpf(long base9) {
    int[] d = new int[11];
    String b = String.format("%09d", base9 % 1_000_000_000L);
    for (int i = 0; i < 9; i++) d[i] = b.charAt(i) - '0';
    for (int j = 9; j < 11; j++) {
      int soma = 0;
      for (int i = 0; i < j; i++) soma += d[i] * (j + 1 - i);
      int dv = (soma * 10) % 11;
      d[j] = dv == 10 ? 0 : dv;
    }
    StringBuilder r = new StringBuilder();
    for (int x : d) r.append(x);
    return r.toString();
  }

  private MockHttpServletRequestBuilder perfil(long id, String perfil) {
    return patch("/api/usuarios/" + id + "/perfil")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"perfil\":\"" + perfil + "\"}");
  }

  @Test
  void gerentePromoveFuncionarioMantendoOMesmoLogin() throws Exception {
    var g = new Navegador();
    g.login(GERENTE, SENHA_GERENTE);
    String login = "promo.it." + System.nanoTime() % 100000;
    String senha = "Senha-Promo-IT-1";
    MvcResult criado =
        g.enviar(
            post("/api/auth/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        java.util.Map.of(
                            "nome", "Promovido IT",
                            "cpf", cpf(123_456_789L + System.nanoTime() % 1000),
                            "cargo", "Caixa",
                            "login", login,
                            "senha", senha))));
    assertThat(criado.getResponse().getStatus()).isEqualTo(201);
    long id = json.readTree(criado.getResponse().getContentAsString()).get("id").asLong();

    // Funcionário: não acessa área de gerente nem consegue se promover.
    var f = new Navegador();
    assertThat(f.login(login, senha).getResponse().getStatus()).isEqualTo(200);
    assertThat(f.status(get("/api/auditoria"))).isEqualTo(403);
    assertThat(f.status(perfil(id, "GERENTE"))).isEqualTo(403);

    // Gerente promove; perfil inválido é recusado.
    assertThat(g.status(perfil(id, "CLIENTE"))).isEqualTo(400);
    assertThat(g.status(perfil(id, "GERENTE"))).isEqualTo(204);
    assertThat(
            jdbc.queryForObject("select perfil from usuarios where id = ?", String.class, id))
        .isEqualTo("GERENTE");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from auditoria_eventos where acao = 'PERFIL_ALTERADO' and recurso_id = ?",
                Integer.class,
                id))
        .isEqualTo(1);

    // A sessão antiga cai; o MESMO login entra de novo já como gerente.
    assertThat(f.status(get("/api/auth/me"))).isEqualTo(401);
    var f2 = new Navegador();
    assertThat(f2.login(login, senha).getResponse().getStatus()).isEqualTo(200);
    JsonNode me = json.readTree(f2.enviar(get("/api/auth/me")).getResponse().getContentAsString());
    assertThat(me.at("/usuario/perfil").asText()).isEqualTo("GERENTE");
    assertThat(f2.status(get("/api/auditoria"))).isEqualTo(200);

    // O novo gerente pode rebaixar (há dois gerentes) e o rebaixado volta a ser funcionário.
    assertThat(g.status(perfil(id, "FUNCIONARIO"))).isEqualTo(204);
    var f3 = new Navegador();
    f3.login(login, senha);
    assertThat(f3.status(get("/api/auditoria"))).isEqualTo(403);

    // O último gerente ativo não pode ser rebaixado.
    Integer gerentes =
        jdbc.queryForObject(
            "select count(*) from usuarios where perfil = 'GERENTE' and ativo = true", Integer.class);
    if (gerentes != null && gerentes == 1) {
      long idGerente =
          jdbc.queryForObject("select id from usuarios where login = ?", Long.class, GERENTE);
      assertThat(g.status(perfil(idGerente, "FUNCIONARIO"))).isEqualTo(409);
    }
  }

  @Test
  void loginBloqueiaAposFalhasRepetidasMesmoComSenhaCerta() throws Exception {
    // Conta real criada para o teste, para provar que o bloqueio vale mesmo com a senha correta.
    var g = new Navegador();
    g.login(GERENTE, SENHA_GERENTE);
    String login = "alvo.it." + System.nanoTime() % 100000;
    g.enviar(
        post("/api/auth/funcionarios")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                json.writeValueAsString(
                    java.util.Map.of(
                        "nome", "Alvo IT", "cpf", "987.654.321-00", "cargo", "Vendedor",
                        "login", login, "senha", "Senha-Alvo-IT-1"))));

    var atacante = new Navegador();
    for (int i = 0; i < 5; i++) {
      assertThat(atacante.login(login, "errada" + i).getResponse().getStatus()).isEqualTo(401);
    }
    MvcResult bloqueado = atacante.login(login, "Senha-Alvo-IT-1");
    assertThat(bloqueado.getResponse().getStatus()).isEqualTo(429);
    assertThat(bloqueado.getResponse().getHeader("Retry-After")).isNotNull();
    assertThat(bloqueado.getResponse().getContentAsString()).contains("MUITAS_TENTATIVAS");
    // Outra conta não é afetada.
    assertThat(new Navegador().login(GERENTE, SENHA_GERENTE).getResponse().getStatus()).isEqualTo(200);
    // Nada de login em claro na tabela de controle.
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tentativas_acesso WHERE chave LIKE ?", Long.class,
            "%" + login + "%")).isZero();
  }

  /** Cria cliente + venda + dívida vencida diretamente no banco e devolve o id da dívida. */
  private long dividaVencida(String cpf, BigDecimal valor) {
    Long gerente = jdbc.queryForObject("SELECT pessoa_id FROM usuarios WHERE login=?", Long.class, GERENTE);
    jdbc.update("INSERT INTO pessoas(nome,cpf) VALUES(?,?)", "Cliente IT " + cpf, cpf);
    Long cliente = jdbc.queryForObject("SELECT id FROM pessoas WHERE cpf=?", Long.class, cpf);
    jdbc.update(
        "INSERT INTO clientes(id,limite_credito,prazo_pagamento_padrao_dias) VALUES(?,1000,30)", cliente);
    jdbc.update(
        "INSERT INTO vendas(data_hora,valor_total,cliente_id,funcionario_id) VALUES(UTC_TIMESTAMP(6),?,?,?)",
        valor, cliente, gerente);
    Long venda = jdbc.queryForObject("SELECT MAX(id) FROM vendas WHERE cliente_id=?", Long.class, cliente);
    var hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
    jdbc.update(
        "INSERT INTO dividas(data_emissao,data_vencimento,status_divida,valor_original,valor_pendente,"
            + "cliente_id,venda_origem_id) VALUES(?,?,'ABERTA',?,?,?,?)",
        hoje.minusDays(40), hoje.minusDays(10), valor, valor, cliente, venda);
    return jdbc.queryForObject("SELECT id FROM dividas WHERE venda_origem_id=?", Long.class, venda);
  }

  @Test
  void tarefaDeLembretesNaoDuplica() {
    long divida = dividaVencida("39053344705", new BigDecimal("50.00"));
    Long cliente = jdbc.queryForObject("SELECT cliente_id FROM dividas WHERE id=?", Long.class, divida);
    notificacoes.gerarNotificacoesDeVencimento();
    notificacoes.gerarNotificacoesDeVencimento();
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM notificacoes WHERE destinatario_id=? AND tipo_notificacao='LEMBRETE_VENCIMENTO'",
                Long.class,
                cliente))
        .isEqualTo(1L);
  }

  @Test
  void pagamentosConcorrentesNaoUltrapassamOSaldoEChaveRepetidaNaoDuplica() throws Exception {
    long divida = dividaVencida("71428793860", new BigDecimal("100.00"));
    var g = new Navegador();
    g.login(GERENTE, SENHA_GERENTE);
    String corpo = "{\"valorPago\":100.00,\"metodoPagamento\":\"PIX\"}";

    // Dois pagamentos integrais simultâneos com chaves diferentes: só um pode passar.
    List<Callable<Integer>> envios = new ArrayList<>();
    List<String> chaves = List.of("chave-pagamento-a-" + divida, "chave-pagamento-b-" + divida);
    for (String chave : chaves) {
      envios.add(
          () ->
              g.status(
                  post("/api/dividas/" + divida + "/pagamentos")
                      .header("Idempotency-Key", chave)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(corpo)));
    }
    var pool = Executors.newFixedThreadPool(2);
    var res = new ArrayList<Integer>();
    String chaveAceita = null;
    var futuros = pool.invokeAll(envios);
    for (int i = 0; i < futuros.size(); i++) {
      res.add(futuros.get(i).get());
      if (res.get(i) == 200) chaveAceita = chaves.get(i);
    }
    pool.shutdown();
    assertThat(res).as("status: %s", res).containsOnlyOnce(200);

    // Repetir a mesma chave (reenvio do navegador) não cria outro pagamento.
    int repetido =
        g.status(
            post("/api/dividas/" + divida + "/pagamentos")
                .header("Idempotency-Key", chaveAceita)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    assertThat(repetido).isEqualTo(200);
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM pagamentos WHERE divida_id=?", Long.class, divida))
        .isEqualTo(1L);
    assertThat(
            jdbc.queryForObject("SELECT valor_pendente FROM dividas WHERE id=?", BigDecimal.class, divida))
        .isEqualByComparingTo("0");
  }

  /** Contrato com o frontend (lib/core/api.dart): mesmo corpo e cabeçalhos que o Flutter envia. */
  @Test
  void contratoDoFrontendParaVendaEPagamento() throws Exception {
    long divida = dividaVencida("82392546006", new BigDecimal("30.00"));
    Long cliente = jdbc.queryForObject("SELECT cliente_id FROM dividas WHERE id=?", Long.class, divida);
    var g = new Navegador();
    g.login(GERENTE, SENHA_GERENTE);
    String venda =
        "{\"clienteId\":" + cliente
            + ",\"valorTotal\":21.0,\"itens\":[{\"descricaoProduto\":\"Arroz\",\"quantidade\":2,\"precoUnitario\":10.5}]}";
    // Sem a chave (como o frontend fazia até a correção da Fase 2) a API recusa.
    assertThat(g.status(post("/api/vendas").contentType(MediaType.APPLICATION_JSON).content(venda)))
        .isEqualTo(400);
    assertThat(
            g.status(
                post("/api/vendas")
                    .header("Idempotency-Key", "0123456789abcdef0123456789abcdef")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(venda)))
        .isIn(200, 201);
    assertThat(
            g.status(
                post("/api/dividas/" + divida + "/pagamentos")
                    .header("Idempotency-Key", "fedcba9876543210fedcba9876543210")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"valorPago\":10.0,\"metodoPagamento\":\"DINHEIRO\"}")))
        .isEqualTo(200);
  }
}
