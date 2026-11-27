package br.com.fatec.agendaflow.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.Usuario;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Cadastros do administrador: serviços, profissionais, vínculos e horários (RF-03 a RF-08). */
@DisplayName("ServicoService e ProfissionalService — cadastros do admin")
class CadastrosAdminTest extends BancoDeTeste {

    private Usuario admin;

    @BeforeEach
    void entrarComoAdmin() throws Exception {
        admin = criarAdmin();
    }

    @Test
    @DisplayName("CT-05: serviço com duração 0 é rejeitado (RN-04)")
    void servicoComDuracaoZero() {
        assertThrows(RegraNegocioException.class,
                () -> servicoService.salvar(admin, new Servico(0, "Corte", "", 0, 50, true)));
        assertThrows(RegraNegocioException.class,
                () -> servicoService.salvar(admin, new Servico(0, "Corte", "", 30, -1, true)));
    }

    @Test
    @DisplayName("RF-07: serviço inativo some da lista do cliente")
    void servicoInativoSomeParaCliente() throws Exception {
        Servico s = criarServico(admin, "Escova", 45, 70);
        assertEquals(1, servicoService.listarAtivos().size());

        servicoService.alterarAtivo(admin, s, false);

        assertTrue(servicoService.listarAtivos().isEmpty());
        assertEquals(1, servicoService.listarTodos().size(), "continua na lista do admin");
    }

    @Test
    @DisplayName("CT-06: horário sobreposto para o mesmo profissional e dia é rejeitado (RN-05)")
    void horarioSobreposto() throws Exception {
        Profissional p = profissionalService.salvar(admin, new Profissional(0, "Ana", true));
        profissionalService.adicionarHorario(admin, new HorarioTrabalho(0, p.getId(), DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(12, 0)));

        assertThrows(RegraNegocioException.class, () -> profissionalService.adicionarHorario(admin,
                new HorarioTrabalho(0, p.getId(), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(14, 0))));

        // outro dia, ou logo depois do fim, pode
        profissionalService.adicionarHorario(admin, new HorarioTrabalho(0, p.getId(), DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(14, 0)));
        profissionalService.adicionarHorario(admin, new HorarioTrabalho(0, p.getId(), DayOfWeek.MONDAY,
                LocalTime.of(12, 0), LocalTime.of(14, 0)));
        assertEquals(3, profissionalService.listarHorarios(p.getId()).size());
    }

    @Test
    @DisplayName("RF-06: início depois do fim é rejeitado")
    void horarioInvertido() throws Exception {
        Profissional p = profissionalService.salvar(admin, new Profissional(0, "Ana", true));
        assertThrows(RegraNegocioException.class, () -> profissionalService.adicionarHorario(admin,
                new HorarioTrabalho(0, p.getId(), DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(10, 0))));
    }

    @Test
    @DisplayName("RF-05 / RF-08: vincular mostra o profissional para o serviço; desvincular esconde")
    void vinculoProfissionalServico() throws Exception {
        Servico corte = criarServico(admin, "Corte", 30, 50);
        Profissional p = profissionalService.salvar(admin, new Profissional(0, "Bruno", true));
        assertTrue(profissionalService.listarParaServico(corte.getId()).isEmpty());

        profissionalService.definirVinculo(admin, p, corte.getId(), true);
        assertEquals("Bruno", profissionalService.listarParaServico(corte.getId()).get(0).getNome());

        profissionalService.definirVinculo(admin, p, corte.getId(), false);
        assertTrue(profissionalService.listarParaServico(corte.getId()).isEmpty());
    }
}
