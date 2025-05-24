package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.TipoNotificacao; // Corrigido para o pacote correto dos enums
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class NotificacaoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idNotificacao;
    private LocalDateTime dataEnvio;
    private String mensagem;
    private TipoNotificacao tipoNotificacao;
    private Long destinatarioId; // ID da Pessoa (Cliente, Fiador, etc.)

    public NotificacaoDTO() {
    }

    public NotificacaoDTO(Long idNotificacao, LocalDateTime dataEnvio, String mensagem, TipoNotificacao tipoNotificacao, Long destinatarioId) {
        this.idNotificacao = idNotificacao;
        this.dataEnvio = dataEnvio;
        this.mensagem = mensagem;
        this.tipoNotificacao = tipoNotificacao;
        this.destinatarioId = destinatarioId;
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

    public Long getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(Long destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificacaoDTO that = (NotificacaoDTO) o;
        return Objects.equals(idNotificacao, that.idNotificacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotificacao);
    }

    @Override
    public String toString() {
        return "NotificacaoDTO{" +
                "idNotificacao=" + idNotificacao +
                ", dataEnvio=" + dataEnvio +
                ", tipoNotificacao=" + tipoNotificacao +
                ", destinatarioId=" + destinatarioId +
                '}';
    }
}