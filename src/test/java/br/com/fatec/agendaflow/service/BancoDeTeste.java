package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.HorarioTrabalhoDAO;
import br.com.fatec.agendaflow.dao.ProfissionalDAO;
import br.com.fatec.agendaflow.dao.ServicoDAO;
import br.com.fatec.agendaflow.dao.UsuarioDAO;
import br.com.fatec.agendaflow.factory.UsuarioFactory;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.observer.AgendamentoEventos;
import br.com.fatec.agendaflow.persistencia.Banco;
import br.com.fatec.agendaflow.util.Senha;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base dos testes de integração: cada teste ganha um banco SQLite novo numa
 * pasta temporária ({@code @TempDir}), com os services ligados a ele.
 */
abstract class BancoDeTeste {

    /** "Agora" fixo dos testes: domingo, 06/01/2030 12:00. A segunda seguinte é 07/01/2030. */
    static final LocalDateTime AGORA = LocalDateTime.of(2030, 1, 6, 12, 0);
    static final LocalDate SEGUNDA = LocalDate.of(2030, 1, 7);

    @TempDir
    Path pasta;

    UsuarioDAO usuarioDAO;
    ServicoDAO servicoDAO;
    ProfissionalDAO profissionalDAO;
    HorarioTrabalhoDAO horarioDAO;
    AgendamentoDAO agendamentoDAO;
    AgendamentoEventos eventos;

    UsuarioService usuarioService;
    ServicoService servicoService;
    ProfissionalService profissionalService;
    AgendaService agendaService;
    AgendamentoService agendamentoService;

    @BeforeEach
    void criarBancoVazio() throws Exception {
        Banco.usarArquivo(pasta.resolve("teste.db"));
        Banco.criarEstrutura();

        usuarioDAO = new UsuarioDAO();
        servicoDAO = new ServicoDAO();
        profissionalDAO = new ProfissionalDAO();
        horarioDAO = new HorarioTrabalhoDAO();
        agendamentoDAO = new AgendamentoDAO();
        eventos = new AgendamentoEventos();

        usuarioService = new UsuarioService(usuarioDAO, agendamentoDAO);
        servicoService = new ServicoService(servicoDAO);
        profissionalService = new ProfissionalService(profissionalDAO, horarioDAO);
        agendaService = new AgendaService(horarioDAO, agendamentoDAO, () -> AGORA);
        agendamentoService = new AgendamentoService(agendamentoDAO, servicoDAO, profissionalDAO, agendaService, eventos);
    }

    Usuario criarAdmin() throws Exception {
        Usuario admin = UsuarioFactory.criar(Papel.ADMIN, 0, "Admin", "admin@teste.com", Senha.gerarHash("admin123"));
        usuarioDAO.inserir(admin);
        return admin;
    }

    Usuario criarCliente(String nome, String email) throws Exception {
        return usuarioService.cadastrarCliente(nome, email, "senha1234");
    }

    Servico criarServico(Usuario admin, String nome, int minutos, double valor) throws Exception {
        return servicoService.salvar(admin, new Servico(0, nome, "", minutos, valor, true));
    }

    /** Profissional vinculado ao serviço, trabalhando segunda das 08:00 às 12:00. */
    Profissional criarProfissionalSegundaManha(Usuario admin, String nome, Servico servico) throws Exception {
        Profissional p = profissionalService.salvar(admin, new Profissional(0, nome, true));
        profissionalService.definirVinculo(admin, p, servico.getId(), true);
        profissionalService.adicionarHorario(admin, new HorarioTrabalho(0, p.getId(), DayOfWeek.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(12, 0)));
        return p;
    }
}
