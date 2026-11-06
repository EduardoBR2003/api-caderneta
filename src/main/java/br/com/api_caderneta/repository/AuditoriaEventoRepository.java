package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.AuditoriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Somente leitura e inserção pela aplicação: nenhum endpoint edita ou exclui auditoria. */
public interface AuditoriaEventoRepository
        extends JpaRepository<AuditoriaEvento, Long>, JpaSpecificationExecutor<AuditoriaEvento> {
}
