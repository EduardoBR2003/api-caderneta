package br.com.api_caderneta.tarefas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cron interno (@Scheduled). Desligue com {@code caderneta.agendamento.interno=false} em plataformas que
 * escalam a zero (Cloud Run): lá o cron não roda de forma confiável e a tarefa "tarefa-lembretes" é
 * disparada por um agendador externo.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "caderneta.agendamento.interno", havingValue = "true", matchIfMissing = true)
public class AgendamentoConfig {}
