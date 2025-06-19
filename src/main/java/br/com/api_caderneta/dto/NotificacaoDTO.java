package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.TipoNotificacao;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class NotificacaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String mensagem;
    private LocalDateTime dataEnvio;
    private TipoNotificacao tipoNotificacao;
    private Long destinatarioId;

    public NotificacaoDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
    public TipoNotificacao getTipoNotificacao() { return tipoNotificacao; }
    public void setTipoNotificacao(TipoNotificacao tipoNotificacao) { this.tipoNotificacao = tipoNotificacao; }
    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificacaoDTO that = (NotificacaoDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}