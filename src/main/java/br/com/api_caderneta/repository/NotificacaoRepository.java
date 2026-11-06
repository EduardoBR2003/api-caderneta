package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatarioId(Long clienteId);

    /** Evita repetir o mesmo lembrete a cada execução da rotina diária. */
    boolean existsByDestinatarioIdAndTipoNotificacaoAndMensagem(
            Long destinatarioId, br.com.api_caderneta.model.enums.TipoNotificacao tipo, String mensagem);
}