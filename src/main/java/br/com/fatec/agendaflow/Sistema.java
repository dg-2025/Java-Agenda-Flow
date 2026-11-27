package br.com.fatec.agendaflow;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.HorarioTrabalhoDAO;
import br.com.fatec.agendaflow.dao.ProfissionalDAO;
import br.com.fatec.agendaflow.dao.ServicoDAO;
import br.com.fatec.agendaflow.dao.UsuarioDAO;
import br.com.fatec.agendaflow.observer.AgendamentoEventos;
import br.com.fatec.agendaflow.observer.HistoricoArquivoObserver;
import br.com.fatec.agendaflow.persistencia.Banco;
import br.com.fatec.agendaflow.service.AgendaService;
import br.com.fatec.agendaflow.service.AgendamentoService;
import br.com.fatec.agendaflow.service.ProfissionalService;
import br.com.fatec.agendaflow.service.ServicoService;
import br.com.fatec.agendaflow.service.UsuarioService;

/**
 * Monta as camadas uma vez só (DAO → Service) e entrega os services às telas.
 * Todas as janelas abertas compartilham o mesmo publicador de eventos: é por
 * ele que uma mudança feita numa janela aparece na outra (Observer).
 */
public final class Sistema {

    public static final AgendamentoEventos EVENTOS = new AgendamentoEventos();

    public static final UsuarioService USUARIOS;
    public static final ServicoService SERVICOS;
    public static final ProfissionalService PROFISSIONAIS;
    public static final AgendaService AGENDA;
    public static final AgendamentoService AGENDAMENTOS;

    static {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        ServicoDAO servicoDAO = new ServicoDAO();
        ProfissionalDAO profissionalDAO = new ProfissionalDAO();
        HorarioTrabalhoDAO horarioDAO = new HorarioTrabalhoDAO();
        AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

        USUARIOS = new UsuarioService(usuarioDAO, agendamentoDAO);
        SERVICOS = new ServicoService(servicoDAO);
        PROFISSIONAIS = new ProfissionalService(profissionalDAO, horarioDAO);
        AGENDA = new AgendaService(horarioDAO, agendamentoDAO);
        AGENDAMENTOS = new AgendamentoService(agendamentoDAO, servicoDAO, profissionalDAO, AGENDA, EVENTOS);

        // Observador fixo: trilha de auditoria em arquivo, ao lado do banco
        EVENTOS.inscrever(new HistoricoArquivoObserver(
                Banco.getArquivo().toAbsolutePath().getParent().resolve("historico.log")));
    }

    private Sistema() {
    }
}
