package br.com.fatec.agendaflow.model;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.exception.RegraNegocioException;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Agendamento — regras da entidade")
class AgendamentoTest {

    private Agendamento ag;

    @BeforeEach
    void criar() {
        ag = new Agendamento(1, 1, 1, LocalDate.of(2030, 1, 7), LocalTime.of(9, 0), 60, 50);
    }

    @Test
    @DisplayName("Nasce Agendado e calcula a hora de fim pela duração")
    void estadoInicial() {
        assertEquals(StatusAgendamento.AGENDADO, ag.getStatus());
        assertEquals(LocalTime.of(10, 0), ag.getHoraFim());
    }

    @Test
    @DisplayName("Conflito: intervalos que se encostam não conflitam; os que se cruzam sim")
    void conflito() {
        assertTrue(ag.conflitaCom(LocalTime.of(9, 30), LocalTime.of(10, 30)));
        assertTrue(ag.conflitaCom(LocalTime.of(8, 30), LocalTime.of(9, 30)));
        assertFalse(ag.conflitaCom(LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertFalse(ag.conflitaCom(LocalTime.of(8, 0), LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("RN-07: cancelado não conflita com nada")
    void canceladoNaoConflita() {
        ag.cancelar();
        assertFalse(ag.conflitaCom(LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("RN-08: depois de finalizado, não muda mais")
    void finalizadoNaoMuda() {
        ag.registrarFalta();
        assertEquals(StatusAgendamento.FALTA, ag.getStatus());
        assertThrows(RegraNegocioException.class, ag::concluir);
        assertThrows(RegraNegocioException.class, ag::cancelar);
    }
}
