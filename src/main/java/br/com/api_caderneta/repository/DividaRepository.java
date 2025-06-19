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
}