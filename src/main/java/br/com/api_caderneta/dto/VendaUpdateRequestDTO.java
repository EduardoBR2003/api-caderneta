package br.com.api_caderneta.dto;

/** Campos ausentes permanecem inalterados; itens vazios removem o detalhamento. */
public class VendaUpdateRequestDTO extends VendaRequestDTO {
  private Long versao;

  public Long getVersao() {
    return versao;
  }

  public void setVersao(Long versao) {
    this.versao = versao;
  }
}
