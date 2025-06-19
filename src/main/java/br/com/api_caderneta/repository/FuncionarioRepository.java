package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByLogin(String login);

    Boolean existsByCpf(String cpf);

    Boolean existsByLogin(String login);
}