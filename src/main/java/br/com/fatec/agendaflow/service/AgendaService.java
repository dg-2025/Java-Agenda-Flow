package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.HorarioTrabalhoDAO;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;

/** RF-09: cálculo dos horários de início disponíveis. */
public class AgendaService {

    /** De quanto em quanto tempo os horários de início são oferecidos. */
    public static final int INTERVALO_MINUTOS = 30;

    private final HorarioTrabalhoDAO horarioDAO;
    private final AgendamentoDAO agendamentoDAO;
    private final Supplier<LocalDateTime> relogio;

    public AgendaService(HorarioTrabalhoDAO horarioDAO, AgendamentoDAO agendamentoDAO) {
        this(horarioDAO, agendamentoDAO, LocalDateTime::now);
    }

    /** Construtor com relógio substituível: os testes fixam o "agora". */
    public AgendaService(HorarioTrabalhoDAO horarioDAO, AgendamentoDAO agendamentoDAO, Supplier<LocalDateTime> relogio) {
        this.horarioDAO = horarioDAO;
        this.agendamentoDAO = agendamentoDAO;
        this.relogio = relogio;
    }

    /**
     * Fluxo do RF-09: expediente do dia da semana → duração do serviço →
     * agendamentos do dia → horários em que o atendimento inteiro cabe.
     * Datas passadas e horários que já passaram hoje não são oferecidos.
     */
    public List<LocalTime> horariosDisponiveis(int profissionalId, int duracaoMinutos, LocalDate data) throws SQLException {
        LocalDateTime agora = relogio.get();
        if (data == null || data.isBefore(agora.toLocalDate())) {
            return List.of();
        }
        List<HorarioTrabalho> expedientes = horarioDAO.listarPorProfissionalEDia(profissionalId, data.getDayOfWeek());
        List<Agendamento> doDia = agendamentoDAO.listarPorProfissionalEData(profissionalId, data);

        List<LocalTime> livres = calcularHorariosLivres(expedientes, duracaoMinutos, doDia, INTERVALO_MINUTOS);
        if (data.equals(agora.toLocalDate())) {
            LocalTime horaAtual = agora.toLocalTime();
            livres.removeIf(h -> !h.isAfter(horaAtual));
        }
        return livres;
    }

    public boolean estaDisponivel(int profissionalId, int duracaoMinutos, LocalDate data, LocalTime inicio) throws SQLException {
        return horariosDisponiveis(profissionalId, duracaoMinutos, data).contains(inicio);
    }

    /**
     * Regra pura (sem banco), coberta por testes unitários — RN-06 e RN-07.
     * <ul>
     *   <li>o intervalo [início, início + duração] precisa caber inteiro no expediente;</li>
     *   <li>não pode bater com agendamento "Agendado" ou "Concluído";</li>
     *   <li>agendamento "Cancelado" (ou "Falta") não bloqueia.</li>
     * </ul>
     */
    public static List<LocalTime> calcularHorariosLivres(List<HorarioTrabalho> expedientes, int duracaoMinutos,
                                                         List<Agendamento> agendamentos, int intervaloMinutos) {
        TreeSet<LocalTime> livres = new TreeSet<>();
        if (duracaoMinutos <= 0 || intervaloMinutos <= 0) {
            return new ArrayList<>();
        }
        for (HorarioTrabalho exp : expedientes) {
            LocalTime inicio = exp.getHoraInicio();
            while (true) {
                LocalTime fim = inicio.plusMinutes(duracaoMinutos);
                if (fim.isBefore(inicio) || !exp.comporta(inicio, fim)) {
                    break; // passou da meia-noite ou do fim do expediente
                }
                final LocalTime ini = inicio;
                boolean ocupado = agendamentos.stream().anyMatch(a -> a.conflitaCom(ini, fim));
                if (!ocupado) {
                    livres.add(inicio);
                }
                LocalTime proximo = inicio.plusMinutes(intervaloMinutos);
                if (!proximo.isAfter(inicio)) {
                    break;
                }
                inicio = proximo;
            }
        }
        return new ArrayList<>(livres);
    }
}
