package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoRequestDTO;
import br.com.api_caderneta.services.DividaService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dividas")
@Tag(name = "Vendas a Prazo (Dívidas)", description = "Endpoints para consultar dívidas e controlar pagamentos.")
public class DividaController {

    private final DividaService service;

    @Autowired
    public DividaController(DividaService service) {
        this.service = service;
    }

    @Operation(summary = "Buscar dívida por ID", description = "Retorna os detalhes de uma dívida específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dívida encontrada", content = @Content(schema = @Schema(implementation = DividaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DividaDTO> getById(@Parameter(description = "ID da dívida a ser buscada") @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getDividaById(id));
    }

    @Operation(summary = "Consultar dívidas de um cliente", description = "Lista todas as dívidas (abertas e pagas) de um cliente específico.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de dívidas retornada", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DividaDTO.class)))))
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<DividaDTO>> getDividasByCliente(@Parameter(description = "ID do cliente para consulta") @PathVariable Long clienteId) {
        return ResponseEntity.ok(service.getDividasByCliente(clienteId));
    }

    @Operation(summary = "Registrar pagamento em uma dívida", description = "Registra um pagamento, parcial ou total, para uma dívida, atualizando seu saldo devedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento registrado e dívida atualizada", content = @Content(schema = @Schema(implementation = DividaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados do pagamento inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dívida não encontrada", content = @Content)
    })
    @PostMapping("/{dividaId}/pagamentos")
    public ResponseEntity<DividaDTO> registrarPagamento(
            @Parameter(description = "ID da dívida que receberá o pagamento") @PathVariable("dividaId") Long dividaId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do pagamento") @RequestBody @Valid PagamentoRequestDTO pagamento) {
        var dividaAtualizada = service.registrarPagamento(dividaId, pagamento);
        return ResponseEntity.ok(dividaAtualizada);
    }
}