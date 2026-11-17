package br.com.api_caderneta.catalogo;

import org.springframework.data.jpa.repository.*;

public interface CatalogoRepository extends JpaRepository<ItemCatalogo, Long>, JpaSpecificationExecutor<ItemCatalogo> {}
