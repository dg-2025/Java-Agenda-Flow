package br.com.fatec.agendaflow.dao;

import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.persistencia.Banco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO implements DAO<Servico> {

    @Override
    public boolean inserir(Servico dado) throws SQLException {
        String sql = "INSERT INTO servico (nome, descricao, duracao_minutos, valor, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(pst, dado);
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
    public boolean alterar(Servico dado) throws SQLException {
        String sql = "UPDATE servico SET nome = ?, descricao = ?, duracao_minutos = ?, valor = ?, ativo = ? WHERE id = ?";
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            preencher(pst, dado);
            pst.setInt(6, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    /** Serviço não é apagado (tem histórico de agendamentos): é inativado (RF-03). */
    @Override
    public boolean remover(Servico dado) throws SQLException {
        dado.setAtivo(false);
        return alterar(dado);
    }

    @Override
    public Servico buscarID(int id) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("SELECT * FROM servico WHERE id = ?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    @Override
    public List<Servico> listar() throws SQLException {
        return consultar("SELECT * FROM servico ORDER BY nome");
    }

    /** RF-07: o cliente só enxerga serviços ativos. */
    public List<Servico> listarAtivos() throws SQLException {
        return consultar("SELECT * FROM servico WHERE ativo = 1 ORDER BY nome");
    }

    private List<Servico> consultar(String sql) throws SQLException {
        List<Servico> lista = new ArrayList<>();
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    private void preencher(PreparedStatement pst, Servico s) throws SQLException {
        pst.setString(1, s.getNome());
        pst.setString(2, s.getDescricao());
        pst.setInt(3, s.getDuracaoMinutos());
        pst.setDouble(4, s.getValor());
        pst.setInt(5, s.isAtivo() ? 1 : 0);
    }

    private Servico montar(ResultSet rs) throws SQLException {
        return new Servico(rs.getInt("id"), rs.getString("nome"), rs.getString("descricao"),
                rs.getInt("duracao_minutos"), rs.getDouble("valor"), rs.getInt("ativo") == 1);
    }
}
