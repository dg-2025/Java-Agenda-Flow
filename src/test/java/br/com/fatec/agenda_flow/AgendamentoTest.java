package br.com.fatec.agenda_flow;

import br.com.fatec.agenda_flow.strategy.DescontoFidelidadeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgendamentoTest {

    private Agendamento agendamento;

    @BeforeEach
    void prepararCenario() {
        Cliente c = new Cliente(1, "Carlos", "carlos@email.com", "12345678");
        Profissional p = new Profissional(10, "Fernando");
        Servico s = new Servico(1, "Corte", "Corte masculino", 30, 50.0f, true);
        agendamento = new Agendamento(100, c, p, s, "2026-10-25", "10:00", "10:30");
    }

    @Test
    @DisplayName("Deve iniciar o agendamento com status AGENDADO e valor integral")
    void deveIniciarAgendadoComValorIntegral() {
        assertEquals(StatusAgendamento.AGENDADO, agendamento.getStatus());
        assertEquals(50.0f, agendamento.getValorFinal(), 0.001f);
    }

    @Test
    @DisplayName("Deve alterar status para CONCLUIDO e notificar o Observer")
    void deveConcluirENotificarObserver() {
        final boolean[] notificado = {false};
        agendamento.adicionarObserver((ag, antigo, novo) -> {
            if (antigo == StatusAgendamento.AGENDADO && novo == StatusAgendamento.CONCLUIDO) {
                notificado[0] = true;
            }
        });

        agendamento.concluir();

        assertEquals(StatusAgendamento.CONCLUIDO, agendamento.getStatus());
        assertTrue(notificado[0]);
    }

    @Test
    @DisplayName("Deve alterar status para CANCELADO ao cancelar")
    void deveCancelarAgendamento() {
        agendamento.cancelar();
        assertEquals(StatusAgendamento.CANCELADO, agendamento.getStatus());
    }

    @Test
    @DisplayName("Deve recalcular valor final ao trocar para Strategy de desconto")
    void deveRecalcularComStrategyDeDesconto() {
        agendamento.setEstrategiaPreco(new DescontoFidelidadeStrategy(20.0f));
        assertEquals(40.0f, agendamento.getValorFinal(), 0.001f);
    }
}
