package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.FuncionarioDTO;
import br.com.api_caderneta.dto.FuncionarioRequestDTO;
import br.com.api_caderneta.dto.FuncionarioUpdateRequestDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.Funcionario;
import br.com.api_caderneta.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioRepository repository;
    private final DataMapper mapper;

    @Autowired
    public FuncionarioService(FuncionarioRepository repository, DataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public FuncionarioDTO createFuncionario(FuncionarioRequestDTO dto) {
        if (repository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("Já existe um funcionário com o CPF informado.");
        }
        if (repository.existsByLogin(dto.getLogin())) {
            throw new BusinessException("O login informado já está em uso.");
        }

        var entity = mapper.parseObject(dto, Funcionario.class);
        // NOTA: Em um ambiente real, a senha deve ser criptografada com BCrypt
        entity.setSenhaHash(dto.getSenha());

        var savedEntity = repository.save(entity);
        return mapper.parseObject(savedEntity, FuncionarioDTO.class);
    }

    @Transactional(readOnly = true)
    public FuncionarioDTO getFuncionarioById(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com ID: " + id));
        return mapper.parseObject(entity, FuncionarioDTO.class);
    }

    @Transactional(readOnly = true)
    public List<FuncionarioDTO> getAllFuncionarios() {
        return mapper.parseListObjects(repository.findAll(), FuncionarioDTO.class);
    }

    @Transactional
    public FuncionarioDTO updateFuncionario(Long id, FuncionarioUpdateRequestDTO dto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com ID: " + id));

        entity.setNome(dto.getNome() != null ? dto.getNome() : entity.getNome());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setTelefone(dto.getTelefone() != null ? dto.getTelefone() : entity.getTelefone());
        entity.setEndereco(dto.getEndereco() != null ? dto.getEndereco() : entity.getEndereco());
        entity.setCargo(dto.getCargo() != null ? dto.getCargo() : entity.getCargo());

        var updatedEntity = repository.save(entity);
        return mapper.parseObject(updatedEntity, FuncionarioDTO.class);
    }

    @Transactional
    public void deleteFuncionario(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com ID: " + id));
        if (!entity.getVendasRegistradas().isEmpty()) {
            throw new BusinessException("Não é possível excluir um funcionário que já registrou vendas.");
        }
        repository.delete(entity);
    }
}