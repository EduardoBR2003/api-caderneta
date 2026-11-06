package br.com.api_caderneta.auth;

import br.com.api_caderneta.auth.Dtos.*;
import br.com.api_caderneta.repository.UsuarioRepository;
import br.com.api_caderneta.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContaController {

    private final ContaService contas;

    public ContaController(ContaService contas) { this.contas = contas; }

    @PostMapping("/api/clientes/{id}/convite")
    public ResponseEntity<ConviteDTO> convite(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal autor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contas.gerarConvite(id, autor));
    }

    @GetMapping("/api/usuarios")
    public Pagina<UsuarioRepository.UsuarioResumo> listar(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return contas.listar(page, size);
    }

    @PatchMapping("/api/usuarios/{id}/ativo")
    public ResponseEntity<Void> ativo(@PathVariable Long id, @Valid @RequestBody AtivoRequest req) {
        contas.alterarAtivo(id, req.ativo());
        return ResponseEntity.noContent().build();
    }
}
