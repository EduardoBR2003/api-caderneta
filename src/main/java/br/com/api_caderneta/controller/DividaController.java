package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.services.DividaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dividas")
public class DividaController {

    private final DividaService service;

    @Autowired
    public DividaController(DividaService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DividaDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getDividaById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<DividaDTO>> getByClienteId(@PathVariable("clienteId") Long clienteId) {
        return ResponseEntity.ok(service.getDividasByCliente(clienteId));
    }

    @PostMapping("/{dividaId}/pagamentos")
    public ResponseEntity<DividaDTO> registrarPagamento(@PathVariable("dividaId") Long dividaId, @RequestBody @Valid PagamentoRequestDTO pagamento) {
        var dividaAtualizada = service.registrarPagamento(dividaId, pagamento);
        return ResponseEntity.ok(dividaAtualizada);
    }
}