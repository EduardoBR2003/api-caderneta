package br.com.api_caderneta.domain.model;

import br.com.api_caderneta.domain.enums.TipoNotificacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idNotificacao;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Lob // Para mensagens potencialmente longas
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacao tipoNotificacao;

    // Uma notificação é enviada para uma Pessoa (Cliente, Fiador, etc.)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Pessoa destinatario;
}