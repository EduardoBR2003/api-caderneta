package br.com.api_caderneta.config;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.model.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Configuração explícita para o mapeamento de Venda para VendaDTO
        modelMapper.createTypeMap(Venda.class, VendaDTO.class)
                .addMapping(Venda::getItensVenda, VendaDTO::setItens) // Mapeia a lista de itens
                .addMapping(src -> src.getCliente().getId(), VendaDTO::setClienteId) // Extrai o ID do cliente
                .addMapping(src -> src.getFuncionario().getId(), VendaDTO::setFuncionarioId) // Extrai o ID do funcionário
                .addMapping(src -> src.getDividaGerada().getId(), VendaDTO::setDividaId); // Extrai o ID da dívida

        modelMapper.createTypeMap(Divida.class, DividaDTO.class)
                .addMapping(src -> src.getCliente().getId(), DividaDTO::setClienteId) // Extrai o ID do cliente
                .addMapping(src -> src.getVendaOrigem().getId(), DividaDTO::setVendaId); // Extrai o ID da venda de origem

        // NOVA CONFIGURAÇÃO ADICIONADA
        modelMapper.createTypeMap(Pagamento.class, PagamentoDTO.class)
                .addMapping(src -> src.getDivida().getId(), PagamentoDTO::setDividaId); // Extrai o ID da dívida

        modelMapper.createTypeMap(Notificacao.class, NotificacaoDTO.class)
                .addMapping(src -> src.getDestinatario().getId(), NotificacaoDTO::setDestinatarioId)
                .addMapping(Notificacao::getIdNotificacao, NotificacaoDTO::setId)
                .addMapping(Notificacao::isLida, NotificacaoDTO::setLida);

        modelMapper.createTypeMap(Cliente.class, ClienteResponseDTO.class)
                .addMapping(Cliente::getFiador, ClienteResponseDTO::setFiador);

        return modelMapper;
    }
}