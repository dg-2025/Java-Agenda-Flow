package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.HorarioTrabalhoDAO;
import br.com.fatec.agendaflow.dao.ProfissionalDAO;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/** Profissionais, vínculos com serviços e horários de trabalho (RF-04, RF-05, RF-06, RF-08). */
public class ProfissionalService {

    private final ProfissionalDAO profissionalDAO;
    private final HorarioTrabalhoDAO horarioDAO;

    public ProfissionalService(ProfissionalDAO profissionalDAO, HorarioTrabalhoDAO horarioDAO) {
        this.profissionalDAO = profissionalDAO;
        this.horarioDAO = horarioDAO;
    }

    /** RF-04: cria (id = 0) ou edita. Novo profissional nasce ativo. */
    public Profissional salvar(Usuario solicitante, Profissional p) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        if (p == null || p.getNome() == null || p.getNome().trim().length() < 2) {
            throw new RegraNegocioException("Informe o nome do profissional.");
        }
        p.setNome(p.getNome().trim());
        if (p.getId() == 0) {
            p.setAtivo(true);
            profissionalDAO.inserir(p);
        } else {
            profissionalDAO.alterar(p);
        }
        return p;
    }

    public void alterarAtivo(Usuario solicitante, Profissional p, boolean ativo) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        p.setAtivo(ativo);
        profissionalDAO.alterar(p);
    }

    /** RF-05: marca/desmarca um serviço que o profissional realiza. */
    public void definirVinculo(Usuario solicitante, Profissional p, int servicoId, boolean vinculado) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        if (vinculado) {
            profissionalDAO.vincularServico(p.getId(), servicoId);
            p.adicionarServico(servicoId);
        } else {
            profissionalDAO.desvincularServico(p.getId(), servicoId);
            p.removerServico(servicoId);
        }
    }

    /** RF-06 + RN-05: cadastra um expediente sem sobrepor outro do mesmo dia. */
    public HorarioTrabalho adicionarHorario(Usuario solicitante, HorarioTrabalho novo) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        if (novo == null || novo.getDiaSemana() == null || !novo.isIntervaloValido()) {
            throw new RegraNegocioException("A hora de início deve ser anterior à hora de término.");
        }
        for (HorarioTrabalho existente : horarioDAO.listarPorProfissionalEDia(novo.getProfissionalId(), novo.getDiaSemana())) {
            if (existente.sobrepoe(novo)) {
                throw new RegraNegocioException("Esse horário se sobrepõe ao expediente já cadastrado ("
                        + existente.getHoraInicio() + "–" + existente.getHoraFim() + ").");
            }
        }
        horarioDAO.inserir(novo);
        return novo;
    }

    public void removerHorario(Usuario solicitante, HorarioTrabalho h) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        horarioDAO.remover(h);
    }

    public List<HorarioTrabalho> listarHorarios(int profissionalId) throws SQLException {
        return horarioDAO.listarPorProfissional(profissionalId);
    }

    public List<Profissional> listarTodos() throws SQLException {
        return profissionalDAO.listar();
    }

    /** RF-08 / RN-10: só ativos e vinculados ao serviço. */
    public List<Profissional> listarParaServico(int servicoId) throws SQLException {
        return profissionalDAO.listarAtivosPorServico(servicoId);
    }

    public Profissional buscar(int id) throws SQLException {
        return profissionalDAO.buscarID(id);
    }
}
