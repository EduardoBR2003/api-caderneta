package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.RelatorioDividasDTO;
import br.com.api_caderneta.dto.RelatorioVendasDTO;
import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RelatorioService {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioService.class);
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
        logger.info("Gerando relatório de vendas de {} a {}", dataInicio, dataFim);
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

        logger.info("Relatório de vendas gerado com {} vendas e faturamento total de R${}", relatorio.getTotalVendas(), relatorio.getFaturamentoTotal());
        return relatorio;
    }

    public RelatorioDividasDTO gerarRelatorioDividas(LocalDate dataInicio, LocalDate dataFim, StatusDivida status) {
        logger.info("Gerando relatório de dívidas de {} a {} com status {}", dataInicio, dataFim, status);
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
        relatorio.setDividas(mapper.parseListObjects(dividasNoPeriodo, DividaDTO.class));

        logger.info("Relatório de dívidas gerado com {} dívidas. Valor original total: R${}, Valor pendente total: R${}",
                relatorio.getTotalDividas(), relatorio.getValorTotalOriginal(), relatorio.getValorTotalPendente());
        return relatorio;
    }
}