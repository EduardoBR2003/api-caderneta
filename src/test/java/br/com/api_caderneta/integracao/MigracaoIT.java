package br.com.api_caderneta.integracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Instalação nova: só a base B9 (sem V1-V8 nem dados de demonstração); banco existente em V8 segue para
 * V9 sem usar a base; privilégios mínimos.
 */
class MigracaoIT {

  @BeforeAll
  static void subir() {
    BancoMySql.iniciar();
  }

  private static List<String> historico() throws SQLException {
    try (var c = DriverManager.getConnection(BancoMySql.url(), BancoMySql.MIG, BancoMySql.SENHA_MIG);
        var rs =
            c.createStatement()
                .executeQuery(
                    "SELECT version, type, success FROM flyway_schema_history ORDER BY installed_rank")) {
      var l = new ArrayList<String>();
      while (rs.next()) l.add(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getBoolean(3));
      return l;
    }
  }

  @Test
  void bancoNovoUsaSoABaseB9() throws Exception {
    var h = historico();
    System.out.println("flyway_schema_history = " + h);
    assertThat(h).containsExactly("9|SQL_BASELINE|true");
  }

  /**
   * Banco legado em V8 (instalado por V1..V8, antes das bases) continua por V9; a base B9 é ignorada
   * porque o banco já tem histórico. Em container próprio: a V1 legada faz "USE" no nome fixo do banco.
   */
  @Test
  void bancoExistenteEmV8SegueParaV9() throws Exception {
    org.junit.jupiter.api.Assumptions.assumeFalse(BancoMySql.TIDB, "V8 não roda no TiDB (motivo da B9)");
    try (var legado = new org.testcontainers.mysql.MySQLContainer(BancoMySql.IMAGEM)
        .withDatabaseName(BancoMySql.BANCO).withUsername("root").withPassword("legado")) {
      legado.start();
      String url = legado.getJdbcUrl();
      // Prefixo inexistente desliga as bases: reproduz a instalação antiga, migration por migration.
      org.flywaydb.core.Flyway.configure().dataSource(url, "root", "legado").target("8")
          .configuration(java.util.Map.of("flyway.baselineMigrationPrefix", "SEM_BASE"))
          .load().migrate();
      var r = org.flywaydb.core.Flyway.configure().dataSource(url, "root", "legado").load().migrate();
      assertThat(r.migrationsExecuted).isEqualTo(1);
      try (var c = DriverManager.getConnection(url, "root", "legado");
          var rs = c.createStatement().executeQuery(
              "SELECT version, type FROM flyway_schema_history ORDER BY installed_rank")) {
        var l = new ArrayList<String>();
        while (rs.next()) l.add(rs.getString(1) + "|" + rs.getString(2));
        System.out.println("historico legado = " + l);
        assertThat(l).containsExactly(
            "1|SQL", "2|SQL", "3|SQL", "4|SQL", "5|SQL", "6|SQL", "7|SQL", "8|SQL", "9|SQL");
      }
    }
  }

  @Test
  void bancoNovoNaoTemDadosDeDemonstracao() {
    assertThat(BancoMySql.CONTAGENS_POS_MIGRACAO).hasSize(6).allSatisfy((t, n) -> assertThat(n).as(t).isZero());
  }

  @Test
  void usuarioDaAplicacaoNaoTemDdl() throws Exception {
    try (var c = DriverManager.getConnection(BancoMySql.url(), BancoMySql.APP, BancoMySql.SENHA_APP)) {
      c.createStatement().executeQuery("SELECT 1 FROM pessoas LIMIT 1").close();
      assertThatThrownBy(() -> c.createStatement().execute("CREATE TABLE intrusa (id INT)"))
          .isInstanceOf(SQLException.class);
      assertThatThrownBy(() -> c.createStatement().execute("DROP TABLE pessoas"))
          .isInstanceOf(SQLException.class);
    }
  }

  @Test
  void migracaoEhIdempotente() throws Exception {
    var antes = historico();
    BancoMySql.migrar();
    assertThat(historico()).isEqualTo(antes);
  }
}
