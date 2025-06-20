package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.dto.VendaRequestDTO;
import br.com.api_caderneta.services.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendas")
@Tag(name = "Venda", description = "Endpoints para Registrar Vendas a Prazo")
public class VendaController {

    @Autowired
    private VendaService service;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma venda", description = "Busca uma venda pelo seu ID",
            tags = {"Venda"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content(schema = @Schema(implementation = VendaDTO.class))),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<VendaDTO> getVendaById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getVendaById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nova venda", description = "Registra uma nova venda a prazo (fiado) para um cliente",
            tags = {"Venda"},
            responses = {
                    @ApiResponse(description = "Created", responseCode = "201", content = @Content(schema = @Schema(implementation = VendaDTO.class))),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<VendaDTO> createVenda(@RequestBody VendaRequestDTO venda) {
        return new ResponseEntity<>(service.createVenda(venda), HttpStatus.CREATED);
    }
}