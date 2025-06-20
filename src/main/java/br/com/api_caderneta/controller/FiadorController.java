package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FiadorDTO;
import br.com.api_caderneta.dto.FiadorRequestDTO;
import br.com.api_caderneta.services.FiadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/fiadores")
@Tag(name = "Fiadores", description = "Endpoints para o gerenciamento de fiadores")
public class FiadorController {

    private final FiadorService service;

    @Autowired
    public FiadorController(FiadorService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os fiadores", description = "Retorna uma lista com todos os fiadores cadastrados no sistema.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de fiadores retornada", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FiadorDTO.class)))))
    @GetMapping
    public ResponseEntity<List<FiadorDTO>> getAllFiadores() {
        return ResponseEntity.ok(service.getAllFiadores());
    }

    @Operation(summary = "Buscar fiador por ID", description = "Retorna os detalhes de um fiador específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fiador encontrado", content = @Content(schema = @Schema(implementation = FiadorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Fiador não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FiadorDTO> getFiadorById(@Parameter(description = "ID do fiador a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(service.getFiadorById(id));
    }

    @Operation(summary = "Cadastrar novo fiador", description = "Cria um novo fiador no sistema, sem vinculá-lo a um cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fiador cadastrado com sucesso", content = @Content(schema = @Schema(implementation = FiadorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FiadorDTO> createFiador(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do fiador para cadastro") @RequestBody FiadorRequestDTO dto) {
        var createdFiador = service.createFiador(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdFiador.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdFiador);
    }

    @Operation(summary = "Associar fiador a um cliente", description = "Cria o vínculo entre um cliente e um fiador, ambos já existentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Associação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou Fiador não encontrado", content = @Content)
    })
    @PostMapping("/associar/cliente/{clienteId}/fiador/{fiadorId}")
    public ResponseEntity<Void> associarFiador(
            @Parameter(description = "ID do cliente a ser associado") @PathVariable Long clienteId,
            @Parameter(description = "ID do fiador a ser associado") @PathVariable Long fiadorId) {
        service.associarFiador(clienteId, fiadorId);
        return ResponseEntity.noContent().build();
    }
}