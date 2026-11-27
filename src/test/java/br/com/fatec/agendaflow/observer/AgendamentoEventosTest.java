package br.com.fatec.agendaflow.observer;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("AgendamentoEventos — padrão Observer")
class AgendamentoEventosTest {

    private AgendamentoEventos eventos;
    private Agendamento ag;

    @BeforeEach
    void preparar() {
        eventos = new AgendamentoEventos();
        ag = new Agendamento(1, 1, 1, LocalDate.of(2030, 1, 7), LocalTime.of(9, 0), 60, 50);
        ag.setId(42);
    }

    @Test
    @DisplayName("Todos os observadores inscritos são avisados")
    void notificaTodos() {
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        eventos.inscrever((x, anterior) -> a.add("A"));
        eventos.inscrever((x, anterior) -> b.add("B"));

        eventos.notificar(ag, StatusAgendamento.AGENDADO);

        assertEquals(List.of("A"), a);
        assertEquals(List.of("B"), b);
    }

    @Test
    @DisplayName("Observador removido não recebe mais avisos")
    void removido() {
        List<String> avisos = new ArrayList<>();
        AgendamentoObserver o = (x, anterior) -> avisos.add("x");
        eventos.inscrever(o);
        eventos.remover(o);

        eventos.notificar(ag, null);

        assertTrue(avisos.isEmpty());
        assertEquals(0, eventos.quantidade());
    }

    @Test
    @DisplayName("Um observador com erro não impede os outros")
    void erroIsolado() {
        List<String> avisos = new ArrayList<>();
        eventos.inscrever((x, anterior) -> { throw new IllegalStateException("falhou"); });
        eventos.inscrever((x, anterior) -> avisos.add("ok"));

        assertDoesNotThrow(() -> eventos.notificar(ag, null));
        assertEquals(List.of("ok"), avisos);
    }

    @Test
    @DisplayName("HistoricoArquivoObserver grava uma linha por evento")
    void historicoEmArquivo(@TempDir Path pasta) throws Exception {
        Path log = pasta.resolve("historico.log");
        eventos.inscrever(new HistoricoArquivoObserver(log));

        eventos.notificar(ag, null);
        ag.concluir();
        eventos.notificar(ag, StatusAgendamento.AGENDADO);

        List<String> linhas = Files.readAllLines(log);
        assertEquals(2, linhas.size());
        assertTrue(linhas.get(0).contains("#42") && linhas.get(0).contains("CRIADO"));
        assertTrue(linhas.get(1).contains("Agendado -> Concluído"));
    }
}
