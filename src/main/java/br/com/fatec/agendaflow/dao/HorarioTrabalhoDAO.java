package br.com.fatec.agendaflow.dao;

import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.persistencia.Banco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HorarioTrabalhoDAO implements DAO<HorarioTrabalho> {

    @Override
    public boolean inserir(HorarioTrabalho dado) throws SQLException {
        String sql = "INSERT INTO horario_trabalho (profissional_id, dia_semana, hora_inicio, hora_fim) VALUES (?, ?, ?, ?)";
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, dado.getProfissionalId());
            pst.setInt(2, dado.getDiaSemana().getValue());
            pst.setString(3, dado.getHoraInicio().toString());
            pst.setString(4, dado.getHoraFim().toString());
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
    public boolean alterar(HorarioTrabalho dado) throws SQLException {
        String sql = "UPDATE horario_trabalho SET dia_semana = ?, hora_inicio = ?, hora_fim = ? WHERE id = ?";
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            pst.setInt(1, dado.getDiaSemana().getValue());
            pst.setString(2, dado.getHoraInicio().toString());
            pst.setString(3, dado.getHoraFim().toString());
            pst.setInt(4, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public boolean remover(HorarioTrabalho dado) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("DELETE FROM horario_trabalho WHERE id = ?")) {
            pst.setInt(1, dado.getId());
            return pst.executeUpdate() >= 1;
        }
    }

    @Override
    public HorarioTrabalho buscarID(int id) throws SQLException {
        try (Connection c = Banco.conectar();
             PreparedStatement pst = c.prepareStatement("SELECT * FROM horario_trabalho WHERE id = ?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    @Override
    public List<HorarioTrabalho> listar() throws SQLException {
        return consultar("SELECT * FROM horario_trabalho ORDER BY profissional_id, dia_semana, hora_inicio", null, null);
    }

    public List<HorarioTrabalho> listarPorProfissional(int profissionalId) throws SQLException {
        return consultar("SELECT * FROM horario_trabalho WHERE profissional_id = ? ORDER BY dia_semana, hora_inicio",
                profissionalId, null);
    }

    public List<HorarioTrabalho> listarPorProfissionalEDia(int profissionalId, DayOfWeek dia) throws SQLException {
        return consultar("SELECT * FROM horario_trabalho WHERE profissional_id = ? AND dia_semana = ? ORDER BY hora_inicio",
                profissionalId, dia.getValue());
    }

    private List<HorarioTrabalho> consultar(String sql, Integer p1, Integer p2) throws SQLException {
        List<HorarioTrabalho> lista = new ArrayList<>();
        try (Connection c = Banco.conectar(); PreparedStatement pst = c.prepareStatement(sql)) {
            if (p1 != null) pst.setInt(1, p1);
            if (p2 != null) pst.setInt(2, p2);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        }
        return lista;
    }

    private HorarioTrabalho montar(ResultSet rs) throws SQLException {
        return new HorarioTrabalho(rs.getInt("id"), rs.getInt("profissional_id"),
                DayOfWeek.of(rs.getInt("dia_semana")),
                LocalTime.parse(rs.getString("hora_inicio")), LocalTime.parse(rs.getString("hora_fim")));
    }
}
