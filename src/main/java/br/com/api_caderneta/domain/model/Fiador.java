package br.com.api_caderneta.domain.model;

import br.com.api_caderneta.domain.model.Pessoa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fiadores")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Fiador extends Pessoa {

    // Um fiador pode afiançar múltiplos clientes
    @OneToMany(mappedBy = "fiador", fetch = FetchType.LAZY)
    private List<Cliente> clientesAfiancados = new ArrayList<>();



    public Fiador(String nome, String cpf, String endereco, String email, String telefone) {
        super(nome, cpf, endereco, email, telefone);
    }
}