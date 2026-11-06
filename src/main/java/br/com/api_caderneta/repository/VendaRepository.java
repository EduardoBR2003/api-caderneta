package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Venda;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendaRepository
    extends JpaRepository<Venda, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Venda> {
  @org.springframework.data.jpa.repository.Query(
      "select v.cliente.id from Venda v where v.id = :id")
  java.util.Optional<Long> clienteId(
      @org.springframework.data.repository.query.Param("id") Long id);

  org.springframework.data.domain.Page<Venda> findByFuncionarioId(
      Long id, org.springframework.data.domain.Pageable pagina);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select v from Venda v where v.id = :id")
  java.util.Optional<Venda> bloquear(
      @org.springframework.data.repository.query.Param("id") Long id);

  /**
   * Busca todas as vendas realizadas dentro de um intervalo de data e hora.
   *
   * @param dataInicio A data e hora de início do período.
   * @param dataFim A data e hora de fim do período.
   * @return Uma lista de vendas encontradas.
   */
  List<Venda> findAllByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

  Boolean existsByFuncionarioId(Long id);

  org.springframework.data.domain.Page<Venda> findByClienteId(
      Long clienteId, org.springframework.data.domain.Pageable pageable);
}
