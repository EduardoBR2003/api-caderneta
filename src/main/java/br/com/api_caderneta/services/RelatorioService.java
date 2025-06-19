// crie o arquivo em src/main/java/br/com/api_caderneta/services/
package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.RelatorioDividasDTO;
import br.com.api_caderneta.dto.RelatorioVendasDTO;
import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RelatorioService {

    private final VendaRepository vendaRepository;
    private final DividaRepository dividaRepository;
    private final DataMapper mapper;

    @Autowired
    public RelatorioService(VendaRepository vendaRepository, DividaRepository dividaRepository, DataMapper mapper) {
        this.vendaRepository = vendaRepository;
        this.dividaRepository = dividaRepository;
        this.mapper = mapper;
    }

    public RelatorioVendasDTO gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim) {
        var vendasNoPeriodo = vendaRepository.findAllByDataHoraBetween(dataInicio.atStartOfDay(), dataFim.atTime(LocalTime.MAX));

        var faturamentoTotal = vendasNoPeriodo.stream()
                .map(venda -> venda.getValorTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var relatorio = new RelatorioVendasDTO();
        relatorio.setDataInicioFiltro(dataInicio);
        relatorio.setDataFimFiltro(dataFim);
        relatorio.setTotalVendas(vendasNoPeriodo.size());
        relatorio.setFaturamentoTotal(faturamentoTotal);
        relatorio.setVendas(mapper.parseListObjects(vendasNoPeriodo, VendaDTO.class));

        return relatorio;
    }

    public RelatorioDividasDTO gerarRelatorioDividas(LocalDate dataInicio, LocalDate dataFim, StatusDivida status) {
        var dividasNoPeriodo = dividaRepository.findDividasByFiltro(dataInicio, dataFim, status);

        var valorTotalOriginal = dividasNoPeriodo.stream()
                .map(divida -> divida.getValorOriginal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var valorTotalPendente = dividasNoPeriodo.stream()
                .map(divida -> divida.getValorPendente())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var relatorio = new RelatorioDividasDTO();
        relatorio.setDataInicioFiltro(dataInicio);
        relatorio.setDataFimFiltro(dataFim);
        relatorio.setTotalDividas(dividasNoPeriodo.size());
        relatorio.setValorTotalOriginal(valorTotalOriginal);
        relatorio.setValorTotalPendente(valorTotalPendente);
        relatorio.setDividas(mapper.parseListObjects(dividasNoPeriodo, br.com.api_caderneta.dto.DividaDTO.class));

        return relatorio;
    }
}