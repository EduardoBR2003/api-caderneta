package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.services.VendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService service;

    @Autowired
    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VendaDTO> create(@RequestBody @Valid VendaRequestDTO venda) {
        var createdVenda = service.createVenda(venda);
        return new ResponseEntity<>(createdVenda, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getVendaById(id));
    }
}