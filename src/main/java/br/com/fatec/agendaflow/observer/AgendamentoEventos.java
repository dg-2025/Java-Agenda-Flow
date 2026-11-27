package br.com.fatec.agendaflow.observer;

import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Padrão GoF <b>Observer</b> — o "sujeito" (publicador).
 * <p>
 * O {@code AgendamentoService} chama {@link #notificar} depois de gravar a
 * mudança no banco. Os observadores inscritos (log em arquivo e as telas
 * abertas) reagem cada um do seu jeito. É assim que a alteração feita pelo
 * admin aparece na hora para o cliente (critério de aceite do RF-15).
 */
public class AgendamentoEventos {

    private final List<AgendamentoObserver> observadores = new CopyOnWriteArrayList<>();

    public void inscrever(AgendamentoObserver observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void remover(AgendamentoObserver observador) {
        observadores.remove(observador);
    }

    public void notificar(Agendamento agendamento, StatusAgendamento statusAnterior) {
        for (AgendamentoObserver o : observadores) {
            try {
                o.atualizar(agendamento, statusAnterior);
            } catch (RuntimeException e) {
                // um observador com problema não pode derrubar a operação nem os outros
                System.err.println("Observador falhou: " + e.getMessage());
            }
        }
    }

    public int quantidade() {
        return observadores.size();
    }
}
