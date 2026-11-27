package br.com.fatec.agendaflow.dao;

import br.com.fatec.agendaflow.factory.UsuarioFactory;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.persistencia.Banco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements DAO<Usuario> {

    @Override
    public boolean inserir(Usuario dado) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha_hash, papel) VALUES (?, ?, ?, ?)";
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, dado.getNome());
            pst.setString(2, dado.getEmail());
            pst.setString(3, dado.getSenhaHash());
            pst.setString(4, dado.getPapel().name());
            if (pst.executeUpdate() < 1) {
                return false;
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    dado.setId(rs.getInt(1));
                }
            }
            return true;
        }
    }

    @Override
    public boolean alterar(Usuario dado) throws SQLException {
        String sql = "UPDATE usuario SET nome = ?, email = ?, senha_hash = ? WHERE id = ?";
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            pst.setString(1, dado.getNome());
            pst.setString(2, dado.getEmail());
            pst.setString(3, dado.getSenhaHash());
            pst.setInt(4, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public boolean remover(Usuario dado) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            pst.setInt(1, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public Usuario buscarID(int id) throws SQLException {
        return buscarUm("SELECT * FROM usuario WHERE id = ?", String.valueOf(id));
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        return buscarUm("SELECT * FROM usuario WHERE email = ? COLLATE NOCASE", email == null ? "" : email.trim());
    }

    public boolean existeEmail(String email) throws SQLException {
        return buscarPorEmail(email) != null;
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("SELECT * FROM usuario ORDER BY nome");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    private Usuario buscarUm(String sql, String parametro) throws SQLException {
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            pst.setString(1, parametro);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    /** O papel gravado no banco decide a classe concreta: quem decide é a Factory. */
    private Usuario montar(ResultSet rs) throws SQLException {
        return UsuarioFactory.criar(rs.getString("papel"), rs.getInt("id"), rs.getString("nome"),
                rs.getString("email"), rs.getString("senha_hash"));
    }
}
