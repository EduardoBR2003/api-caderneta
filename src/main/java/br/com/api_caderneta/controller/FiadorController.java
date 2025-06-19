package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FiadorDTO;
import br.com.api_caderneta.dto.FiadorRequestDTO;
import br.com.api_caderneta.services.FiadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/fiadores")
public class FiadorController {

    private final FiadorService service;

    @Autowired
    public FiadorController(FiadorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FiadorDTO>> getAllFiadores() {
        return ResponseEntity.ok(service.getAllFiadores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiadorDTO> getFiadorById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFiadorById(id));
    }

    @PostMapping
    public ResponseEntity<FiadorDTO> createFiador(@RequestBody FiadorRequestDTO dto) {
        var createdFiador = service.createFiador(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdFiador.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdFiador);
    }

    @PostMapping("/associar/cliente/{clienteId}/fiador/{fiadorId}")
    public ResponseEntity<Void> associarFiador(@PathVariable Long clienteId, @PathVariable Long fiadorId) {
        service.associarFiador(clienteId, fiadorId);
        return ResponseEntity.noContent().build();
    }
}