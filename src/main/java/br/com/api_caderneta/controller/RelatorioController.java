package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.PagamentoDTO;
import br.com.api_caderneta.dto.RelatorioDividasDTO;
import br.com.api_caderneta.dto.RelatorioVendasDTO;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.services.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios gerenciais.")
public class RelatorioController {

    private final RelatorioService service;

    @Autowired
    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/pagamentos")
    @Operation(summary = "Relatório de Pagamentos para Gráficos")
    public ResponseEntity<List<PagamentoDTO>> getRelatorioPagamentos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarRelatorioPagamentos(inicio, fim));
    }

    @Operation(summary = "Gerar relatório de vendas a prazo", description = "Gera um relatório consolidado de todas as vendas a prazo realizadas em um determinado período.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Relatório de vendas gerado com sucesso", content = @Content(schema = @Schema(implementation = RelatorioVendasDTO.class))))
    @GetMapping("/vendas-a-prazo")
    public ResponseEntity<RelatorioVendasDTO> getRelatorioVendas(
            @Parameter(description = "Data de início do período (formato AAAA-MM-DD)", required = true, example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do período (formato AAAA-MM-DD)", required = true, example = "2025-06-20")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(service.gerarRelatorioVendas(dataInicio, dataFim));
    }

    @Operation(summary = "Gerar relatório de dívidas", description = "Gera um relatório com as dívidas de um período, opcionalmente filtradas por status.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Relatório de dívidas gerado com sucesso", content = @Content(schema = @Schema(implementation = RelatorioDividasDTO.class))))
    @GetMapping("/debitos-pendentes")
    public ResponseEntity<RelatorioDividasDTO> getRelatorioDividas(
            @Parameter(description = "Data de início do período (formato AAAA-MM-DD)", required = true, example = "2025-01-01")
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data de fim do período (formato AAAA-MM-DD)", required = true, example = "2025-06-30")
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Filtra as dívidas por status (opcional). Valores possíveis: PAGA, PENDENTE, ATRASADA, CANCELADA")
            @RequestParam(value = "status", required = false) StatusDivida status) {
        return ResponseEntity.ok(service.gerarRelatorioDividas(dataInicio, dataFim, status));
    }

}