package br.com.api_caderneta.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.JOINED) // Estratégia de herança
public abstract class Pessoa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPessoa;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14) // Formato CPF: xxx.xxx.xxx-xx
    private String cpf;

    @Column(length = 200)
    private String endereco;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefone;

    // Uma pessoa pode receber várias notificações
    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Notificacao> notificacoesRecebidas;

    public Pessoa() {
        this.notificacoesRecebidas = new ArrayList<>();
    }

    public Pessoa(String nome, String cpf, String endereco, String email, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.endereco = endereco;
        this.email = email;
        this.telefone = telefone;
        this.notificacoesRecebidas = new ArrayList<>();
    }

    // Getters e Setters
    public Long getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(Long idPessoa) {
        this.idPessoa = idPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<Notificacao> getNotificacoesRecebidas() {
        return notificacoesRecebidas;
    }

    public void setNotificacoesRecebidas(List<Notificacao> notificacoesRecebidas) {
        this.notificacoesRecebidas = notificacoesRecebidas;
    }

    // Métodos de negócio conforme Documento de Requisitos [cite: 11, 12]
    /**
     * Adiciona uma notificação à coleção interna de notificações da pessoa.
     * @param notificacao A notificação a ser adicionada.
     */
    public void adicionarNotificacaoRecebida(Notificacao notificacao) {
        if (this.notificacoesRecebidas == null) {
            this.notificacoesRecebidas = new ArrayList<>();
        }
        this.notificacoesRecebidas.add(notificacao);
        notificacao.setDestinatario(this); // Garante a bidirecionalidade
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(idPessoa, pessoa.idPessoa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPessoa);
    }

    @Override
    public String toString() {
        return "Pessoa{" +
                "idPessoa=" + idPessoa +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                '}';
    }
}