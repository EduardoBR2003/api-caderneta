package br.com.api_caderneta.services;

import br.com.api_caderneta.audit.AuditoriaService;
import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.dto.*;
import br.com.api_caderneta.exceptions.*;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.model.*;
import br.com.api_caderneta.model.enums.*;
import br.com.api_caderneta.repository.*;
import br.com.api_caderneta.security.UsuarioAtual;
import br.com.api_caderneta.util.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendaService {
  private static final List<StatusDivida> ABERTAS =
      List.of(StatusDivida.ABERTA, StatusDivida.PAGA_PARCIALMENTE, StatusDivida.VENCIDA);
  private final VendaRepository vendaRepository;
  private final ClienteRepository clienteRepository;
  private final FuncionarioRepository funcionarioRepository;
  private final DataMapper mapper;
  private final NotificacaoService notificacaoService;
  private final AuditoriaService auditoria;
  private final DividaRepository dividaRepository;

  public VendaService(
      VendaRepository v,
      ClienteRepository c,
      FuncionarioRepository f,
      DataMapper m,
      NotificacaoService n,
      AuditoriaService a,
      DividaRepository d) {
    vendaRepository = v;
    clienteRepository = c;
    funcionarioRepository = f;
    mapper = m;
    notificacaoService = n;
    auditoria = a;
    dividaRepository = d;
  }

  private void autorizar(Venda v) {
    var u = UsuarioAtual.obrigatorio();
    if (u.getPerfil() != Perfil.GERENTE
        && !Objects.equals(u.getPessoaId(), v.getFuncionario().getId()))
      throw new ResourceNotFoundException("Venda não encontrada.");
  }

  private VendaDTO dto(Venda v) {
    var r = mapper.parseObject(v, VendaDTO.class);
    r.setVersao(v.getVersao());
    r.setStatusDivida(
        v.getDividaGerada() == null ? null : v.getDividaGerada().getStatusDivida().name());
    r.setTemPagamentos(
        v.getDividaGerada() != null && !v.getDividaGerada().getPagamentos().isEmpty());
    return r;
  }

  private Cliente cliente(Long id) {
    return clienteRepository
        .bloquear(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
  }

  private void credito(Cliente c, BigDecimal total, BigDecimal excluir) {
    var saldo = dividaRepository.somaPendenteDoCliente(c.getId(), ABERTAS);
    var exposicao = (saldo == null ? BigDecimal.ZERO : saldo).subtract(excluir).add(total);
    if (c.getLimiteCredito() != null && exposicao.compareTo(c.getLimiteCredito()) > 0)
      throw new BusinessException("O valor da venda excede o limite de crédito do cliente.");
  }

  private BigDecimal validarItens(List<ItemVendaRequestDTO> itens, BigDecimal total) {
    BigDecimal soma = BigDecimal.ZERO;
    if (itens != null) {
      if (itens.size() > 200) throw new BusinessException("Informe no máximo 200 itens.");
      for (var i : itens) {
        if (i == null
            || i.getQuantidade() == null
            || i.getQuantidade() < 1
            || i.getQuantidade() > 1000000)
          throw new BusinessException("A quantidade deve estar entre 1 e 1000000.");
        if (i.getDescricaoProduto() == null
            || i.getDescricaoProduto().trim().isEmpty()
            || i.getDescricaoProduto().trim().length() > 200)
          throw new BusinessException("Informe a descrição do item (até 200 caracteres).");
        var preco = Regras.dinheiro(i.getPrecoUnitario(), false);
        if (preco.precision() > 10)
          throw new BusinessException("Preço unitário fora do limite permitido.");
        soma = soma.add(preco.multiply(BigDecimal.valueOf(i.getQuantidade())));
      }
    }
    if (soma.compareTo(total) > 0)
      throw new BusinessException("A soma dos itens não pode ultrapassar o total da venda.");
    return soma;
  }

  private void itens(Venda v, List<ItemVendaRequestDTO> itens) {
    var novos = new ArrayList<ItemVenda>();
    if (itens != null)
      for (var i : itens) {
        var item = new ItemVenda();
        item.setDescricaoProduto(i.getDescricaoProduto().trim());
        item.setQuantidade(i.getQuantidade());
        item.setPrecoUnitario(i.getPrecoUnitario().setScale(2));
        novos.add(item);
      }
    v.setItensVenda(novos);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public VendaDTO createVenda(VendaRequestDTO req) {
    if (req.getClienteId() == null) throw new BusinessException("Cliente é obrigatório.");
    if (req.getFuncionarioId() != null)
      throw new BusinessException("O responsável vem da sessão; não envie funcionarioId.");
    var total = Regras.dinheiro(req.getValorTotal(), false);
    validarItens(req.getItens(), total);
    var c = cliente(req.getClienteId());
    credito(c, total, BigDecimal.ZERO);
    var u = UsuarioAtual.obrigatorio();
    var f =
        funcionarioRepository
            .findById(u.getPessoaId())
            .orElseThrow(
                () -> new BusinessException("O usuário autenticado não é um funcionário."));
    var v = new Venda();
    v.setCliente(c);
    v.setFuncionario(f);
    v.setValorTotal(total);
    itens(v, req.getItens());
    var d = new Divida();
    d.setCliente(c);
    d.setValorOriginal(total);
    d.setValorPendente(total);
    d.setDataVencimento(
        Fuso.hoje()
            .plusDays(
                c.getPrazoPagamentoPadraoDias() == null ? 30 : c.getPrazoPagamentoPadraoDias()));
    v.setDividaGerada(d);
    v = vendaRepository.saveAndFlush(v);
    notificacaoService.enviarNotificacao(
        c,
        "Nova compra de " + Moeda.formatar(total) + " (Venda #" + v.getId() + ")",
        TipoNotificacao.COMPRA_REALIZADA);
    auditoria.registrar(
        "VENDA_CRIADA",
        "VENDA",
        v.getId(),
        AuditoriaService.mudanca(
            "clienteId",
            null,
            c.getId(),
            "valorTotal",
            null,
            total,
            "funcionarioId",
            null,
            f.getId()));
    return dto(v);
  }

  @Transactional(readOnly = true)
  public Pagina<VendaDTO> listar(int page, int size) {
    return listar(page, size, "", null, null, null);
  }

  @Transactional(readOnly = true)
  public Pagina<VendaDTO> listar(
      int page,
      int size,
      String q,
      String status,
      java.time.LocalDate inicio,
      java.time.LocalDate fim) {
    var p =
        PageRequest.of(
            Math.max(0, page),
            Math.min(100, Math.max(1, size)),
            Sort.by(Sort.Order.desc("dataHora"), Sort.Order.desc("id")));
    var u = UsuarioAtual.obrigatorio();
    org.springframework.data.jpa.domain.Specification<Venda> spec =
        (root, query, cb) -> {
          var ps = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (u.getPerfil() != Perfil.GERENTE)
            ps.add(cb.equal(root.get("funcionario").get("id"), u.getPessoaId()));
          if (q != null && !q.isBlank())
            ps.add(
                cb.or(
                    cb.like(
                        cb.lower(root.get("cliente").get("nome")),
                        "%" + q.toLowerCase(java.util.Locale.ROOT) + "%"),
                    cb.equal(root.get("id").as(String.class), q)));
          if (inicio != null)
            ps.add(cb.greaterThanOrEqualTo(root.get("dataHora"), Fuso.inicioDoDiaUtc(inicio)));
          if (fim != null)
            ps.add(cb.lessThan(root.get("dataHora"), Fuso.inicioDoDiaUtc(fim.plusDays(1))));
          if (status != null && !status.isBlank()) {
            var d = root.join("dividaGerada");
            ps.add(br.com.api_caderneta.util.Consultas.status(cb, d, status));
          }
          return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    var vendas = vendaRepository.findAll(spec, p);
    return Pagina.de(vendas.map(this::dto));
  }

  @Transactional(readOnly = true)
  public java.util.List<VendaDTO> getAllVendas() {
    return listar(0, 100).content();
  }

  @Transactional(readOnly = true)
  public VendaDTO getVendaById(Long id) {
    var v =
        vendaRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada."));
    autorizar(v);
    return dto(v);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public VendaDTO updateVenda(Long id, VendaRequestDTO req) {
    if (!(req instanceof VendaUpdateRequestDTO update) || update.getVersao() == null)
      throw new BusinessException("Informe a versão da venda.");
    if (req.getFuncionarioId() != null)
      throw new BusinessException("Não é permitido alterar o responsável.");
    Long origem =
        vendaRepository
            .clienteId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada."));
    Long destino = req.getClienteId() == null ? origem : req.getClienteId();
    var clientes = new TreeMap<Long, Cliente>();
    for (Long cid : new TreeSet<>(List.of(origem, destino))) clientes.put(cid, cliente(cid));
    var v =
        vendaRepository
            .bloquear(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada."));
    autorizar(v);
    if (!Objects.equals(v.getCliente().getId(), origem)
        || !Objects.equals(v.getVersao(), update.getVersao()))
      throw new ConflictException(
          "A venda foi alterada. Reabra a edição para conferir os dados atuais.");
    var d = v.getDividaGerada();
    if (d == null)
      throw new ConflictException("Venda sem dívida vinculada. Solicite revisão do cadastro.");
    d =
        dividaRepository
            .findByIdParaAtualizar(d.getId())
            .orElseThrow(() -> new ConflictException("Dívida não encontrada."));
    var total =
        req.getValorTotal() == null
            ? v.getValorTotal()
            : Regras.dinheiro(req.getValorTotal(), false);
    var data = req.getDataHora() == null ? v.getDataHora() : req.getDataHora();
    boolean financeira =
        !destino.equals(origem)
            || total.compareTo(v.getValorTotal()) != 0
            || !data.equals(v.getDataHora());
    if (financeira && !d.getPagamentos().isEmpty())
      throw new ConflictException("Venda com pagamentos: somente os itens podem ser alterados.");
    if (req.getItens() != null) validarItens(req.getItens(), total);
    else if (v.calcularValorTotal().compareTo(total) > 0)
      throw new BusinessException("O total não pode ser menor que a soma dos itens.");
    var antesCliente = v.getCliente().getId();
    var antesTotal = v.getValorTotal();
    var antesData = v.getDataHora();
    var antesItens =
        v.getItensVenda().stream()
            .map(i -> List.of(i.getDescricaoProduto(), i.getQuantidade(), i.getPrecoUnitario()))
            .toList();
    if (financeira) {
      credito(
          clientes.get(destino),
          total,
          destino.equals(origem) ? d.getValorPendente() : BigDecimal.ZERO);
      v.setCliente(clientes.get(destino));
      v.setValorTotal(total);
      v.setDataHora(data);
      d.setCliente(clientes.get(destino));
      d.setValorOriginal(total);
      d.setValorPendente(total);
      if (!data.equals(antesData) || !destino.equals(origem)) {
        var emissao =
            data.atOffset(java.time.ZoneOffset.UTC).atZoneSameInstant(Fuso.NEGOCIO).toLocalDate();
        d.setDataEmissao(emissao);
        d.setDataVencimento(
            emissao.plusDays(
                v.getCliente().getPrazoPagamentoPadraoDias() == null
                    ? 30
                    : v.getCliente().getPrazoPagamentoPadraoDias()));
      }
    }
    if (req.getItens() != null) itens(v, req.getItens());
    // Força incremento da versão inclusive quando apenas os filhos foram alterados.
    v.setAtualizadaEm(java.time.Instant.now());
    v = vendaRepository.saveAndFlush(v);
    var depoisItens =
        v.getItensVenda().stream()
            .map(i -> List.of(i.getDescricaoProduto(), i.getQuantidade(), i.getPrecoUnitario()))
            .toList();
    auditoria.registrar(
        "VENDA_EDITADA",
        "VENDA",
        id,
        AuditoriaService.mudanca(
            "clienteId",
            antesCliente,
            destino,
            "valorTotal",
            antesTotal,
            total,
            "dataHora",
            antesData,
            data,
            "itens",
            antesItens,
            depoisItens));
    return dto(v);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void deleteVenda(Long id) {
    if (UsuarioAtual.obrigatorio().getPerfil() != Perfil.GERENTE)
      throw new org.springframework.security.access.AccessDeniedException("Somente gerente.");
    var cid =
        vendaRepository
            .clienteId(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada."));
    cliente(cid);
    var v =
        vendaRepository
            .bloquear(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada."));
    if (!cid.equals(v.getCliente().getId()))
      throw new ConflictException("Venda alterada; tente novamente.");
    if (v.getDividaGerada() != null) {
      var d = dividaRepository.findByIdParaAtualizar(v.getDividaGerada().getId()).orElseThrow();
      if (!d.getPagamentos().isEmpty())
        throw new ConflictException("Não é possível excluir venda com pagamentos.");
    }
    auditoria.registrar(
        "VENDA_EXCLUIDA",
        "VENDA",
        id,
        AuditoriaService.mudanca("valorTotal", v.getValorTotal(), null));
    vendaRepository.delete(v);
  }
}
