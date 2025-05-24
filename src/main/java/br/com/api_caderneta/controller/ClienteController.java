package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.ClienteDTO;
import br.com.api_caderneta.dto.ClienteRequestDTO;
import br.com.api_caderneta.dto.ClienteUpdateRequestDTO;
import br.com.api_caderneta.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes") // Alterado para "/clientes" (plural, mais comum em REST)
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteDTO> findById(@PathVariable Long id) {
        ClienteDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteDTO> create(@RequestBody ClienteRequestDTO clienteRequestDTO) { // [cite: 1]
        ClienteDTO createdClienteDTO = service.create(clienteRequestDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdClienteDTO.getIdPessoa()).toUri();
        return ResponseEntity.created(uri).body(createdClienteDTO);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Adicionado {id} ao Path
    @PutMapping(value = "/{id}", // [cite: 1]
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ClienteDTO> update(@PathVariable Long id, @RequestBody ClienteUpdateRequestDTO clienteUpdateRequestDTO) { // [cite: 1]
        ClienteDTO updatedDto = service.update(id, clienteUpdateRequestDTO);
        return ResponseEntity.ok().body(updatedDto);
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ClienteDTO>> findAll() {
        List<ClienteDTO> listDto = service.findAll();
        return ResponseEntity.ok().body(listDto);
    }
}