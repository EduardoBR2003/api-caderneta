package br.com.api_caderneta.me;

import br.com.api_caderneta.auth.Dtos.Pagina;
import br.com.api_caderneta.dto.DividaDTO;
import br.com.api_caderneta.dto.PagamentoDTO;
import br.com.api_caderneta.dto.VendaDTO;
import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.mapper.DataMapper;
import br.com.api_caderneta.repository.DividaRepository;
import br.com.api_caderneta.repository.PagamentoRepository;
import br.com.api_caderneta.repository.VendaRepository;
import br.com.api_caderneta.security.UsuarioPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Área do cliente. A pessoa vem SEMPRE da sessão; nenhum identificador de cliente é aceito como parâmetro,
 * então não há como pedir dados de outra pessoa. (/api/me/historico está em HistoricoController.)
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final VendaRepository vendas;
    private final DividaRepository dividas;
    private final PagamentoRepository pagamentos;
    private final DataMapper mapper;

    public MeController(VendaRepository vendas, DividaRepository dividas, PagamentoRepository pagamentos, DataMapper mapper) {
        this.vendas = vendas;
        this.dividas = dividas;
        this.pagamentos = pagamentos;
        this.mapper = mapper;
    }

    @GetMapping("/vendas")
    @Transactional(readOnly = true)
    public Pagina<VendaDTO> vendas(@AuthenticationPrincipal UsuarioPrincipal u, @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return Pagina.de(vendas.findByClienteId(u.getPessoaId(),
                pagina(page, size, Sort.by(Sort.Order.desc("dataHora"), Sort.Order.desc("id"))))
                .map(v -> mapper.parseObject(v, VendaDTO.class)));
    }

    @GetMapping("/dividas")
    @Transactional(readOnly = true)
    public Pagina<DividaDTO> dividas(@AuthenticationPrincipal UsuarioPrincipal u, @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return Pagina.de(dividas.findByClienteId(u.getPessoaId(),
                pagina(page, size, Sort.by(Sort.Order.asc("dataVencimento"), Sort.Order.asc("id"))))
                .map(d -> mapper.parseObject(d, DividaDTO.class)));
    }

    @GetMapping("/pagamentos")
    @Transactional(readOnly = true)
    public Pagina<PagamentoDTO> pagamentos(@AuthenticationPrincipal UsuarioPrincipal u, @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return Pagina.de(pagamentos.findDoCliente(u.getPessoaId(),
                pagina(page, size, Sort.by(Sort.Order.desc("dataPagamento"), Sort.Order.desc("id"))))
                .map(p -> mapper.parseObject(p, PagamentoDTO.class)));
    }

    private static Pageable pagina(int page, int size, Sort sort) {
        if (page < 0) throw new BusinessException("page deve ser maior ou igual a zero.");
        if (size < 1 || size > 100) throw new BusinessException("size deve estar entre 1 e 100.");
        return PageRequest.of(page, size, sort);
    }
}
