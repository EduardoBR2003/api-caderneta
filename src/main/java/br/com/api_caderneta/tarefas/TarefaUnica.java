package br.com.api_caderneta.tarefas;

import br.com.api_caderneta.services.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Executa uma tarefa e encerra o processo (Cloud Run Job, serviço "migracao" do compose ou agendador).
 *
 * <ul>
 *   <li>perfil {@code migracao}: o Flyway já migrou o banco durante a inicialização; só encerra.
 *   <li>perfil {@code tarefa-lembretes}: gera os lembretes de vencimento (idempotente) e encerra.
 * </ul>
 *
 * Código de saída 0 = sucesso; qualquer exceção na inicialização ou na tarefa encerra com erro, e a
 * plataforma registra a falha (e pode repetir).
 */
@Component
@Profile({"migracao", "tarefa-lembretes"})
@Order(Ordered.LOWEST_PRECEDENCE)
public class TarefaUnica implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(TarefaUnica.class);

  private final ConfigurableApplicationContext contexto;
  private final Environment env;
  private final NotificacaoService notificacoes;

  public TarefaUnica(
      ConfigurableApplicationContext contexto, Environment env, NotificacaoService notificacoes) {
    this.contexto = contexto;
    this.env = env;
    this.notificacoes = notificacoes;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (env.matchesProfiles("tarefa-lembretes")) {
      notificacoes.gerarNotificacoesDeVencimento();
    }
    if (env.matchesProfiles("migracao")) {
      log.info("Migrations aplicadas; tarefa concluída.");
    }
    if (!env.getProperty("caderneta.tarefa.manter-contexto", Boolean.class, false)) {
      System.exit(SpringApplication.exit(contexto, () -> 0));
    }
  }
}
