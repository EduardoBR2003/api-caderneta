package br.com.api_caderneta.integracao;

import br.com.api_caderneta.Startup;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Banco descartável (Testcontainers) compartilhado pelos testes *IT. Padrão: MySQL 8.4. Para validar
 * compatibilidade com TiDB: {@code mvn verify -Dcaderneta.it.imagem-banco=pingcap/tidb:v8.5.7}.
 *
 * <p>Prepara o banco como em produção: usuários separados (no MySQL, pelo próprio
 * scripts/criar-usuarios-mysql.sh) e a tarefa "migracao". Nunca usa o banco/volume local do compose.
 */
public final class BancoMySql {

  public static final String IMAGEM = System.getProperty("caderneta.it.imagem-banco", "mysql:8.4");
  public static final boolean TIDB = IMAGEM.contains("tidb");
  public static final String BANCO = "api_caderneta_database";
  public static final String APP = "caderneta_app";
  public static final String MIG = "caderneta_migracao";
  // Senhas geradas a cada execução: nada fixo no repositório.
  public static final String SENHA_ROOT = "r" + UUID.randomUUID();
  public static final String SENHA_APP = "a" + UUID.randomUUID();
  public static final String SENHA_MIG = "m" + UUID.randomUUID();

  private static GenericContainer<?> container;
  private static boolean pronto;
  /** Linhas por tabela logo após a primeira migração (antes de qualquer teste gravar dados). */
  public static final Map<String, Long> CONTAGENS_POS_MIGRACAO = new HashMap<>();

  private BancoMySql() {}

  public static synchronized void iniciar() {
    if (pronto) return;
    try {
      if (TIDB) iniciarTidb();
      else iniciarMysql();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    migrar();
    try (var c = DriverManager.getConnection(url(), APP, SENHA_APP)) {
      for (String t :
          java.util.List.of("pessoas", "funcionarios", "usuarios", "vendas", "dividas", "notificacoes")) {
        try (var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + t)) {
          rs.next();
          CONTAGENS_POS_MIGRACAO.put(t, rs.getLong(1));
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    pronto = true;
  }

  private static void iniciarMysql() throws Exception {
    var mysql =
        new MySQLContainer(IMAGEM).withDatabaseName(BANCO).withUsername("root").withPassword(SENHA_ROOT);
    mysql.start();
    container = mysql;
    mysql.copyFileToContainer(
        MountableFile.forHostPath(Path.of("scripts/criar-usuarios-mysql.sh")), "/tmp/criar-usuarios.sh");
    var r =
        mysql.execInContainer(
            "env",
            "MYSQL_ROOT_PASSWORD=" + SENHA_ROOT,
            "MYSQL_DATABASE=" + BANCO,
            "CADERNETA_DB_PASSWORD=" + SENHA_APP,
            "CADERNETA_MIGRATION_PASSWORD=" + SENHA_MIG,
            "bash",
            "/tmp/criar-usuarios.sh");
    if (r.getExitCode() != 0) throw new IllegalStateException("script de usuários falhou: " + r.getStderr());
  }

  /** TiDB: sem cliente mysql na imagem; mesmas instruções do script, via JDBC como root. */
  private static void iniciarTidb() throws Exception {
    var tidb =
        new GenericContainer<>(IMAGEM)
            .withExposedPorts(4000)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(3)));
    tidb.start();
    container = tidb;
    String raiz = "jdbc:mysql://" + tidb.getHost() + ":" + tidb.getMappedPort(4000) + "/?serverTimezone=UTC";
    try (var c = DriverManager.getConnection(raiz, "root", "");
        var st = c.createStatement()) {
      st.execute(
          "CREATE DATABASE IF NOT EXISTS `" + BANCO + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
      st.execute("CREATE USER IF NOT EXISTS '" + APP + "'@'%' IDENTIFIED BY '" + SENHA_APP + "'");
      st.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON `" + BANCO + "`.* TO '" + APP + "'@'%'");
      st.execute("CREATE USER IF NOT EXISTS '" + MIG + "'@'%' IDENTIFIED BY '" + SENHA_MIG + "'");
      st.execute(
          "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON `"
              + BANCO + "`.* TO '" + MIG + "'@'%'");
    }
  }

  public static String url() {
    int porta = container.getMappedPort(TIDB ? 4000 : 3306);
    return "jdbc:mysql://" + container.getHost() + ":" + porta + "/" + BANCO + "?serverTimezone=UTC";
  }

  /** Roda a tarefa "migracao" exatamente como no Cloud Run Job. */
  public static void migrar() {
    Map<String, Object> props = new HashMap<>();
    props.put("CADERNETA_DB_URL", url());
    props.put("CADERNETA_MIGRATION_PASSWORD", SENHA_MIG);
    props.put("caderneta.tarefa.manter-contexto", "true");
    new SpringApplicationBuilder(Startup.class).profiles("migracao").properties(props).run().close();
  }
}
