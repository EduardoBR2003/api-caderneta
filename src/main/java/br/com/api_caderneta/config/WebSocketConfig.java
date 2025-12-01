package br.com.api_caderneta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um broker simples em memória para enviar mensagens aos clientes
        // O frontend vai se inscrever em URLs começando com /topic
        config.enableSimpleBroker("/topic");
        // Prefixo para mensagens que vão DO cliente PARA o servidor (se necessário)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Ponto de conexão do socket. O frontend vai conectar em http://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite conexão de qualquer origem (frontend em outra porta)
                .withSockJS(); // Habilita fallback para SockJS se o navegador não suportar WS puro
    }
}