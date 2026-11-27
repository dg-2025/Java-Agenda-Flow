package br.com.fatec.agendaflow.model;

/**
 * Usuário do sistema. É abstrata: na prática só existem {@link Cliente} e
 * {@link Administrador}, criados pela {@code UsuarioFactory}.
 */
public abstract class Usuario {

    protected int id;
    protected String nome;
    protected String email;
    protected String senhaHash;

    protected Usuario(int id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    /** Papel do usuário (ADMIN ou CLIENTE). Cada subclasse devolve o seu. */
    public abstract Papel getPapel();

    public boolean isAdmin() {
        return getPapel() == Papel.ADMIN;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    @Override
    public String toString() {
        return nome;
    }
}
