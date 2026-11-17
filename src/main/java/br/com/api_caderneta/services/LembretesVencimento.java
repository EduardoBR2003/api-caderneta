package br.com.api_caderneta.services;

import br.com.api_caderneta.model.enums.StatusDivida;
import br.com.api_caderneta.model.enums.TipoNotificacao;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.NotificacaoRepository;
import br.com.api_caderneta.util.Fuso;
import br.com.api_caderneta.util.Moeda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LembretesVencimento {
  private static final Logger logger = LoggerFactory.getLogger(LembretesVencimento.class);
  private final DividaRepository dividaRepository;
  private final NotificacaoRepository notificacaoRepository;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public LembretesVencimento(
      DividaRepository dividaRepository,
      NotificacaoRepository notificacaoRepository,
      org.springframework.jdbc.core.JdbcTemplate jdbc) {
    this.dividaRepository = dividaRepository;
    this.notificacaoRepository = notificacaoRepository;
    this.jdbc = jdbc;
  }

  /**
   * Lembrete de vencimento: no máximo UM por dívida/estado. A rotina roda toda noite, mas a
   * mensagem (que inclui o número da dívida, o saldo e a data) só é gerada se ainda não existir.
   */
  @Transactional
  @Scheduled(cron = "0 0 1 * * *", zone = "America/Sao_Paulo")
  public void executar() {
    logger.info("Gerando notificações de vencimento...");
    var hoje = Fuso.hoje();
    var dividas =
        dividaRepository.findComSaldoVencendoAntes(
            java.util.List.of(
                StatusDivida.ABERTA, StatusDivida.PAGA_PARCIALMENTE, StatusDivida.VENCIDA),
            hoje.plusDays(1));

    int novas = 0;
    for (var divida : dividas) {
      if (!divida.getCliente().isAtivo()) continue;
      String situacao = divida.getDataVencimento().isBefore(hoje) ? "venceu" : "vence hoje";
      String mensagem =
          String.format(
              "Lembrete: sua dívida #%d, com saldo de %s, %s em %s.",
              divida.getId(),
              Moeda.formatar(divida.getValorPendente()),
              situacao,
              divida.getDataVencimento());
      var cliente = divida.getCliente();
      if (notificacaoRepository.existsByDestinatarioIdAndTipoNotificacaoAndMensagem(
          cliente.getId(), TipoNotificacao.LEMBRETE_VENCIMENTO, mensagem)) {
        continue;
      }
      String chave = "divida:" + divida.getId() + ":" + situacao + ":" + divida.getDataVencimento();
      novas +=
          jdbc.update(
              "INSERT IGNORE INTO"
                  + " notificacoes(loja_id,destinatario_id,mensagem,tipo_notificacao,data_envio,lida,chave_evento)"
                  + " VALUES(?,?,?,'LEMBRETE_VENCIMENTO',?,false,?)",
              divida.getLojaId(),
              cliente.getId(),
              mensagem,
              Fuso.agoraUtc(),
              chave);
    }
    logger.info(
        "Lembretes de vencimento: {} novo(s) de {} dívida(s) com saldo vencida(s)/vencendo.",
        novas,
        dividas.size());
  }
}
