package br.com.api_caderneta.dto;

import br.com.api_caderneta.model.enums.StatusDivida;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class DividaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private BigDecimal valorOriginal;
    private BigDecimal valorPendente;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private StatusDivida statusDivida;
    private Long clienteId;
    private Long vendaId;
    private List<PagamentoDTO> pagamentos;

    public BigDecimal getValorPago() {
        if (valorOriginal == null || valorPendente == null) {
            return BigDecimal.ZERO;
        }
        return valorOriginal.subtract(valorPendente);
    }

    public DividaDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public void setValorOriginal(BigDecimal valorOriginal) { this.valorOriginal = valorOriginal; }
    public BigDecimal getValorPendente() { return valorPendente; }
    public void setValorPendente(BigDecimal valorPendente) { this.valorPendente = valorPendente; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public StatusDivida getStatusDivida() { return statusDivida; }
    public void setStatusDivida(StatusDivida statusDivida) { this.statusDivida = statusDivida; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getVendaId() { return vendaId; }
    public void setVendaId(Long vendaId) { this.vendaId = vendaId; }
    public List<PagamentoDTO> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoDTO> pagamentos) { this.pagamentos = pagamentos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DividaDTO dividaDTO = (DividaDTO) o;
        return Objects.equals(id, dividaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}