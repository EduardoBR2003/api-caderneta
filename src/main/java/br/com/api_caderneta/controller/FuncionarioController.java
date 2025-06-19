package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.services.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService service;

    @Autowired
    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FuncionarioDTO> create(@RequestBody @Valid FuncionarioRequestDTO funcionario) {
        return new ResponseEntity<>(service.createFuncionario(funcionario), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFuncionarioById(id));
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> getAll() {
        return ResponseEntity.ok(service.getAllFuncionarios());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> update(@PathVariable Long id, @RequestBody @Valid FuncionarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(service.updateFuncionario(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteFuncionario(id);
        return ResponseEntity.noContent().build();
    }
}