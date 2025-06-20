package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.dto.NotificacaoRequestDTO;
import br.com.api_caderneta.services.NotificacaoService;
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
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Endpoints para envio e consulta de notificações de cobrança.")
public class NotificacaoController {

    private final NotificacaoService service;

    @Autowired
    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar notificações de um cliente", description = "Retorna o histórico de notificações enviadas a um cliente.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de notificações retornada", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacaoDTO.class)))))
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<NotificacaoDTO>> getNotificacoesPorCliente(@Parameter(description = "ID do cliente") @PathVariable Long clienteId) {
        List<NotificacaoDTO> notificacoes = service.getNotificacoesByCliente(clienteId);
        return ResponseEntity.ok(notificacoes);
    }

    @Operation(summary = "Criar notificação manualmente", description = "Registra uma notificação manual no sistema para um cliente e uma dívida específicos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificação criada com sucesso", content = @Content(schema = @Schema(implementation = NotificacaoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou Dívida não encontrados", content = @Content)
    })
    @PostMapping("/manual")
    public ResponseEntity<NotificacaoDTO> createManualNotificacao(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da notificação manual") @RequestBody @Valid NotificacaoRequestDTO requestDto) {
        NotificacaoDTO dto = service.createManualNotificacao(requestDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}