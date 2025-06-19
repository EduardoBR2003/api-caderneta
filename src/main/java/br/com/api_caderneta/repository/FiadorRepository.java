package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Fiador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiadorRepository extends JpaRepository<Fiador, Long> {

    boolean existsByCpf(String cpf);
}