package br.com.api_caderneta.model;

import br.com.api_caderneta.model.enums.TipoNotificacao;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notificacoes")
public class Notificacao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacao;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacao tipoNotificacao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Pessoa destinatario;

    public Notificacao() {
        this.dataEnvio = LocalDateTime.now(); // Default para data/hora atual
    }

    // Getters e Setters
    public Long getIdNotificacao() {
        return idNotificacao;
    }

    public void setIdNotificacao(Long idNotificacao) {
        this.idNotificacao = idNotificacao;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public TipoNotificacao getTipoNotificacao() {
        return tipoNotificacao;
    }

    public void setTipoNotificacao(TipoNotificacao tipoNotificacao) {
        this.tipoNotificacao = tipoNotificacao;
    }

    public Pessoa getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Pessoa destinatario) {
        this.destinatario = destinatario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notificacao that = (Notificacao) o;
        return Objects.equals(idNotificacao, that.idNotificacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotificacao);
    }

    @Override
    public String toString() {
        return "Notificacao{" +
                "idNotificacao=" + idNotificacao +
                ", dataEnvio=" + dataEnvio +
                ", tipoNotificacao=" + tipoNotificacao +
                ", destinatarioId=" + (destinatario != null ? destinatario.getId() : null) +
                '}';
    }
}