package br.com.fatec.agendaflow.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Testes unitários da regra pura de disponibilidade (RF-09, RN-06, RN-07).
 * Não usam banco: os expedientes e agendamentos são montados na mão.
 */
@DisplayName("AgendaService — cálculo de horários disponíveis")
class AgendaServiceTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2030, 1, 7);
    private List<HorarioTrabalho> expedienteManha;

    @BeforeEach
    void prepararExpediente() {
        // Profissional trabalha de 08:00 às 12:00 na segunda-feira
        expedienteManha = List.of(new HorarioTrabalho(1, 1, DayOfWeek.MONDAY, t("08:00"), t("12:00")));
    }

    @Test
    @DisplayName("CT-07: sem expediente cadastrado no dia, a lista vem vazia")
    void semExpedienteListaVazia() {
        List<LocalTime> livres = AgendaService.calcularHorariosLivres(List.of(), 60, List.of(), 30);
        assertTrue(livres.isEmpty());
    }

    @Test
    @DisplayName("CT-08: agendamento 09:00–10:00 tira o 09:00; 08:00 e 10:00 continuam livres")
    void agendamentoExistenteBloqueiaHorario() {
        Agendamento existente = agendamento("09:00", 60, StatusAgendamento.AGENDADO);

        List<LocalTime> livres = AgendaService.calcularHorariosLivres(expedienteManha, 60, List.of(existente), 30);

        assertFalse(livres.contains(t("09:00")));
        assertFalse(livres.contains(t("08:30")), "08:30–09:30 também bate no 09:00");
        assertFalse(livres.contains(t("09:30")), "09:30–10:30 começa dentro do agendamento");
        assertTrue(livres.contains(t("08:00")));
        assertTrue(livres.contains(t("10:00")));
        assertEquals(List.of(t("08:00"), t("10:00"), t("10:30"), t("11:00")), livres);
    }

    @Test
    @DisplayName("RN-06: o serviço precisa caber inteiro antes do fim do expediente")
    void naoUltrapassaFimDoExpediente() {
        List<LocalTime> livres = AgendaService.calcularHorariosLivres(expedienteManha, 90, List.of(), 30);

        assertEquals(t("10:30"), livres.get(livres.size() - 1), "10:30 + 90 min = 12:00, o último possível");
        assertFalse(livres.contains(t("11:00")));
    }

    @Test
    @DisplayName("RN-07: agendamento cancelado não bloqueia o horário")
    void canceladoLiberaHorario() {
        Agendamento cancelado = agendamento("09:00", 60, StatusAgendamento.CANCELADO);

        List<LocalTime> livres = AgendaService.calcularHorariosLivres(expedienteManha, 60, List.of(cancelado), 30);

        assertTrue(livres.contains(t("09:00")));
    }

    @Test
    @DisplayName("RN-06: agendamento concluído continua ocupando o horário")
    void concluidoContinuaOcupando() {
        Agendamento concluido = agendamento("09:00", 60, StatusAgendamento.CONCLUIDO);

        List<LocalTime> livres = AgendaService.calcularHorariosLivres(expedienteManha, 60, List.of(concluido), 30);

        assertFalse(livres.contains(t("09:00")));
    }

    @Test
    @DisplayName("Dois expedientes no mesmo dia (com almoço) geram horários nos dois blocos")
    void doisExpedientesNoMesmoDia() {
        List<HorarioTrabalho> comAlmoco = List.of(
                new HorarioTrabalho(1, 1, DayOfWeek.MONDAY, t("08:00"), t("12:00")),
                new HorarioTrabalho(2, 1, DayOfWeek.MONDAY, t("13:00"), t("17:00")));

        List<LocalTime> livres = AgendaService.calcularHorariosLivres(comAlmoco, 60, List.of(), 60);

        assertEquals(List.of(t("08:00"), t("09:00"), t("10:00"), t("11:00"),
                t("13:00"), t("14:00"), t("15:00"), t("16:00")), livres);
    }

    @ParameterizedTest(name = "serviço de {0} min → {1} horários de início")
    @CsvSource({"30, 8", "60, 7", "120, 5", "240, 1", "241, 0"})
    @DisplayName("Quantidade de horários conforme a duração (expediente de 4 h, passo de 30 min)")
    void quantidadeConformeDuracao(int duracao, int esperado) {
        assertEquals(esperado, AgendaService.calcularHorariosLivres(expedienteManha, duracao, List.of(), 30).size());
    }

    private static Agendamento agendamento(String inicio, int duracao, StatusAgendamento status) {
        Agendamento a = new Agendamento(1, 1, 1, SEGUNDA, t(inicio), duracao, 50);
        a.setStatus(status);
        return a;
    }

    private static LocalTime t(String hora) {
        return LocalTime.parse(hora);
    }
}
