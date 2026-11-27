package br.com.fatec.agendaflow.dao;

import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.persistencia.Banco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Profissionais e o vínculo profissional × serviço (RF-04, RF-05). */
public class ProfissionalDAO implements DAO<Profissional> {

    @Override
    public boolean inserir(Profissional dado) throws SQLException {
        String sql = "INSERT INTO profissional (nome, ativo) VALUES (?, ?)";
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, dado.getNome());
            pst.setInt(2, dado.isAtivo() ? 1 : 0);
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
    public boolean alterar(Profissional dado) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("UPDATE profissional SET nome = ?, ativo = ? WHERE id = ?")) {
            pst.setString(1, dado.getNome());
            pst.setInt(2, dado.isAtivo() ? 1 : 0);
            pst.setInt(3, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    /** Profissional com histórico não é apagado: é inativado (RF-04). */
    @Override
    public boolean remover(Profissional dado) throws SQLException {
        dado.setAtivo(false);
        return alterar(dado);
    }

    @Override
    public Profissional buscarID(int id) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("SELECT * FROM profissional WHERE id = ?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Profissional p = montar(rs);
                carregarServicos(c, p);
                return p;
            }
        }
    }

    @Override
    public List<Profissional> listar() throws SQLException {
        List<Profissional> lista = new ArrayList<>();
        try (Connection c = Banco.conectar()) {
            try (PreparedStatement pst = c.prepareStatement("SELECT * FROM profissional ORDER BY nome");
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
            for (Profissional p : lista) {
                carregarServicos(c, p);
            }
        }
        return lista;
    }

    /** RF-08 / RN-10: profissionais ativos que fazem o serviço escolhido. */
    public List<Profissional> listarAtivosPorServico(int servicoId) throws SQLException {
        String sql = """
            SELECT p.* FROM profissional p
              JOIN profissional_servico ps ON ps.profissional_id = p.id
             WHERE ps.servico_id = ? AND p.ativo = 1
             ORDER BY p.nome""";
        List<Profissional> lista = new ArrayList<>();
        try (Connection c = Banco.conectar()) {
            try (PreparedStatement pst = c.prepareStatement(sql)) {
                pst.setInt(1, servicoId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        lista.add(montar(rs));
                    }
                }
            }
            for (Profissional p : lista) {
                carregarServicos(c, p);
            }
        }
        return lista;
    }

    public void vincularServico(int profissionalId, int servicoId) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(
                     "INSERT OR IGNORE INTO profissional_servico (profissional_id, servico_id) VALUES (?, ?)")) {
            pst.setInt(1, profissionalId);
            pst.setInt(2, servicoId);
            pst.executeUpdate();
        }
    }

    public void desvincularServico(int profissionalId, int servicoId) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(
                     "DELETE FROM profissional_servico WHERE profissional_id = ? AND servico_id = ?")) {
            pst.setInt(1, profissionalId);
            pst.setInt(2, servicoId);
            pst.executeUpdate();
        }
    }

    private void carregarServicos(Connection c, Profissional p) throws SQLException {
        try (PreparedStatement pst = c.prepareStatement(
                "SELECT servico_id FROM profissional_servico WHERE profissional_id = ?")) {
            pst.setInt(1, p.getId());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    p.adicionarServico(rs.getInt(1));
                }
            }
        }
    }

    private Profissional montar(ResultSet rs) throws SQLException {
        return new Profissional(rs.getInt("id"), rs.getString("nome"), rs.getInt("ativo") == 1);
    }
}
