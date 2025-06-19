package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioVendasDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private int totalVendas;
    private BigDecimal faturamentoTotal;
    private List<VendaDTO> vendas;

    public RelatorioVendasDTO() {}

    // Getters e Setters
    public LocalDate getDataInicioFiltro() { return dataInicioFiltro; }
    public void setDataInicioFiltro(LocalDate dataInicioFiltro) { this.dataInicioFiltro = dataInicioFiltro; }
    public LocalDate getDataFimFiltro() { return dataFimFiltro; }
    public void setDataFimFiltro(LocalDate dataFimFiltro) { this.dataFimFiltro = dataFimFiltro; }
    public int getTotalVendas() { return totalVendas; }
    public void setTotalVendas(int totalVendas) { this.totalVendas = totalVendas; }
    public BigDecimal getFaturamentoTotal() { return faturamentoTotal; }
    public void setFaturamentoTotal(BigDecimal faturamentoTotal) { this.faturamentoTotal = faturamentoTotal; }
    public List<VendaDTO> getVendas() { return vendas; }
    public void setVendas(List<VendaDTO> vendas) { this.vendas = vendas; }
}