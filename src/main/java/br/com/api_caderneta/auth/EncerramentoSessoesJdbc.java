package br.com.api_caderneta.auth;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class EncerramentoSessoesJdbc implements EncerramentoSessoes {
  private final FindByIndexNameSessionRepository<?> sessoes;

  public EncerramentoSessoesJdbc(FindByIndexNameSessionRepository<?> sessoes) {
    this.sessoes = sessoes;
  }

  public void aposCommit(String login) {
    Runnable encerrar =
        () -> {
          @SuppressWarnings("unchecked")
          var repo = (SessionRepository<?>) sessoes;
          sessoes.findByPrincipalName(login).keySet().forEach(repo::deleteById);
        };
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              encerrar.run();
            }
          });
    } else {
      encerrar.run();
    }
  }
}
