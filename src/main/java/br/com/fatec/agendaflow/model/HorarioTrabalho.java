package br.com.fatec.agendaflow.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

/** Expediente semanal fixo de um profissional em um dia da semana (RF-06). */
public class HorarioTrabalho {

    private int id;
    private int profissionalId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;

    public HorarioTrabalho() {
    }

    public HorarioTrabalho(int id, int profissionalId, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    /** O intervalo é válido quando o início vem antes do fim. */
    public boolean isIntervaloValido() {
        return horaInicio != null && horaFim != null && horaInicio.isBefore(horaFim);
    }

    /** RN-05: dois expedientes do mesmo dia se sobrepõem? */
    public boolean sobrepoe(HorarioTrabalho outro) {
        return diaSemana == outro.diaSemana
                && horaInicio.isBefore(outro.horaFim)
                && outro.horaInicio.isBefore(horaFim);
    }

    /** RN-06: o atendimento inteiro (início até início + duração) cabe neste expediente? */
    public boolean comporta(LocalTime inicio, LocalTime fim) {
        return !inicio.isBefore(horaInicio) && !fim.isAfter(horaFim) && inicio.isBefore(fim);
    }

    public String getDiaSemanaTexto() {
        String dia = diaSemana.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return Character.toUpperCase(dia.charAt(0)) + dia.substring(1);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProfissionalId() { return profissionalId; }
    public void setProfissionalId(int profissionalId) { this.profissionalId = profissionalId; }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    @Override
    public String toString() {
        return getDiaSemanaTexto() + " " + horaInicio + "–" + horaFim;
    }
}
