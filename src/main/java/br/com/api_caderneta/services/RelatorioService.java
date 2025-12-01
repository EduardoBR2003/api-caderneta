package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Divida;
import br.com.api_caderneta.model.Pagamento;
import br.com.api_caderneta.model.Venda;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.PagamentoRepository;
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
    private final PagamentoRepository pagamentoRepository;
    private final DataMapper mapper;

    @Autowired
    public RelatorioService(VendaRepository vendaRepository,
                            DividaRepository dividaRepository,
                            PagamentoRepository pagamentoRepository,
                            DataMapper mapper) {
        this.vendaRepository = vendaRepository;
        this.dividaRepository = dividaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.mapper = mapper;
    }

    public RelatorioVendasDTO gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim) {
        List<Venda> vendas = vendaRepository.findAllByDataHoraBetween(
                dataInicio.atStartOfDay(),
                dataFim.atTime(LocalTime.MAX));

        BigDecimal total = vendas.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RelatorioVendasDTO dto = new RelatorioVendasDTO();
        dto.setTotalVendas(vendas.size());
        dto.setFaturamentoTotal(total);
        dto.setVendas(mapper.parseListObjects(vendas, VendaDTO.class));
        return dto;
    }

    public RelatorioDividasDTO gerarRelatorioDividas(LocalDate dataInicio, LocalDate dataFim, StatusDivida status) {
        List<Divida> dividas = dividaRepository.findDividasByFiltro(dataInicio, dataFim, status);

        BigDecimal totalPendente = dividas.stream()
                .map(Divida::getValorPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RelatorioDividasDTO dto = new RelatorioDividasDTO();
        dto.setValorTotalPendente(totalPendente);
        dto.setDividas(mapper.parseListObjects(dividas, DividaDTO.class));
        return dto;
    }

    // Método para o gráfico de Pagamentos
    public List<PagamentoDTO> gerarRelatorioPagamentos(LocalDate inicio, LocalDate fim) {
        List<Pagamento> pagamentos = pagamentoRepository.findAllByDataPagamentoBetween(inicio, fim);
        return mapper.parseListObjects(pagamentos, PagamentoDTO.class);
    }
}