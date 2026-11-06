package br.com.fatec.agenda_flow.observer;

import br.com.fatec.agenda_flow.Agendamento;
import br.com.fatec.agenda_flow.StatusAgendamento;

public interface AgendamentoObserver {
    void onStatusChanged(Agendamento agendamento, StatusAgendamento statusAntigo, StatusAgendamento statusNovo);
}
