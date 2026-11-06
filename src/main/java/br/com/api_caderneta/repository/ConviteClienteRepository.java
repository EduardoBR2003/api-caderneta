package br.com.api_caderneta.repository;

import br.com.api_caderneta.model.ConviteCliente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConviteClienteRepository extends JpaRepository<ConviteCliente, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ConviteCliente> findByCodigoHash(String codigoHash);
}
