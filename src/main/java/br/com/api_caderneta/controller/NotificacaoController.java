package br.com.api_caderneta.controller;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    @Autowired
    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<NotificacaoDTO>> getNotificacoesDoCliente(@PathVariable Long clienteId) {
        List<NotificacaoDTO> notificacoes = service.getNotificacoesByCliente(clienteId);
        return ResponseEntity.ok(notificacoes);
    }
}