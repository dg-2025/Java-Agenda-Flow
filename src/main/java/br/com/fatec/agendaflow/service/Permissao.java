package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.exception.AcessoNegadoException;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Usuario;

/** RN-09 / RNF-05: confere o papel antes de qualquer ação restrita. */
public final class Permissao {

    private Permissao() {
    }

    public static void exigirAdmin(Usuario usuario) {
        if (usuario == null || usuario.getPapel() != Papel.ADMIN) {
            throw new AcessoNegadoException("Acesso negado: esta área é exclusiva do administrador.");
        }
    }

    public static void exigirCliente(Usuario usuario) {
        if (usuario == null || usuario.getPapel() != Papel.CLIENTE) {
            throw new AcessoNegadoException("Acesso negado: esta ação é exclusiva de clientes.");
        }
    }
}
