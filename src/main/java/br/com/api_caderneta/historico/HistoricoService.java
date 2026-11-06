package br.com.api_caderneta.historico;

import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.model.Notificacao;
import br.com.api_caderneta.model.Pessoa;
import br.com.api_caderneta.model.enums.Perfil;
import br.com.api_caderneta.model.enums.TipoNotificacao;
import br.com.api_caderneta.security.UsuarioPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Histórico = eventos de {@code notificacoes} com destinatário. Filtro, contagem e paginação no banco;
 * o recorte do perfil entra na consulta ANTES da contagem e da paginação.
 */
@Service
public class HistoricoService {

    public record Destinatario(Long id, String nome) {}
    public record EventoDTO(Long id, TipoNotificacao tipo, String mensagem, Instant dataEnvio, Destinatario destinatario) {}

    /** Únicos tipos que o funcionário pode ver ou pedir. */
    public static final Set<TipoNotificacao> TIPOS_FUNCIONARIO = EnumSet.of(
            TipoNotificacao.CADASTRO_CLIENTE, TipoNotificacao.COMPRA_REALIZADA, TipoNotificacao.PAGAMENTO_RECEBIDO);

    private final EntityManager em;

    public HistoricoService(EntityManager em) { this.em = em; }

    /** Histórico geral (gerente e funcionário). */
    @Transactional(readOnly = true)
    public Pagina<EventoDTO> geral(UsuarioPrincipal usuario, int page, int size, String tipo, String inicio, String fim) {
        TipoNotificacao t = tipo(tipo);
        Set<TipoNotificacao> permitidos = usuario.getPerfil() == Perfil.FUNCIONARIO
                ? TIPOS_FUNCIONARIO : EnumSet.allOf(TipoNotificacao.class);
        if (t != null && !permitidos.contains(t)) {
            throw new AccessDeniedException("Tipo de evento não permitido para o perfil.");
        }
        return consultar(null, t == null ? permitidos : Set.of(t), page, size, Intervalo.de(inicio, fim));
    }

    /** Histórico do próprio cliente: o destinatário vem da sessão, nunca de parâmetro. */
    @Transactional(readOnly = true)
    public Pagina<EventoDTO> proprio(UsuarioPrincipal usuario, int page, int size, String tipo, String inicio, String fim) {
        TipoNotificacao t = tipo(tipo);
        return consultar(usuario.getPessoaId(), t == null ? EnumSet.allOf(TipoNotificacao.class) : Set.of(t),
                page, size, Intervalo.de(inicio, fim));
    }

    private static TipoNotificacao tipo(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return TipoNotificacao.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de evento inexistente: " + valor);
        }
    }

    private Pagina<EventoDTO> consultar(Long destinatarioId, Set<TipoNotificacao> tipos, int page, int size,
                                        Intervalo intervalo) {
        if (page < 0) throw new BusinessException("page deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new BusinessException("size deve estar entre 1 e 100.");

        CriteriaBuilder cb = em.getCriteriaBuilder();

        var contagem = cb.createQuery(Long.class);
        Root<Notificacao> rc = contagem.from(Notificacao.class);
        contagem.select(cb.count(rc)).where(filtros(cb, rc, destinatarioId, tipos, intervalo));
        long total = em.createQuery(contagem).getSingleResult();

        var q = cb.createTupleQuery();
        Root<Notificacao> r = q.from(Notificacao.class);
        var dest = r.join("destinatario");
        q.multiselect(r.get("idNotificacao").alias("id"), r.get("tipoNotificacao").alias("tipo"),
                        r.get("mensagem").alias("mensagem"), r.get("dataEnvio").alias("dataEnvio"),
                        dest.get("id").alias("destId"), dest.get("nome").alias("destNome"))
         .where(filtros(cb, r, destinatarioId, tipos, intervalo))
         .orderBy(cb.desc(r.get("dataEnvio")), cb.desc(r.get("idNotificacao")));
        List<Tuple> linhas = em.createQuery(q).setFirstResult(page * size).setMaxResults(size).getResultList();

        var itens = linhas.stream().map(t -> new EventoDTO(
                t.get("id", Long.class), t.get("tipo", TipoNotificacao.class), t.get("mensagem", String.class),
                t.get("dataEnvio", java.time.LocalDateTime.class).toInstant(ZoneOffset.UTC),
                new Destinatario(t.get("destId", Long.class), t.get("destNome", String.class)))).toList();

        int totalPages = (int) Math.ceil(total / (double) size);
        return new Pagina<>(itens, page, size, total, totalPages, page == 0, totalPages == 0 || page >= totalPages - 1);
    }

    private static Predicate filtros(CriteriaBuilder cb, Root<Notificacao> r, Long destinatarioId,
                                     Set<TipoNotificacao> tipos, Intervalo intervalo) {
        var ps = new ArrayList<Predicate>();
        ps.add(r.get("tipoNotificacao").in(tipos));
        if (destinatarioId != null) ps.add(cb.equal(r.<Pessoa>get("destinatario").get("id"), destinatarioId));
        if (intervalo.inicio() != null) ps.add(cb.greaterThanOrEqualTo(r.get("dataEnvio"), intervalo.inicioUtc()));
        if (intervalo.fim() != null) ps.add(cb.lessThan(r.get("dataEnvio"), intervalo.fimUtc()));
        return cb.and(ps.toArray(Predicate[]::new));
    }
}
