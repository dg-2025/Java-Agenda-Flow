package br.com.fatec.agendaflow.model;

/** Usuário que faz agendamentos. */
public class Cliente extends Usuario {

    public Cliente(int id, String nome, String email, String senhaHash) {
        super(id, nome, email, senhaHash);
    }

    @Override
    public Papel getPapel() {
        return Papel.CLIENTE;
    }
}
