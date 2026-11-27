package br.com.fatec.agendaflow.factory;

import br.com.fatec.agendaflow.model.Administrador;
import br.com.fatec.agendaflow.model.Cliente;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Usuario;

/**
 * Padrão GoF <b>Factory</b> (criacional).
 * <p>
 * Centraliza a decisão de "qual classe concreta de usuário instanciar". O
 * cadastro (sempre CLIENTE) e o DAO (que lê o papel gravado no banco) pedem um
 * {@link Usuario} à fábrica, sem conhecer {@link Cliente} nem
 * {@link Administrador}. Se surgir um novo papel, só a fábrica muda.
 */
public final class UsuarioFactory {

    private UsuarioFactory() {
    }

    public static Usuario criar(Papel papel, int id, String nome, String email, String senhaHash) {
        if (papel == null) {
            throw new IllegalArgumentException("O papel do usuário é obrigatório.");
        }
        return switch (papel) {
            case ADMIN -> new Administrador(id, nome, email, senhaHash);
            case CLIENTE -> new Cliente(id, nome, email, senhaHash);
        };
    }

    /** Versão usada pelo DAO, que lê o papel do banco como texto. */
    public static Usuario criar(String papel, int id, String nome, String email, String senhaHash) {
        if (papel == null || papel.isBlank()) {
            throw new IllegalArgumentException("O papel do usuário é obrigatório.");
        }
        try {
            return criar(Papel.valueOf(papel.trim().toUpperCase()), id, nome, email, senhaHash);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de usuário desconhecido: " + papel);
        }
    }

    /** Atalho do RF-01: todo cadastro feito pela tela é de CLIENTE. */
    public static Usuario novoCliente(String nome, String email, String senhaHash) {
        return criar(Papel.CLIENTE, 0, nome, email, senhaHash);
    }
}
