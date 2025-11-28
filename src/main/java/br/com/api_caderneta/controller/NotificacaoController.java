package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.dto.NotificacaoRequestDTO;
import br.com.api_caderneta.services.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações")
public class NotificacaoController {

    private final NotificacaoService service;

    @Autowired
    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> getAllNotificacoes() {
        return ResponseEntity.ok(service.getAllNotificacoes());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<NotificacaoDTO>> getNotificacoesPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.getNotificacoesByCliente(clienteId));
    }

    @PostMapping("/manual")
    public ResponseEntity<NotificacaoDTO> createManualNotificacao(@RequestBody @Valid NotificacaoRequestDTO dto) {
        return new ResponseEntity<>(service.createManualNotificacao(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        service.marcarComoLida(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasLidas() {
        service.marcarTodasComoLidas();
        return ResponseEntity.noContent().build();
    }
}