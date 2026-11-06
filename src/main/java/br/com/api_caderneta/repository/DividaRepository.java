package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Divida;
import br.com.api_caderneta.model.enums.StatusDivida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividaRepository extends JpaRepository<Divida, Long> {

    /**
     * Busca uma lista de dívidas associadas a um ID de cliente específico.
     * A convenção "ClienteIdPessoa" instrui o Spring Data a procurar pelo
     * ID na entidade aninhada 'cliente' (que é do tipo Pessoa).
     *
     * @param clienteId O ID do cliente.
     * @return Uma lista de dívidas do cliente especificado.
     */
    List<Divida> findByClienteId(Long clienteId);

    org.springframework.data.domain.Page<Divida> findByClienteId(Long clienteId, org.springframework.data.domain.Pageable pageable);

    /**
     * Busca dívidas com base em um intervalo de data de emissão e, opcionalmente, por status.
     * Se o status for nulo, ele não será considerado no filtro.
     *
     * @param dataInicio A data de início do período.
     * @param dataFim A data de fim do período.
     * @param status O status da dívida (pode ser nulo).
     * @return Uma lista de dívidas que correspondem aos filtros.
     */
    @Query("SELECT d FROM Divida d WHERE d.dataEmissao BETWEEN :dataInicio AND :dataFim AND (:status IS NULL OR d.statusDivida = :status)")
    List<Divida> findDividasByFiltro(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("status") StatusDivida status
    );

    List<Divida> findByStatusDividaAndDataVencimentoBefore(StatusDivida status, LocalDate data);

    /** Dívidas com saldo (abertas, parciais ou vencidas) que vencem antes do limite informado. */
    @Query("SELECT d FROM Divida d WHERE d.statusDivida IN :status AND d.dataVencimento < :limite")
    List<Divida> findComSaldoVencendoAntes(@Param("status") java.util.Collection<StatusDivida> status,
                                           @Param("limite") LocalDate limite);

    /** Saldo devedor atual do cliente: base da validação do limite de crédito. */
    @Query("SELECT COALESCE(SUM(d.valorPendente), 0) FROM Divida d "
            + "WHERE d.cliente.id = :clienteId AND d.statusDivida IN :status")
    java.math.BigDecimal somaPendenteDoCliente(@Param("clienteId") Long clienteId,
                                               @Param("status") java.util.Collection<StatusDivida> status);

    /** Trava a linha para que pagamentos simultâneos na mesma dívida sejam serializados. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Divida d WHERE d.id = :id")
    java.util.Optional<Divida> findByIdParaAtualizar(@Param("id") Long id);


}