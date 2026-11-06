package br.com.api_caderneta.audit;

import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.historico.Intervalo;
import br.com.api_caderneta.model.AuditoriaEvento;
import br.com.api_caderneta.repository.AuditoriaEventoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;

/** Somente gerente (SecurityConfig). Leitura apenas; ordenação fixa ocorridoEm DESC, id DESC. */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    public record EventoDTO(Long id, Instant ocorridoEm, Ator ator, String acao, Recurso recurso,
                            String resultado, String alteracoes) {}
    public record Ator(Long usuarioId, String login, String perfil) {}
    public record Recurso(String tipo, Long id) {}

    private final AuditoriaEventoRepository repository;

    public AuditoriaController(AuditoriaEventoRepository repository) { this.repository = repository; }

    @GetMapping
    @Transactional(readOnly = true)
    public Pagina<EventoDTO> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) String inicio,
                                    @RequestParam(required = false) String fim,
                                    @RequestParam(required = false) String acao,
                                    @RequestParam(required = false) String recursoTipo) {
        if (page < 0) throw new BusinessException("page deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new BusinessException("size deve estar entre 1 e 100.");
        Intervalo intervalo = Intervalo.de(inicio, fim);
        Specification<AuditoriaEvento> spec = (root, q, cb) -> {
            var ps = new ArrayList<Predicate>();
            if (intervalo.inicio() != null) ps.add(cb.greaterThanOrEqualTo(root.get("ocorridoEm"), intervalo.inicio()));
            if (intervalo.fim() != null) ps.add(cb.lessThan(root.get("ocorridoEm"), intervalo.fim()));
            if (acao != null && !acao.isBlank()) ps.add(cb.equal(root.get("acao"), acao.trim()));
            if (recursoTipo != null && !recursoTipo.isBlank()) ps.add(cb.equal(root.get("recursoTipo"), recursoTipo.trim()));
            return cb.and(ps.toArray(Predicate[]::new));
        };
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("ocorridoEm"), Sort.Order.desc("id")));
        return Pagina.de(repository.findAll(spec, pageable).map(e -> new EventoDTO(e.getId(), e.getOcorridoEm(),
                new Ator(e.getUsuarioId(), e.getAtorLogin(), e.getPerfil()), e.getAcao(),
                new Recurso(e.getRecursoTipo(), e.getRecursoId()), e.getResultado(), e.getAlteracoes())));
    }
}
