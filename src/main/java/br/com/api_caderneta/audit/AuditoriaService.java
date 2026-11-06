package br.com.api_caderneta.audit;

import br.com.api_caderneta.model.AuditoriaEvento;
import br.com.api_caderneta.repository.AuditoriaEventoRepository;
import br.com.api_caderneta.security.UsuarioAtual;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registra eventos na MESMA transação da operação de domínio (MANDATORY): se a operação for revertida, o
 * evento também. Quem chama é responsável por não passar segredos (senha, hash, código de convite, token).
 */
@Service
public class AuditoriaService {

    private final AuditoriaEventoRepository repository;
    private final ObjectMapper mapper;

    public AuditoriaService(AuditoriaEventoRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String acao, String recursoTipo, Long recursoId, Map<String, Object[]> alteracoes) {
        var ator = UsuarioAtual.get();
        registrarComo(ator.map(a -> a.getId()).orElse(null), ator.map(a -> a.getUsername()).orElse(null),
                ator.map(a -> a.getPerfil().name()).orElse(null), acao, recursoTipo, recursoId, alteracoes);
    }

    /** Para ações sem sessão (ex.: ativação por convite): o ator informado é o próprio titular da conta. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarComo(Long usuarioId, String atorLogin, String perfil, String acao, String recursoTipo,
                              Long recursoId, Map<String, Object[]> alteracoes) {
        repository.save(new AuditoriaEvento(Instant.now(), usuarioId, atorLogin, perfil, acao, recursoTipo,
                recursoId, "SUCESSO", json(alteracoes)));
    }

    /** {campo: {antes, depois}}; só inclui o que mudou. */
    public static Map<String, Object[]> mudanca(Object... paresCampoAntesDepois) {
        Map<String, Object[]> m = new LinkedHashMap<>();
        for (int i = 0; i + 2 < paresCampoAntesDepois.length; i += 3) {
            Object antes = paresCampoAntesDepois[i + 1], depois = paresCampoAntesDepois[i + 2];
            if (!java.util.Objects.equals(antes, depois)) {
                m.put((String) paresCampoAntesDepois[i], new Object[]{antes, depois});
            }
        }
        return m;
    }

    private String json(Map<String, Object[]> alteracoes) {
        if (alteracoes == null || alteracoes.isEmpty()) return null;
        Map<String, Object> out = new LinkedHashMap<>();
        alteracoes.forEach((k, v) -> {
            Map<String, Object> par = new LinkedHashMap<>();
            par.put("antes", v[0] == null ? null : v[0].toString());
            par.put("depois", v[1] == null ? null : v[1].toString());
            out.put(k, par);
        });
        try {
            return mapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
