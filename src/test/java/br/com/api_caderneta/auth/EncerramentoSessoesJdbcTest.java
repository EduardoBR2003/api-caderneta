package br.com.api_caderneta.auth;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class EncerramentoSessoesJdbcTest {
  @Test
  void encerramentoAguardaCommitEIgnoraRollback() {
    var sessoes = mock(FindByIndexNameSessionRepository.class);
    var encerramento = new EncerramentoSessoesJdbc(sessoes);
    TransactionSynchronizationManager.initSynchronization();
    try {
      encerramento.aposCommit("gerente");
      verifyNoInteractions(sessoes);
      TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
      verify(sessoes).findByPrincipalName("gerente");
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
    reset(sessoes);
    TransactionSynchronizationManager.initSynchronization();
    try {
      encerramento.aposCommit("gerente");
      TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(1));
      verifyNoInteractions(sessoes);
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }
}
