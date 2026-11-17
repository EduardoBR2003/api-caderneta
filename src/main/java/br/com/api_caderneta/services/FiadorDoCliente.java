package br.com.api_caderneta.services;

import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import br.com.api_caderneta.model.Cliente;
import br.com.api_caderneta.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class FiadorDoCliente {
  private final ClienteRepository repository;
  private final br.com.api_caderneta.repository.FiadorRepository fiadores;
  private final br.com.api_caderneta.audit.AuditoriaService auditoria;

  public FiadorDoCliente(
      ClienteRepository repository,
      br.com.api_caderneta.repository.FiadorRepository fiadores,
      br.com.api_caderneta.audit.AuditoriaService auditoria) {
    this.repository = repository;
    this.fiadores = fiadores;
    this.auditoria = auditoria;
  }

  /** Executado dentro da transação de cadastro/edição do cliente, preservando os bloqueios. */
  public br.com.api_caderneta.model.Fiador salvar(FiadorRequestDTO dto, String cpfCliente) {
    if (dto == null) return null;
    var f =
        dto.getId() == null
            ? fiadores
                .porCpf(br.com.api_caderneta.util.Regras.cpf(dto.getCpf()))
                .orElseGet(br.com.api_caderneta.model.Fiador::new)
            : fiadores
                .findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiador não encontrado."));
    if (f.getId() != null) repository.bloquearPorFiador(f.getId()).forEach(Cliente::exigirAtivo);
    br.com.api_caderneta.util.RegrasFiador.exigirDistinto(
        cpfCliente, dto.getId() == null ? dto.getCpf() : f.getCpf());
    var antes =
        java.util.Arrays.asList(f.getNome(), f.getEmail(), f.getTelefone(), f.getEndereco());
    f.setNome(br.com.api_caderneta.util.Regras.nome(dto.getNome()));
    f.setCpf(br.com.api_caderneta.util.Regras.cpf(dto.getCpf()));
    f.setTelefone(dto.getTelefone());
    f.setEmail(dto.getEmail());
    f.setEndereco(dto.getEndereco());
    f = fiadores.saveAndFlush(f);
    auditoria.registrar(
        "FIADOR_SALVO",
        "FIADOR",
        f.getId(),
        br.com.api_caderneta.audit.AuditoriaService.mudanca(
            "cadastro",
            antes,
            java.util.Arrays.asList(f.getNome(), f.getEmail(), f.getTelefone(), f.getEndereco())));
    return f;
  }
}
