package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.services.ProprietarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proprietarios")
@Tag(name = "Proprietário", description = "Endpoints para Gerenciar Proprietários")
public class ProprietarioController {

    @Autowired
    private ProprietarioService service;

    @GetMapping(value = "/{id}")
    @Operation(summary = "Buscar um proprietário", description = "Busca um proprietário pelo seu ID",
            tags = {"Proprietário"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))
                    ),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<FuncionarioDTO> getProprietarioById(@PathVariable(value = "id") Long id) {
        FuncionarioDTO dto = service.getProprietarioById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "Buscar todos os proprietários", description = "Busca todos os proprietários cadastrados",
            tags = {"Proprietário"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FuncionarioDTO.class))
                    ),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<List<FuncionarioDTO>> getAllProprietarios() {
        List<FuncionarioDTO> proprietarios = service.getAllProprietarios();
        return ResponseEntity.ok(proprietarios);
    }

    @PostMapping
    @Operation(summary = "Criar um novo proprietário", description = "Cria um novo proprietário no sistema",
            tags = {"Proprietário"},
            responses = {
                    @ApiResponse(description = "Created", responseCode = "201",
                            content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))
                    ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<FuncionarioDTO> createProprietario(@RequestBody FuncionarioRequestDTO requestDto) {
        FuncionarioDTO dto = service.createProprietario(requestDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Atualizar um proprietário", description = "Atualiza os dados de um proprietário existente",
            tags = {"Proprietário"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = FuncionarioDTO.class))
                    ),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<FuncionarioDTO> updateProprietario(
            @PathVariable(value = "id") Long id,
            @RequestBody FuncionarioUpdateRequestDTO requestDto) {
        FuncionarioDTO dto = service.updateProprietario(id, requestDto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Deletar um proprietário", description = "Deleta um proprietário pelo seu ID",
            tags = {"Proprietário"},
            responses = {
                    @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteProprietario(@PathVariable(value = "id") Long id) {
        service.deleteProprietario(id);
        return ResponseEntity.noContent().build();
    }
}