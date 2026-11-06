package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "convites_cliente")
public class ConviteCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "codigo_hash", nullable = false, unique = true, length = 64)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usado_em")
    private Instant usadoEm;

    @Column(name = "criado_por_usuario_id", nullable = false)
    private Long criadoPorUsuarioId;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public ConviteCliente() {}

    public ConviteCliente(Long clienteId, String codigoHash, Instant expiraEm, Long criadoPor) {
        this.clienteId = clienteId;
        this.codigoHash = codigoHash;
        this.expiraEm = expiraEm;
        this.criadoPorUsuarioId = criadoPor;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getUsadoEm() { return usadoEm; }
    public void setUsadoEm(Instant usadoEm) { this.usadoEm = usadoEm; }
}
