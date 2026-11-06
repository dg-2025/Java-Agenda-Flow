package br.com.fatec.agenda_flow.factory;

import br.com.fatec.agenda_flow.Administrador;
import br.com.fatec.agenda_flow.Cliente;
import br.com.fatec.agenda_flow.Usuario;

public class UsuarioFactory {
    public static Usuario criarUsuario(String tipo, int id, String nome, String email, String senha) {
        if ("ADMIN".equalsIgnoreCase(tipo)) {
            return new Administrador(id, nome, email, senha);
        } else if ("CLIENTE".equalsIgnoreCase(tipo)) {
            return new Cliente(id, nome, email, senha);
        }
        throw new IllegalArgumentException("Tipo de usuario desconhecido: " + tipo);
    }
}
