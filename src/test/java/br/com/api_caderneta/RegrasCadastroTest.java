package br.com.api_caderneta;

import static org.assertj.core.api.Assertions.*;
import br.com.api_caderneta.auth.PoliticaSenha;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.model.Cliente;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class RegrasCadastroTest {
  @Test
  void telefoneOpcionalTemMesmoFormatoDoFormulario() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var v = factory.getValidator();
      var dto = new ClienteRequestDTO(); dto.setNome("Cliente Teste"); dto.setCpf("52998224725");
      for (String telefone : new String[]{"(64) 99999-1234", "6433331234", "", null}) {
        dto.setTelefone(telefone); assertThat(v.validate(dto)).isEmpty();
      }
      for (String telefone : new String[]{"abc", "123", "00999991234", "64abc999991234"}) {
        dto.setTelefone(telefone); assertThat(v.validate(dto)).isNotEmpty();
      }
    }
  }
  @Test
  void estadoInativoBloqueiaEscritaEAReativacaoPreservaCredito() {
    var c = new Cliente(); c.setLimiteCredito(new java.math.BigDecimal("120.00"));
    c.setAtivo(false);
    assertThatThrownBy(c::exigirAtivo).isInstanceOf(br.com.api_caderneta.exceptions.ConflictException.class);
    c.setAtivo(true); assertThatCode(c::exigirAtivo).doesNotThrowAnyException();
    assertThat(c.getLimiteCredito()).isEqualByComparingTo("120");
  }
  @Test
  void senhaComumRecusadaSemExigirComposicaoArbitraria() {
    assertThat(PoliticaSenha.aceita("12345678")).isFalse();
    assertThat(PoliticaSenha.aceita("PASSWORD123")).isFalse();
    assertThat(PoliticaSenha.aceita("minha frase particular longa")).isTrue();
    assertThat(PoliticaSenha.aceita("á".repeat(36))).isTrue();
    assertThat(PoliticaSenha.aceita("á".repeat(37))).isFalse();
  }
}
