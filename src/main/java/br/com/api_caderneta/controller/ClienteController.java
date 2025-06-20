package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Endpoints para o gerenciamento de clientes, limites e prazos.")
public class ClienteController {

    private final ClienteService service;

    @Autowired
    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar um novo cliente", description = "Cria o registro de um novo cliente no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida. Verifique os dados enviados.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ClienteDTO> create(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do cliente para cadastro.")
                                             @RequestBody @Valid ClienteRequestDTO cliente) {
        var createdCliente = service.createCliente(cliente);
        return new ResponseEntity<>(createdCliente, HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar cliente por ID", description = "Retorna os detalhes de um cliente específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getById(
            @Parameter(description = "ID do cliente a ser buscado.", required = true, example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getClienteById(id));
    }

    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista com todos os clientes cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes retornada.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ClienteResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllClientes());
    }

    @Operation(summary = "Atualizar dados de um cliente", description = "Atualiza os dados cadastrais de um cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(
            @Parameter(description = "ID do cliente a ser atualizado.", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do cliente.")
            @RequestBody @Valid ClienteUpdateRequestDTO cliente) {
        return ResponseEntity.ok(service.updateCliente(id, cliente));
    }

    @Operation(summary = "Excluir um cliente", description = "Remove o registro de um cliente do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do cliente a ser excluído.", required = true, example = "1")
            @PathVariable("id") Long id) {
        service.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Definir limite de crédito", description = "Define ou ajusta o limite de crédito para um cliente específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Limite de crédito atualizado com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PatchMapping("/{id}/limite-credito")
    public ResponseEntity<ClienteDTO> updateLimite(
            @Parameter(description = "ID do cliente.", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Valor do limite de crédito a ser definido.")
            @RequestBody @Valid LimiteCreditoRequestDTO dto) {
        return ResponseEntity.ok(service.updateLimiteCredito(id, dto));
    }

    @Operation(summary = "Configurar prazo de pagamento", description = "Define o prazo de pagamento em dias para um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prazo de pagamento atualizado com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClienteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PatchMapping("/{id}/prazo-pagamento")
    public ResponseEntity<ClienteDTO> updatePrazo(
            @Parameter(description = "ID do cliente.", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Número de dias para o prazo de pagamento.")
            @RequestBody @Valid PrazoPagamentoRequestDTO dto) {
        return ResponseEntity.ok(service.updatePrazoPagamento(id, dto));
    }
}