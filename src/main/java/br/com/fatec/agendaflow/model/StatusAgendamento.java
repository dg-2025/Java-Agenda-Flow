package br.com.fatec.agendaflow.model;

/** Situação de um agendamento ao longo do seu ciclo de vida. */
public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado"),
    FALTA("Falta do cliente");

    private final String texto;

    StatusAgendamento(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    /** RN-08: depois de concluído, cancelado ou falta, o status não muda mais. */
    public boolean isFinalizado() {
        return this != AGENDADO;
    }

    /** RN-06 / RN-07: só "Agendado" e "Concluído" ocupam o horário do profissional. */
    public boolean ocupaHorario() {
        return this == AGENDADO || this == CONCLUIDO;
    }

    @Override
    public String toString() {
        return texto;
    }
}
