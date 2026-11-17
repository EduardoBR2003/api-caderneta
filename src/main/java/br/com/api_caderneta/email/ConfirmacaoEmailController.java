package br.com.api_caderneta.email;

import br.com.api_caderneta.auth.Dtos.MensagemDTO;
import br.com.api_caderneta.lojas.LojaAtual;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.security.UsuarioAtual;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email")
public class ConfirmacaoEmailController {
  public record Confirmacao(@NotNull Long lojaId, @NotBlank String token) {}

  public record Endereco(@NotBlank @Email @Size(max = 100) String email) {}

  private final ConfirmacaoEmailService servico;

  public ConfirmacaoEmailController(ConfirmacaoEmailService servico) {
    this.servico = servico;
  }

  @GetMapping
  public SituacaoEmail situacao() {
    return servico.situacao(LojaAtual.exigida());
  }

  @PostMapping("/solicitar")
  public MensagemDTO solicitar(@Valid @RequestBody Endereco req) {
    if (UsuarioAtual.obrigatorio().getPerfil() != Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException(
          "Peça ao gerente para confirmar o e-mail da loja.");
    servico.solicitar(LojaAtual.exigida(), req.email());
    return new MensagemDTO(
        "Se o envio estiver disponível, você receberá o link. Confira sua caixa de entrada e spam;"
            + " se não chegar, tente reenviar após um minuto.");
  }

  @PostMapping("/confirmar")
  public MensagemDTO confirmar(@Valid @RequestBody Confirmacao req) {
    servico.confirmar(req.lojaId(), req.token());
    return new MensagemDTO("E-mail confirmado. Entre para usar a loja.");
  }
}
