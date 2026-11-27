package br.com.fatec.agendaflow.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.exception.AcessoNegadoException;
import br.com.fatec.agendaflow.exception.HorarioIndisponivelException;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.model.Usuario;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de integração do fluxo de agendamento (RF-09 a RF-15) com banco SQLite temporário. */
@DisplayName("AgendamentoService — criação, cancelamento e status")
class AgendamentoServiceTest extends BancoDeTeste {

    private Usuario admin;
    private Usuario cliente;
    private Servico corte;
    private Profissional ana;

    @BeforeEach
    void prepararCenario() throws Exception {
        admin = criarAdmin();
        cliente = criarCliente("Carlos", "carlos@teste.com");
        corte = criarServico(admin, "Corte", 60, 50);
        ana = criarProfissionalSegundaManha(admin, "Ana", corte);
        // CT-08: já existe um agendamento das 09:00 às 10:00
        agendamentoService.confirmar(criarCliente("Outro", "outro@teste.com"), corte.getId(), ana.getId(),
                SEGUNDA, LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("CT-08: a consulta no banco não oferece 09:00, mas oferece 08:00 e 10:00")
    void consultaDisponibilidadeComAgendamentoExistente() throws Exception {
        List<LocalTime> livres = agendaService.horariosDisponiveis(ana.getId(), 60, SEGUNDA);
        assertFalse(livres.contains(LocalTime.of(9, 0)));
        assertTrue(livres.contains(LocalTime.of(8, 0)));
        assertTrue(livres.contains(LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("CT-09: cria agendamento em horário livre com status Agendado")
    void criaAgendamentoEmHorarioLivre() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));

        assertTrue(ag.getId() > 0);
        assertEquals(StatusAgendamento.AGENDADO, ag.getStatus());
        assertEquals(LocalTime.of(11, 0), ag.getHoraFim());
        assertEquals(50.0, ag.getValor(), 0.001);
    }

    @Test
    @DisplayName("CT-10: tentativa no horário já ocupado é rejeitada")
    void rejeitaHorarioOcupado() {
        assertThrows(HorarioIndisponivelException.class, () ->
                agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("RNF-03: mesmo passando da regra, o gatilho do banco barra a sobreposição")
    void gatilhoDoBancoBarraSobreposicao() {
        Agendamento sobreposto = new Agendamento(cliente.getId(), ana.getId(), corte.getId(), SEGUNDA,
                LocalTime.of(9, 30), 60, 50);
        assertThrows(HorarioIndisponivelException.class, () -> agendamentoDAO.inserir(sobreposto));
    }

    @Test
    @DisplayName("CT-11: cancelar libera o horário de novo")
    void cancelamentoLiberaHorario() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));
        assertFalse(agendaService.horariosDisponiveis(ana.getId(), 60, SEGUNDA).contains(LocalTime.of(10, 0)));

        agendamentoService.cancelarPeloCliente(cliente, ag.getId());

        assertEquals(StatusAgendamento.CANCELADO, agendamentoDAO.buscarID(ag.getId()).getStatus());
        assertTrue(agendaService.horariosDisponiveis(ana.getId(), 60, SEGUNDA).contains(LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("CT-12: não é possível cancelar agendamento já concluído")
    void naoCancelaConcluido() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));
        agendamentoService.alterarStatus(admin, ag.getId(), StatusAgendamento.CONCLUIDO);

        assertThrows(RegraNegocioException.class, () -> agendamentoService.cancelarPeloCliente(cliente, ag.getId()));
    }

    @Test
    @DisplayName("RN-03: cliente não cancela agendamento de outro cliente")
    void naoCancelaDeOutroCliente() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));
        Usuario intruso = criarCliente("Intruso", "intruso@teste.com");

        assertThrows(AcessoNegadoException.class, () -> agendamentoService.cancelarPeloCliente(intruso, ag.getId()));
    }

    @Test
    @DisplayName("CT-13: admin conclui e o Observer avisa a mudança")
    void adminConcluiENotificaObservador() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));
        List<String> avisos = new ArrayList<>();
        eventos.inscrever((agendamento, anterior) -> avisos.add(anterior + "->" + agendamento.getStatus()));

        agendamentoService.alterarStatus(admin, ag.getId(), StatusAgendamento.CONCLUIDO);

        assertEquals(List.of("Agendado->Concluído"), avisos);
        Agendamento visaoDoCliente = agendamentoService.listarDoCliente(cliente).get(0);
        assertEquals(StatusAgendamento.CONCLUIDO, visaoDoCliente.getStatus(), "RF-12 reflete na hora");
    }

    @Test
    @DisplayName("RN-08: status finalizado não muda mais")
    void statusFinalizadoNaoMuda() throws Exception {
        Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));
        agendamentoService.alterarStatus(admin, ag.getId(), StatusAgendamento.FALTA);

        assertThrows(RegraNegocioException.class,
                () -> agendamentoService.alterarStatus(admin, ag.getId(), StatusAgendamento.CONCLUIDO));
    }

    @Test
    @DisplayName("CT-14: admin vê agendamentos de todos os clientes; cliente vê só os seus")
    void adminListaTodos() throws Exception {
        agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0));

        assertEquals(2, agendamentoService.listarTodos(admin, null).size());
        assertEquals(2, agendamentoService.listarTodos(admin, SEGUNDA).size());
        assertEquals(1, agendamentoService.listarDoCliente(cliente).size());
    }

    @Test
    @DisplayName("CT-15: cliente não acessa ações de administrador")
    void clienteNaoAcessaAreaAdmin() {
        assertThrows(AcessoNegadoException.class, () -> agendamentoService.listarTodos(cliente, null));
        assertThrows(AcessoNegadoException.class,
                () -> agendamentoService.alterarStatus(cliente, 1, StatusAgendamento.CONCLUIDO));
        assertThrows(AcessoNegadoException.class,
                () -> servicoService.salvar(cliente, new Servico(0, "Hack", "", 30, 10, true)));
    }

    @Test
    @DisplayName("RN-10: profissional inativo não pode ser agendado")
    void profissionalInativoNaoAgenda() throws Exception {
        profissionalService.alterarAtivo(admin, ana, false);

        assertThrows(RegraNegocioException.class, () ->
                agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, LocalTime.of(10, 0)));
        assertTrue(profissionalService.listarParaServico(corte.getId()).isEmpty());
    }

    @Test
    @DisplayName("RN-11 (Strategy): a partir do 3º atendimento concluído o cliente ganha 10% de desconto")
    void descontoFidelidadeAposTresConcluidos() throws Exception {
        LocalTime[] horas = {LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)};
        for (LocalTime h : horas) {
            Agendamento ag = agendamentoService.confirmar(cliente, corte.getId(), ana.getId(), SEGUNDA, h);
            assertEquals(50.0, ag.getValor(), 0.001, "ainda sem desconto");
            agendamentoService.alterarStatus(admin, ag.getId(), StatusAgendamento.CONCLUIDO);
        }

        AgendamentoService.Resumo resumo = agendamentoService.montarResumo(cliente, corte, ana,
                SEGUNDA.plusWeeks(1), LocalTime.of(8, 0));

        assertEquals(45.0, resumo.valorFinal(), 0.001);
        assertTrue(resumo.temDesconto());
        assertEquals("Desconto fidelidade 10%", resumo.regraPreco());
    }
}
