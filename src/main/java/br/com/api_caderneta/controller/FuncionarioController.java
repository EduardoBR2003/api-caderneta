package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.services.FuncionarioService;
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
@RequestMapping("/api/funcionarios")
@Tag(name = "Funcionários", description = "Endpoints para o gerenciamento de funcionários")
public class FuncionarioController {

    private final FuncionarioService service;

    @Autowired
    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar novo funcionário", description = "Cria o registro de um novo funcionário no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Funcionário cadastrado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<FuncionarioDTO> create(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do funcionário para cadastro") @RequestBody @Valid FuncionarioRequestDTO funcionario) {
        return new ResponseEntity<>(service.createFuncionario(funcionario), HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar funcionário por ID", description = "Retorna os detalhes de um funcionário específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário encontrado", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> getById(@Parameter(description = "ID do funcionário a ser buscado") @PathVariable Long id) {
        return ResponseEntity.ok(service.getFuncionarioById(id));
    }

    @Operation(summary = "Listar todos os funcionários", description = "Retorna uma lista com todos os funcionários cadastrados.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de funcionários retornada", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FuncionarioDTO.class)))))
    @GetMapping
    public ResponseEntity<List<FuncionarioDTO>> getAll() {
        return ResponseEntity.ok(service.getAllFuncionarios());
    }

    @Operation(summary = "Atualizar dados de um funcionário", description = "Atualiza os dados de um funcionário existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso", content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioDTO> update(@Parameter(description = "ID do funcionário a ser atualizado") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do funcionário") @RequestBody @Valid FuncionarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(service.updateFuncionario(id, dto));
    }

    @Operation(summary = "Excluir um funcionário", description = "Remove o registro de um funcionário do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Funcionário excluído com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "ID do funcionário a ser excluído") @PathVariable Long id) {
        service.deleteFuncionario(id);
        return ResponseEntity.noContent().build();
    }
}