package br.com.api_caderneta.catalogo;

import br.com.api_caderneta.auth.Dtos.Pagina;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
  private final CatalogoService catalogo;
  public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }
  @GetMapping
  public Pagina<CatalogoService.ItemDTO> listar(@RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="") String q,
      @RequestParam(defaultValue="false") boolean incluirInativos) {
    return catalogo.listar(page, size, q, incluirInativos);
  }
  @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public CatalogoService.ItemDTO criar(@RequestBody CatalogoService.Entrada req) { return catalogo.salvar(null, req); }
  @PutMapping("/{id}")
  public CatalogoService.ItemDTO editar(@PathVariable Long id, @RequestBody CatalogoService.Entrada req) { return catalogo.salvar(id, req); }
}
