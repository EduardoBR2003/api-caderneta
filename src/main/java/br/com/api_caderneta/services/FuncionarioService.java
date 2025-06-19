package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuncionarioService {

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioService.class);

    private final FuncionarioRepository repository;
    private final VendaRepository vendaRepository;
    private final DataMapper mapper;

    @Autowired
    public FuncionarioService(FuncionarioRepository repository, VendaRepository vendaRepository, DataMapper mapper) {
        this.repository = repository;
        this.vendaRepository = vendaRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public FuncionarioDTO getFuncionarioById(Long id) {
        logger.info("Buscando funcionário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Funcionário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Funcionário não encontrado com o ID: " + id);
                });
        return mapper.parseObject(entity, FuncionarioDTO.class);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioDTO> getAllFuncionarios() {
        logger.info("Buscando todos os funcionários");
        var entities = repository.findAll();
        return mapper.parseListObjects(entities, FuncionarioDTO.class);
    }

    @Transactional
    public FuncionarioDTO createFuncionario(FuncionarioRequestDTO dto) {
        logger.info("Criando novo funcionário com CPF: {}", dto.getCpf());
        if (repository.existsByCpf(dto.getCpf())) {
            logger.warn("Tentativa de criar funcionário com CPF duplicado: {}", dto.getCpf());
            throw new BusinessException("Já existe um funcionário cadastrado com o CPF informado.");
        }
        var entity = mapper.parseObject(dto, Funcionario.class);
        var savedEntity = repository.save(entity);
        logger.info("Funcionário criado com sucesso. ID: {}", savedEntity.getId());
        return mapper.parseObject(savedEntity, FuncionarioDTO.class);
    }

    @Transactional
    public FuncionarioDTO updateFuncionario(Long id, FuncionarioUpdateRequestDTO dto) {
        logger.info("Atualizando funcionário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha na atualização. Funcionário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Funcionário não encontrado com o ID: " + id);
                });

        entity.setNome(dto.getNome() != null ? dto.getNome() : entity.getNome());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setTelefone(dto.getTelefone() != null ? dto.getTelefone() : entity.getTelefone());
        entity.setCargo(dto.getCargo() != null ? dto.getCargo() : entity.getCargo());

        var updatedEntity = repository.save(entity);
        logger.info("Funcionário com ID: {} atualizado com sucesso", id);
        return mapper.parseObject(updatedEntity, FuncionarioDTO.class);
    }

    @Transactional
    public void deleteFuncionario(Long id) {
        logger.info("Tentando deletar funcionário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha ao deletar. Funcionário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Funcionário não encontrado com o ID: " + id);
                });

        if (vendaRepository.existsByFuncionarioId(id)) {
            logger.warn("Tentativa de exclusão de funcionário com vendas associadas. ID: {}", id);
            throw new BusinessException("Não é possível excluir um funcionário que já realizou vendas.");
        }

        repository.delete(entity);
        logger.info("Funcionário com ID: {} deletado com sucesso", id);
    }
}