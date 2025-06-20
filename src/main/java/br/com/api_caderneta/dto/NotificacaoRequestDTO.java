package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.TipoNotificacao;

public class NotificacaoRequestDTO {

    private Long clienteId;
    private String mensagem;
    private TipoNotificacao tipoNotificacao;

    public NotificacaoRequestDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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
}