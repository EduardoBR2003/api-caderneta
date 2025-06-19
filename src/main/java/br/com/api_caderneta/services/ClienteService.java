package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository repository;
    private final DataMapper mapper;

    @Autowired
    public ClienteService(ClienteRepository repository, DataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO getClienteById(Long id) {
        logger.info("Buscando cliente com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Cliente não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
        return mapper.parseObject(entity, ClienteResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> getAllClientes() {
        logger.info("Buscando todos os clientes");
        var entities = repository.findAll();
        return mapper.parseListObjects(entities, ClienteResponseDTO.class);
    }

    @Transactional
    public ClienteDTO createCliente(ClienteRequestDTO dto) {
        logger.info("Criando novo cliente com CPF: {}", dto.getCpf());
        if (repository.existsByCpf(dto.getCpf())) {
            logger.warn("Tentativa de criar cliente com CPF duplicado: {}", dto.getCpf());
            throw new BusinessException("Já existe um cliente cadastrado com o CPF informado.");
        }
        var entity = mapper.parseObject(dto, Cliente.class);
        var savedEntity = repository.save(entity);
        logger.info("Cliente criado com sucesso. ID: {}", savedEntity.getId());
        return mapper.parseObject(savedEntity, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO updateCliente(Long id, ClienteUpdateRequestDTO dto) {
        logger.info("Atualizando cliente com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha na atualização. Cliente não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });

        entity.setNome(dto.getNome() != null ? dto.getNome() : entity.getNome());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setTelefone(dto.getTelefone() != null ? dto.getTelefone() : entity.getTelefone());
        entity.setEndereco(dto.getEndereco() != null ? dto.getEndereco() : entity.getEndereco());

        var updatedEntity = repository.save(entity);
        logger.info("Cliente com ID: {} atualizado com sucesso", id);
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }

    @Transactional
    public void deleteCliente(Long id) {
        logger.info("Tentando deletar cliente com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha ao deletar. Cliente não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
        if (!entity.getDividas().isEmpty()) {
            logger.warn("Tentativa de exclusão de cliente com dívidas pendentes. ID: {}", id);
            throw new BusinessException("Não é possível excluir um cliente que possui dívidas pendentes.");
        }
        repository.delete(entity);
        logger.info("Cliente com ID: {} deletado com sucesso", id);
    }

    @Transactional
    public ClienteDTO updateLimiteCredito(Long id, LimiteCreditoRequestDTO dto) {
        logger.info("Atualizando limite de crédito para o cliente ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha ao atualizar limite. Cliente não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
        entity.setLimiteCredito(dto.getNovoLimiteCredito());
        var updatedEntity = repository.save(entity);
        logger.info("Limite de crédito do cliente ID: {} atualizado para: {}", id, dto.getNovoLimiteCredito());
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO updatePrazoPagamento(Long id, PrazoPagamentoRequestDTO dto) {
        logger.info("Atualizando prazo de pagamento para o cliente ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha ao atualizar prazo. Cliente não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
                });
        entity.setPrazoPagamentoPadraoDias(dto.getNovoPrazoPagamentoDias());
        var updatedEntity = repository.save(entity);
        logger.info("Prazo de pagamento do cliente ID: {} atualizado para: {} dias", id, dto.getNovoPrazoPagamentoDias());
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }
}