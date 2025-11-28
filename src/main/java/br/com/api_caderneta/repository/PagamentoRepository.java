package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findAllByDataPagamentoBetween(LocalDate inicio, LocalDate fim);
}