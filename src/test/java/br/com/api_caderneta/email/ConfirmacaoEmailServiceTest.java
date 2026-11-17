package br.com.api_caderneta.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class ConfirmacaoEmailServiceTest {
  final ConfirmacoesEmail confirmacoes = mock(ConfirmacoesEmail.class);
  final EnviarEmail email = mock(EnviarEmail.class);
  final ConfirmacaoEmailService service =
      new ConfirmacaoEmailService(
          confirmacoes, email, mock(AuditoriaService.class), "https://exemplo.test");

  @BeforeEach
  void iniciar() {
    TransactionSynchronizationManager.initSynchronization();
  }

  @AfterEach
  void limpar() {
    TransactionSynchronizationManager.clearSynchronization();
  }

  @Test
  void armazenaHashEEnviaTokenSomenteAposCommit() {
    when(confirmacoes.situacao(1)).thenReturn(new SituacaoEmail(null, false));
    service.solicitar(1, "Loja@Exemplo.test");
    verifyNoInteractions(email);
    var hash = ArgumentCaptor.forClass(String.class);
    verify(confirmacoes)
        .salvarPendente(eq(1L), eq("loja@exemplo.test"), hash.capture(), any(), any());
    TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
    var corpo = ArgumentCaptor.forClass(String.class);
    verify(email).enviar(eq("loja@exemplo.test"), anyString(), corpo.capture());
    var token = java.util.regex.Pattern.compile("token=([a-f0-9]{64})").matcher(corpo.getValue());
    assertTrue(token.find());
    assertNotEquals(token.group(1), hash.getValue());
    assertEquals(ConfirmacaoEmailService.hash(token.group(1)), hash.getValue());
  }

  @Test
  void reenvioDentroDoIntervaloNaoAlteraContatoNemDisparaEnvio() {
    when(confirmacoes.ultimoEnvio(1)).thenReturn(LocalDateTime.now(ZoneOffset.UTC));
    assertThrows(ConflictException.class, () -> service.solicitar(1, "loja@exemplo.test"));
    verify(confirmacoes, never()).salvarPendente(anyLong(), anyString(), anyString(), any(), any());
    verifyNoInteractions(email);
  }

  @Test
  void tokenExpiradoOuConsumidoNaoConfirmaLoja() {
    when(confirmacoes.bloquearLoja(1)).thenReturn(true);
    when(confirmacoes.emailValido(eq(1L), anyString(), any())).thenReturn(Optional.empty());
    assertThrows(BusinessException.class, () -> service.confirmar(1, "a".repeat(64)));
    verify(confirmacoes, never()).confirmar(anyLong(), anyString());
    verify(confirmacoes, never()).remover(anyLong());
  }
}
