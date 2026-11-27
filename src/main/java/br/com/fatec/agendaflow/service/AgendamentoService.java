package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.ProfissionalDAO;
import br.com.fatec.agendaflow.dao.ServicoDAO;
import br.com.fatec.agendaflow.exception.AcessoNegadoException;
import br.com.fatec.agendaflow.exception.HorarioIndisponivelException;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.observer.AgendamentoEventos;
import br.com.fatec.agendaflow.strategy.CalculoPrecoStrategy;
import br.com.fatec.agendaflow.strategy.SeletorPrecoStrategy;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Criação, cancelamento e mudança de status (RF-10 a RF-15).
 * <p>
 * É o <b>contexto</b> do Strategy (escolhe como calcular o preço) e o ponto que
 * dispara o <b>Observer</b> (avisa quem estiver ouvindo depois de cada mudança).
 */
public class AgendamentoService {

    /** Resumo exibido antes da confirmação (RF-10). */
    public record Resumo(Servico servico, Profissional profissional, LocalDate data, LocalTime inicio,
                         LocalTime fim, double valorServico, double valorFinal, String regraPreco) {
        public boolean temDesconto() {
            return valorFinal < valorServico;
        }
    }

    private final AgendamentoDAO agendamentoDAO;
    private final ServicoDAO servicoDAO;
    private final ProfissionalDAO profissionalDAO;
    private final AgendaService agendaService;
    private final AgendamentoEventos eventos;

    public AgendamentoService(AgendamentoDAO agendamentoDAO, ServicoDAO servicoDAO, ProfissionalDAO profissionalDAO,
                              AgendaService agendaService, AgendamentoEventos eventos) {
        this.agendamentoDAO = agendamentoDAO;
        this.servicoDAO = servicoDAO;
        this.profissionalDAO = profissionalDAO;
        this.agendaService = agendaService;
        this.eventos = eventos;
    }

    public AgendamentoEventos getEventos() {
        return eventos;
    }

    /** Strategy: a regra de preço depende do histórico do cliente (RN-11). */
    public CalculoPrecoStrategy estrategiaPara(Usuario cliente) throws SQLException {
        return SeletorPrecoStrategy.para(agendamentoDAO.contarConcluidos(cliente.getId()));
    }

    /** RF-10, passo 1: monta o resumo. Não grava nada. */
    public Resumo montarResumo(Usuario cliente, Servico servico, Profissional profissional,
                               LocalDate data, LocalTime inicio) throws SQLException {
        CalculoPrecoStrategy estrategia = estrategiaPara(cliente);
        return new Resumo(servico, profissional, data, inicio, inicio.plusMinutes(servico.getDuracaoMinutos()),
                servico.getValor(), estrategia.calcular(servico.getValor()), estrategia.descricao());
    }

    /**
     * RF-10, passos 2 e 3 + RF-11: revalida tudo e cria o agendamento como "Agendado".
     * {@code synchronized} + gatilho no banco garantem que dois pedidos para o
     * mesmo horário resultem em um único agendamento (RNF-03).
     */
    public synchronized Agendamento confirmar(Usuario cliente, int servicoId, int profissionalId,
                                              LocalDate data, LocalTime inicio) throws SQLException {
        Permissao.exigirCliente(cliente);

        Servico servico = servicoDAO.buscarID(servicoId);
        if (servico == null || !servico.isAtivo()) {                              // RN-10
            throw new RegraNegocioException("Este serviço não está mais disponível.");
        }
        Profissional profissional = profissionalDAO.buscarID(profissionalId);
        if (profissional == null || !profissional.isAtivo() || !profissional.atendeServico(servicoId)) {
            throw new RegraNegocioException("Este profissional não está disponível para o serviço escolhido.");
        }
        if (!agendaService.estaDisponivel(profissionalId, servico.getDuracaoMinutos(), data, inicio)) {   // RN-06
            throw new HorarioIndisponivelException("O horário " + inicio + " não está mais disponível. Escolha outro.");
        }

        double valor = estrategiaPara(cliente).calcular(servico.getValor());
        Agendamento ag = new Agendamento(cliente.getId(), profissionalId, servicoId, data, inicio,
                servico.getDuracaoMinutos(), valor);
        agendamentoDAO.inserir(ag);
        ag.setClienteNome(cliente.getNome());
        ag.setProfissionalNome(profissional.getNome());
        ag.setServicoNome(servico.getNome());

        eventos.notificar(ag, null);
        return ag;
    }

    /** RF-12. */
    public List<Agendamento> listarDoCliente(Usuario cliente) throws SQLException {
        Permissao.exigirCliente(cliente);
        return agendamentoDAO.listarPorCliente(cliente.getId());
    }

    /** RF-14: todos, com filtro opcional por data. */
    public List<Agendamento> listarTodos(Usuario admin, LocalDate filtroData) throws SQLException {
        Permissao.exigirAdmin(admin);
        return filtroData == null ? agendamentoDAO.listar() : agendamentoDAO.listarPorData(filtroData);
    }

    /** RF-13: o cliente cancela um agendamento próprio ainda "Agendado". */
    public Agendamento cancelarPeloCliente(Usuario cliente, int agendamentoId) throws SQLException {
        Permissao.exigirCliente(cliente);
        Agendamento ag = buscarExistente(agendamentoId);
        if (ag.getClienteId() != cliente.getId()) {                                // RN-03
            throw new AcessoNegadoException("Você só pode cancelar os seus próprios agendamentos.");
        }
        return mudarStatus(ag, StatusAgendamento.CANCELADO);
    }

    /** RF-15: o admin conclui, cancela ou registra falta. */
    public Agendamento alterarStatus(Usuario admin, int agendamentoId, StatusAgendamento novo) throws SQLException {
        Permissao.exigirAdmin(admin);                                              // RN-09
        return mudarStatus(buscarExistente(agendamentoId), novo);
    }

    private Agendamento mudarStatus(Agendamento ag, StatusAgendamento novo) throws SQLException {
        StatusAgendamento anterior = ag.getStatus();
        ag.alterarStatus(novo);                                                    // RN-08
        agendamentoDAO.alterar(ag);
        eventos.notificar(ag, anterior);
        return ag;
    }

    private Agendamento buscarExistente(int id) throws SQLException {
        Agendamento ag = agendamentoDAO.buscarID(id);
        if (ag == null) {
            throw new RegraNegocioException("Agendamento não encontrado.");
        }
        return ag;
    }
}
