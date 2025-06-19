package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    @Autowired
    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> create(@RequestBody @Valid ClienteRequestDTO cliente) {
        var createdCliente = service.createCliente(cliente);
        return new ResponseEntity<>(createdCliente, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getClienteById(id));
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAll() {
        return ResponseEntity.ok(service.getAllClientes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(@PathVariable("id") Long id, @RequestBody @Valid ClienteUpdateRequestDTO cliente) {
        return ResponseEntity.ok(service.updateCliente(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/limite-credito")
    public ResponseEntity<ClienteDTO> updateLimite(@PathVariable("id") Long id, @RequestBody @Valid LimiteCreditoRequestDTO dto) {
        return ResponseEntity.ok(service.updateLimiteCredito(id, dto));
    }

    @PatchMapping("/{id}/prazo-pagamento")
    public ResponseEntity<ClienteDTO> updatePrazo(@PathVariable("id") Long id, @RequestBody @Valid PrazoPagamentoRequestDTO dto) {
        return ResponseEntity.ok(service.updatePrazoPagamento(id, dto));
    }
}