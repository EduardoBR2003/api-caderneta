package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    /**
     * Busca todas as vendas realizadas dentro de um intervalo de data e hora.
     * @param dataInicio A data e hora de início do período.
     * @param dataFim A data e hora de fim do período.
     * @return Uma lista de vendas encontradas.
     */
    List<Venda> findAllByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);
}