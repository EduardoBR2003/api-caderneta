package br.com.api_caderneta.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioDividasDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate dataInicioFiltro;
    private LocalDate dataFimFiltro;
    private int totalDividas;
    private BigDecimal valorTotalOriginal;
    private BigDecimal valorTotalPendente;
    private List<DividaDTO> dividas;

    public RelatorioDividasDTO() {}

    // Getters e Setters
    public LocalDate getDataInicioFiltro() { return dataInicioFiltro; }
    public void setDataInicioFiltro(LocalDate dataInicioFiltro) { this.dataInicioFiltro = dataInicioFiltro; }
    public LocalDate getDataFimFiltro() { return dataFimFiltro; }
    public void setDataFimFiltro(LocalDate dataFimFiltro) { this.dataFimFiltro = dataFimFiltro; }
    public int getTotalDividas() { return totalDividas; }
    public void setTotalDividas(int totalDividas) { this.totalDividas = totalDividas; }
    public BigDecimal getValorTotalOriginal() { return valorTotalOriginal; }
    public void setValorTotalOriginal(BigDecimal valorTotalOriginal) { this.valorTotalOriginal = valorTotalOriginal; }
    public BigDecimal getValorTotalPendente() { return valorTotalPendente; }
    public void setValorTotalPendente(BigDecimal valorTotalPendente) { this.valorTotalPendente = valorTotalPendente; }
    public List<DividaDTO> getDividas() { return dividas; }
    public void setDividas(List<DividaDTO> dividas) { this.dividas = dividas; }
}