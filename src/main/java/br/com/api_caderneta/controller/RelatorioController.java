// crie o arquivo em src/main/java/br/com/api_caderneta/controller/
package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.RelatorioDividasDTO;
import br.com.api_caderneta.dto.RelatorioVendasDTO;
import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    @Autowired
    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/vendas")
    public ResponseEntity<RelatorioVendasDTO> getRelatorioVendas(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        return ResponseEntity.ok(service.gerarRelatorioVendas(dataInicio, dataFim));
    }

    @GetMapping("/dividas")
    public ResponseEntity<RelatorioDividasDTO> getRelatorioDividas(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "status", required = false) StatusDivida status) {

        return ResponseEntity.ok(service.gerarRelatorioDividas(dataInicio, dataFim, status));
    }
}