package br.com.fatec.agendaflow.observer;

import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;

/**
 * Padrão GoF <b>Observer</b> (comportamental) — o "observador".
 * <p>
 * Quem implementa é avisado sempre que um agendamento é criado ou muda de
 * status, sem que o serviço de agendamento precise conhecer quem está ouvindo.
 */
@FunctionalInterface
public interface AgendamentoObserver {

    /**
     * @param agendamento     agendamento já com o status novo
     * @param statusAnterior  status antes da mudança; {@code null} quando o agendamento acabou de ser criado
     */
    void atualizar(Agendamento agendamento, StatusAgendamento statusAnterior);
}
