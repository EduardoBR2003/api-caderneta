package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Proprietario;
import br.com.api_caderneta.repository.FuncionarioRepository;
import br.com.api_caderneta.repository.ProprietarioRepository;
import br.com.api_caderneta.repository.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProprietarioService {

    private static final Logger logger = LoggerFactory.getLogger(ProprietarioService.class);

    private final ProprietarioRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final VendaRepository vendaRepository;
    private final DataMapper mapper;

    @Autowired
    public ProprietarioService(ProprietarioRepository repository, FuncionarioRepository funcionarioRepository, VendaRepository vendaRepository, DataMapper mapper) {
        this.repository = repository;
        this.funcionarioRepository = funcionarioRepository;
        this.vendaRepository = vendaRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public FuncionarioDTO getProprietarioById(Long id) {
        logger.info("Buscando proprietário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Proprietário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Proprietário não encontrado com o ID: " + id);
                });
        return mapper.parseObject(entity, FuncionarioDTO.class);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioDTO> getAllProprietarios() {
        logger.info("Buscando todos os proprietários");
        var entities = repository.findAll();
        return mapper.parseListObjects(entities, FuncionarioDTO.class);
    }

    @Transactional
    public FuncionarioDTO createProprietario(FuncionarioRequestDTO dto) {
        logger.info("Criando novo proprietário com CPF: {}", dto.getCpf());
        if (funcionarioRepository.existsByCpf(dto.getCpf())) {
            logger.warn("Tentativa de criar proprietário com CPF duplicado: {}", dto.getCpf());
            throw new BusinessException("Já existe uma pessoa (funcionário ou cliente) cadastrada com o CPF informado.");
        }
        if (funcionarioRepository.existsByLogin(dto.getLogin())) {
            logger.warn("Tentativa de criar proprietário com login duplicado: {}", dto.getLogin());
            throw new BusinessException("O login informado já está em uso.");
        }

        var entity = mapper.parseObject(dto, Proprietario.class);
        // Em um projeto real, a senha seria codificada usando um PasswordEncoder (ex: BCryptPasswordEncoder)
        entity.setSenhaHash(dto.getSenhaHash());

        var savedEntity = repository.save(entity);
        logger.info("Proprietário criado com sucesso. ID: {}", savedEntity.getId());
        return mapper.parseObject(savedEntity, FuncionarioDTO.class);
    }

    @Transactional
    public FuncionarioDTO updateProprietario(Long id, FuncionarioUpdateRequestDTO dto) {
        logger.info("Atualizando proprietário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha na atualização. Proprietário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Proprietário não encontrado com o ID: " + id);
                });

        // Atualiza os campos se eles não forem nulos no DTO
        if (dto.getNome() != null) entity.setNome(dto.getNome());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) entity.setTelefone(dto.getTelefone());
        if (dto.getEndereco() != null) entity.setEndereco(dto.getEndereco());
        if (dto.getCargo() != null) entity.setCargo(dto.getCargo());

        var updatedEntity = repository.save(entity);
        logger.info("Proprietário com ID: {} atualizado com sucesso", id);
        return mapper.parseObject(updatedEntity, FuncionarioDTO.class);
    }

    @Transactional
    public void deleteProprietario(Long id) {
        logger.info("Tentando deletar proprietário com ID: {}", id);
        var entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Falha ao deletar. Proprietário não encontrado com o ID: {}", id);
                    return new ResourceNotFoundException("Proprietário não encontrado com o ID: " + id);
                });

        if (vendaRepository.existsByFuncionarioId(id)) {
            logger.warn("Tentativa de exclusão de proprietário com vendas associadas. ID: {}", id);
            throw new BusinessException("Não é possível excluir um proprietário que já realizou vendas.");
        }

        repository.delete(entity);
        logger.info("Proprietário com ID: {} deletado com sucesso", id);
    }
}