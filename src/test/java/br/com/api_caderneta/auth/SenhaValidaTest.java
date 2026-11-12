package br.com.api_caderneta.auth;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SenhaValidaTest {
  @Test
  void todosOsCadastrosValidamOsBytesAntesDoBCrypt() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      for (var senha : new String[] {"x".repeat(72), "á".repeat(36), "😀".repeat(18),
          "x".repeat(73), "á".repeat(40), "😀".repeat(19), "curta", " ".repeat(8)}) {
        boolean aceita = PoliticaSenha.aceita(senha);
        Object[] entradas = {
          new Dtos.CriarLojaRequest("Loja Teste", "Pessoa Teste", "52998224725", null, null, "teste", senha),
          new Dtos.FuncionarioContaRequest("Pessoa Teste", "52998224725", null, null, null, null, "Caixa", "teste", senha),
          new Dtos.RegistroClienteRequest("52998224725", "CODIGO", "teste", senha),
          new Dtos.RestaurarRequest(senha)
        };
        for (var entrada : entradas) {
          assertThat(validator.validate(entrada).isEmpty()).as(entrada.getClass().getSimpleName()).isEqualTo(aceita);
        }
        if (aceita) {
          var encoder = new BCryptPasswordEncoder(4);
          assertThat(encoder.matches(senha, encoder.encode(senha))).isTrue();
        }
      }
      assertThat(validator.validate(new Dtos.RestaurarRequest(null))).isNotEmpty();
    }
  }
}
