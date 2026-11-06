package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Fiador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiadorRepository extends JpaRepository<Fiador, Long> {
  @org.springframework.data.jpa.repository.Query(
      "select f from Fiador f where replace(replace(replace(f.cpf,'.',''),'-',''),' ','')=:cpf")
  java.util.Optional<Fiador> porCpf(
      @org.springframework.data.repository.query.Param("cpf") String cpf);

  boolean existsByCpf(String cpf);
}
