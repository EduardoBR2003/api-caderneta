package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fiadores")
public class Fiador extends Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "fiador", fetch = FetchType.LAZY)
    private List<Cliente> clientesAfiancados;

    public Fiador() {
        super();
        this.clientesAfiancados = new ArrayList<>();
    }

    public Fiador(String nome, String cpf, String endereco, String email, String telefone) {
        super(nome, cpf, endereco, email, telefone);
        this.clientesAfiancados = new ArrayList<>();
    }

    // Getters e Setters
    public List<Cliente> getClientesAfiancados() {
        return clientesAfiancados;
    }

    public void setClientesAfiancados(List<Cliente> clientesAfiancados) {
        this.clientesAfiancados = clientesAfiancados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o); // Fiador não adiciona novos campos para equals/hashCode além dos de Pessoa.
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Fiador{" +
                "idPessoa=" + getId() +
                ", nome='" + getNome() + '\'' +
                '}';
    }
}