package br.com.fatec.agendaflow.persistencia;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.HorarioTrabalhoDAO;
import br.com.fatec.agendaflow.dao.ProfissionalDAO;
import br.com.fatec.agendaflow.dao.ServicoDAO;
import br.com.fatec.agendaflow.dao.UsuarioDAO;
import br.com.fatec.agendaflow.factory.UsuarioFactory;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.util.Senha;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Dados de demonstração gravados no primeiro uso (premissa 2.4 da documentação).
 * Logins: admin@agendaflow.com / admin123 e cliente@agendaflow.com / cliente123.
 */
final class DadosIniciais {

    static final String EMAIL_ADMIN = "admin@agendaflow.com";

    private DadosIniciais() {
    }

    static void popularSeVazio() throws SQLException {
        UsuarioDAO usuarios = new UsuarioDAO();
        if (!usuarios.listar().isEmpty()) {
            return;
        }
        ServicoDAO servicos = new ServicoDAO();
        ProfissionalDAO profissionais = new ProfissionalDAO();
        HorarioTrabalhoDAO horarios = new HorarioTrabalhoDAO();
        AgendamentoDAO agendamentos = new AgendamentoDAO();

        Usuario admin = UsuarioFactory.criar(Papel.ADMIN, 0, "Administração", EMAIL_ADMIN, Senha.gerarHash("admin123"));
        Usuario daniel = UsuarioFactory.novoCliente("Daniel Gomes", "cliente@agendaflow.com", Senha.gerarHash("cliente123"));
        Usuario marina = UsuarioFactory.novoCliente("Marina Costa", "marina@agendaflow.com", Senha.gerarHash("marina123"));
        usuarios.inserir(admin);
        usuarios.inserir(daniel);
        usuarios.inserir(marina);

        Servico corteFem = servico(servicos, "Corte feminino", "Lavagem, corte e finalização", 60, 90);
        Servico corteMasc = servico(servicos, "Corte masculino", "Corte na tesoura ou máquina", 30, 50);
        Servico escova = servico(servicos, "Escova modelada", "Escova com finalização", 45, 70);
        Servico coloracao = servico(servicos, "Coloração", "Coloração completa com tratamento", 120, 220);
        Servico manicure = servico(servicos, "Manicure", "Cutilagem e esmaltação", 45, 40);
        Servico sobrancelha = servico(servicos, "Design de sobrancelha", "Design com pinça e linha", 30, 45);

        Set<DayOfWeek> segASab = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
        Set<DayOfWeek> terASab = EnumSet.range(DayOfWeek.TUESDAY, DayOfWeek.SATURDAY);
        Set<DayOfWeek> segASex = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

        Profissional ana = profissional(profissionais, "Ana Souza", corteFem, escova, coloracao);
        Profissional bruno = profissional(profissionais, "Bruno Lima", corteMasc, sobrancelha);
        Profissional carla = profissional(profissionais, "Carla Mendes", manicure, sobrancelha);

        expediente(horarios, ana, segASab, "09:00", "18:00");
        expediente(horarios, bruno, terASab, "10:00", "19:00");
        expediente(horarios, carla, segASex, "08:00", "12:00");
        expediente(horarios, carla, segASex, "13:00", "17:00");

        // Daniel já é cliente fiel: 3 atendimentos concluídos (RN-11 → ganha 10% de desconto)
        LocalDate hoje = LocalDate.now();
        LocalDate passado = hoje.minusDays(30);
        for (int i = 0; i < 3; i++) {
            passado = proximoDia(passado.plusDays(7), terASab);
            agendamento(agendamentos, daniel, bruno, corteMasc, passado, "10:00", StatusAgendamento.CONCLUIDO, 50);
        }
        LocalDate proximo = proximoDia(hoje.plusDays(1), segASab);
        agendamento(agendamentos, daniel, ana, escova, proximo, "14:00", StatusAgendamento.AGENDADO, 63);
        agendamento(agendamentos, marina, ana, corteFem, proximo, "10:00", StatusAgendamento.AGENDADO, 90);
        agendamento(agendamentos, marina, carla, manicure, proximoDia(hoje.plusDays(2), segASex), "09:00",
                StatusAgendamento.AGENDADO, 40);
        agendamento(agendamentos, marina, carla, manicure, proximoDia(hoje.minusDays(10), segASex), "08:00",
                StatusAgendamento.FALTA, 40);
    }

    private static Servico servico(ServicoDAO dao, String nome, String desc, int min, double valor) throws SQLException {
        Servico s = new Servico(0, nome, desc, min, valor, true);
        dao.inserir(s);
        return s;
    }

    private static Profissional profissional(ProfissionalDAO dao, String nome, Servico... servicos) throws SQLException {
        Profissional p = new Profissional(0, nome, true);
        dao.inserir(p);
        for (Servico s : servicos) {
            dao.vincularServico(p.getId(), s.getId());
            p.adicionarServico(s.getId());
        }
        return p;
    }

    private static void expediente(HorarioTrabalhoDAO dao, Profissional p, Set<DayOfWeek> dias,
                                   String inicio, String fim) throws SQLException {
        for (DayOfWeek dia : dias) {
            dao.inserir(new HorarioTrabalho(0, p.getId(), dia, LocalTime.parse(inicio), LocalTime.parse(fim)));
        }
    }

    private static void agendamento(AgendamentoDAO dao, Usuario cliente, Profissional p, Servico s, LocalDate data,
                                    String hora, StatusAgendamento status, double valor) throws SQLException {
        Agendamento a = new Agendamento(cliente.getId(), p.getId(), s.getId(), data, LocalTime.parse(hora),
                s.getDuracaoMinutos(), valor);
        a.setStatus(status);
        dao.inserir(a);
    }

    /** Primeiro dia a partir de {@code inicio} (inclusive) que cai num dos dias de expediente. */
    private static LocalDate proximoDia(LocalDate inicio, Set<DayOfWeek> dias) {
        LocalDate d = inicio;
        while (!dias.contains(d.getDayOfWeek())) {
            d = d.plusDays(1);
        }
        return d;
    }
}
