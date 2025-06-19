package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final DataMapper mapper;

    @Autowired
    public ClienteService(ClienteRepository repository, DataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ClienteDTO getClienteById(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return mapper.parseObject(entity, ClienteDTO.class);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> getAllClientes() {
        var entities = repository.findAll();
        return mapper.parseListObjects(entities, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO createCliente(ClienteRequestDTO dto) {
        if (repository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("Já existe um cliente cadastrado com o CPF informado.");
        }
        var entity = mapper.parseObject(dto, Cliente.class);
        var savedEntity = repository.save(entity);
        return mapper.parseObject(savedEntity, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO updateCliente(Long id, ClienteUpdateRequestDTO dto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        // Atualiza apenas os campos fornecidos
        entity.setNome(dto.getNome() != null ? dto.getNome() : entity.getNome());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setTelefone(dto.getTelefone() != null ? dto.getTelefone() : entity.getTelefone());
        entity.setEndereco(dto.getEndereco() != null ? dto.getEndereco() : entity.getEndereco());

        var updatedEntity = repository.save(entity);
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }

    @Transactional
    public void deleteCliente(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        if (!entity.getDividas().isEmpty()) {
            throw new BusinessException("Não é possível excluir um cliente que possui dívidas pendentes.");
        }
        repository.delete(entity);
    }

    @Transactional
    public ClienteDTO updateLimiteCredito(Long id, LimiteCreditoRequestDTO dto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        entity.setLimiteCredito(dto.getNovoLimiteCredito());
        var updatedEntity = repository.save(entity);
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }

    @Transactional
    public ClienteDTO updatePrazoPagamento(Long id, PrazoPagamentoRequestDTO dto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        entity.setPrazoPagamentoPadraoDias(dto.getNovoPrazoPagamentoDias());
        var updatedEntity = repository.save(entity);
        return mapper.parseObject(updatedEntity, ClienteDTO.class);
    }
}