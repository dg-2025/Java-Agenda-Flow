package br.com.fatec.agendaflow.dao;

import br.com.fatec.agendaflow.exception.HorarioIndisponivelException;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.persistencia.Banco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoDAO implements DAO<Agendamento> {

    /** Consulta base com os nomes de cliente, profissional e serviço para as telas. */
    private static final String SELECT = """
        SELECT a.*, u.nome AS cliente_nome, p.nome AS profissional_nome, s.nome AS servico_nome
          FROM agendamento a
          JOIN usuario u      ON u.id = a.usuario_id
          JOIN profissional p ON p.id = a.profissional_id
          JOIN servico s      ON s.id = a.servico_id
        """;

    @Override
    public boolean inserir(Agendamento dado) throws SQLException {
        String sql = """
            INSERT INTO agendamento (usuario_id, profissional_id, servico_id, data, hora_inicio,
                                     hora_fim, status, valor, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, dado.getClienteId());
            pst.setInt(2, dado.getProfissionalId());
            pst.setInt(3, dado.getServicoId());
            pst.setString(4, dado.getData().toString());
            pst.setString(5, dado.getHoraInicio().toString());
            pst.setString(6, dado.getHoraFim().toString());
            pst.setString(7, dado.getStatus().name());
            pst.setDouble(8, dado.getValor());
            pst.setString(9, (dado.getDataCriacao() == null ? LocalDateTime.now() : dado.getDataCriacao()).toString());
            if (pst.executeUpdate() < 1) {
                return false;
            }
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    dado.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            // gatilho trg_agendamento_sem_conflito (RNF-03)
            if (e.getMessage() != null && e.getMessage().contains("CONFLITO_HORARIO")) {
                throw new HorarioIndisponivelException(
                        "Esse horário acabou de ser ocupado. Escolha outro horário.");
            }
            throw e;
        }
    }

    /** Só o status muda depois de criado (não há reagendamento — fora do escopo). */
    @Override
    public boolean alterar(Agendamento dado) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("UPDATE agendamento SET status = ? WHERE id = ?")) {
            pst.setString(1, dado.getStatus().name());
            pst.setInt(2, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public boolean remover(Agendamento dado) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("DELETE FROM agendamento WHERE id = ?")) {
            pst.setInt(1, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public Agendamento buscarID(int id) throws SQLException {
        List<Agendamento> r = consultar(SELECT + " WHERE a.id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    /** RF-14: todos os agendamentos. */
    @Override
    public List<Agendamento> listar() throws SQLException {
        return consultar(SELECT + " ORDER BY a.data DESC, a.hora_inicio DESC");
    }

    /** RF-14: filtro por data. */
    public List<Agendamento> listarPorData(LocalDate data) throws SQLException {
        return consultar(SELECT + " WHERE a.data = ? ORDER BY a.hora_inicio", data.toString());
    }

    /** RF-12: só os do cliente (RN-03). */
    public List<Agendamento> listarPorCliente(int clienteId) throws SQLException {
        return consultar(SELECT + " WHERE a.usuario_id = ? ORDER BY a.data DESC, a.hora_inicio DESC", clienteId);
    }

    /** RF-09: agendamentos do profissional naquele dia (todos os status; quem filtra é a regra). */
    public List<Agendamento> listarPorProfissionalEData(int profissionalId, LocalDate data) throws SQLException {
        return consultar(SELECT + " WHERE a.profissional_id = ? AND a.data = ? ORDER BY a.hora_inicio",
                profissionalId, data.toString());
    }

    /** RN-11: quantos atendimentos o cliente já concluiu. */
    public int contarConcluidos(int clienteId) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(
                     "SELECT COUNT(*) FROM agendamento WHERE usuario_id = ? AND status = 'CONCLUIDO'")) {
            pst.setInt(1, clienteId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** RNF-L04: ao excluir a conta, agendamentos futuros em aberto são cancelados. */
    public int cancelarAbertosDoCliente(int clienteId) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(
                     "UPDATE agendamento SET status = 'CANCELADO' WHERE usuario_id = ? AND status = 'AGENDADO'")) {
            pst.setInt(1, clienteId);
            return pst.executeUpdate();
        }
    }

    private List<Agendamento> consultar(String sql, Object... parametros) throws SQLException {
        List<Agendamento> lista = new ArrayList<>();
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                pst.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    private Agendamento montar(ResultSet rs) throws SQLException {
        Agendamento a = new Agendamento();
        a.setId(rs.getInt("id"));
        a.setClienteId(rs.getInt("usuario_id"));
        a.setProfissionalId(rs.getInt("profissional_id"));
        a.setServicoId(rs.getInt("servico_id"));
        a.setData(LocalDate.parse(rs.getString("data")));
        a.setHoraInicio(LocalTime.parse(rs.getString("hora_inicio")));
        a.setHoraFim(LocalTime.parse(rs.getString("hora_fim")));
        a.setStatus(StatusAgendamento.valueOf(rs.getString("status")));
        a.setValor(rs.getDouble("valor"));
        a.setDataCriacao(LocalDateTime.parse(rs.getString("data_criacao")));
        a.setClienteNome(rs.getString("cliente_nome"));
        a.setProfissionalNome(rs.getString("profissional_nome"));
        a.setServicoNome(rs.getString("servico_nome"));
        return a;
    }
}
