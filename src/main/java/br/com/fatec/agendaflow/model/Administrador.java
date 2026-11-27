package br.com.fatec.agendaflow.model;

/** Usuário que configura o salão e gerencia todos os agendamentos. */
public class Administrador extends Usuario {

    public Administrador(int id, String nome, String email, String senhaHash) {
        super(id, nome, email, senhaHash);
    }

    @Override
    public Papel getPapel() {
        return Papel.ADMIN;
    }
}
