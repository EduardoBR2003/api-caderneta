package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.NotificacaoDTO;
import br.com.api_caderneta.dto.NotificacaoRequestDTO;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Notificacao;
import br.com.api_caderneta.model.Pessoa;
import br.com.api_caderneta.model.enums.TipoNotificacao;
import br.com.api_caderneta.repository.ClienteRepository;
import br.com.api_caderneta.repository.NotificacaoRepository;
import br.com.api_caderneta.util.Fuso;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {

  private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
  private final NotificacaoRepository notificacaoRepository;
  private final DataMapper mapper;
  private final ClienteRepository clienteRepository;

  @Autowired
  public NotificacaoService(
      NotificacaoRepository notificacaoRepository,
      DataMapper mapper,
      ClienteRepository clienteRepository) {
    this.notificacaoRepository = notificacaoRepository;
    this.mapper = mapper;
    this.clienteRepository = clienteRepository;
  }

  // Cria e persiste o evento. Sem WebSocket: a interface consulta o histórico por REST.
  public void enviarNotificacao(Pessoa destinatario, String mensagem, TipoNotificacao tipo) {
    salvar(destinatario, mensagem, tipo);
  }

  private Notificacao salvar(Pessoa destinatario, String mensagem, TipoNotificacao tipo) {
    var notificacao = new Notificacao();
    notificacao.setDestinatario(destinatario);
    notificacao.setMensagem(mensagem);
    notificacao.setTipoNotificacao(tipo);
    notificacao.setDataEnvio(Fuso.agoraUtc());

    var saved = notificacaoRepository.save(notificacao);
    logger.info("Evento de histórico registrado: {}", saved.getIdNotificacao());
    return saved;
  }

  @Transactional(readOnly = true)
  public List<NotificacaoDTO> getNotificacoesByCliente(Long clienteId) {
    logger.info("Buscando notificações para o cliente ID: {}", clienteId);
    var notificacoes = notificacaoRepository.findByDestinatarioId(clienteId);
    return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
  }

  @Transactional(readOnly = true)
  public List<NotificacaoDTO> getAllNotificacoes() {
    logger.info("Buscando todas as notificações do sistema");
    var notificacoes = notificacaoRepository.findAll();
    return mapper.parseListObjects(notificacoes, NotificacaoDTO.class);
  }

  @Transactional
  public NotificacaoDTO createManualNotificacao(NotificacaoRequestDTO dto) {
    var cliente =
        clienteRepository
            .bloquear(dto.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

    cliente.exigirAtivo();
    return mapper.parseObject(
        salvar(cliente, dto.getMensagem(), dto.getTipoNotificacao()), NotificacaoDTO.class);
  }

  // Marcar notificação como lida
  @Transactional
  public void marcarComoLida(Long id) {
    var notificacao =
        notificacaoRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
    notificacao.setLida(true);
    notificacaoRepository.save(notificacao);
  }

  @Transactional
  public void marcarTodasComoLidas() {
    var todas = notificacaoRepository.findAll();
    todas.forEach(n -> n.setLida(true));
    notificacaoRepository.saveAll(todas);
  }
}
