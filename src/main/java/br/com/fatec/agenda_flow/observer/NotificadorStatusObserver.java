package br.com.fatec.agenda_flow.observer;

import br.com.fatec.agenda_flow.Agendamento;
import br.com.fatec.agenda_flow.StatusAgendamento;

public class NotificadorStatusObserver implements AgendamentoObserver {
    @Override
    public void onStatusChanged(Agendamento agendamento, StatusAgendamento statusAntigo, StatusAgendamento statusNovo) {
        System.out.println("[NOTIFICACAO] Agendamento #" + agendamento.getId() + " mudou de " + statusAntigo + " para " + statusNovo);
    }
}
