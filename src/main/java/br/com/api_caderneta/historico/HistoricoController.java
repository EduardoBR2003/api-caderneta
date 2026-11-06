package br.com.api_caderneta.historico;

import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoricoController {

    private final HistoricoService service;

    public HistoricoController(HistoricoService service) { this.service = service; }

    /** Gerente (todos os tipos) e funcionário (3 tipos operacionais). */
    @GetMapping("/api/historico")
    public Pagina<HistoricoService.EventoDTO> geral(@AuthenticationPrincipal UsuarioPrincipal usuario,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo, @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fim) {
        return service.geral(usuario, page, size, tipo, inicio, fim);
    }

    /** Cliente: apenas eventos em que ele é o destinatário. */
    @GetMapping("/api/me/historico")
    public Pagina<HistoricoService.EventoDTO> proprio(@AuthenticationPrincipal UsuarioPrincipal usuario,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo, @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fim) {
        return service.proprio(usuario, page, size, tipo, inicio, fim);
    }
}
