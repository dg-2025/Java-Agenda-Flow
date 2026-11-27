package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.ServicoDAO;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/** Catálogo de serviços (RF-03, RF-07). */
public class ServicoService {

    private final ServicoDAO servicoDAO;

    public ServicoService(ServicoDAO servicoDAO) {
        this.servicoDAO = servicoDAO;
    }

    /** RF-03: cria (id = 0) ou edita um serviço. Novo serviço nasce ativo. */
    public Servico salvar(Usuario solicitante, Servico servico) throws SQLException {
        Permissao.exigirAdmin(solicitante);                         // RN-09
        validar(servico);
        servico.setNome(servico.getNome().trim());
        if (servico.getId() == 0) {
            servico.setAtivo(true);
            servicoDAO.inserir(servico);
        } else {
            servicoDAO.alterar(servico);
        }
        return servico;
    }

    /** RF-03: ativa/inativa. Inativo some da lista do cliente (RF-07). */
    public void alterarAtivo(Usuario solicitante, Servico servico, boolean ativo) throws SQLException {
        Permissao.exigirAdmin(solicitante);
        servico.setAtivo(ativo);
        servicoDAO.alterar(servico);
    }

    public List<Servico> listarTodos() throws SQLException {
        return servicoDAO.listar();
    }

    /** RF-07 / RN-10. */
    public List<Servico> listarAtivos() throws SQLException {
        return servicoDAO.listarAtivos();
    }

    public Servico buscar(int id) throws SQLException {
        return servicoDAO.buscarID(id);
    }

    /** RN-04: duração maior que 0 e valor maior ou igual a 0. */
    public static void validar(Servico s) {
        if (s == null || s.getNome() == null || s.getNome().isBlank()) {
            throw new RegraNegocioException("Informe o nome do serviço.");
        }
        if (s.getDuracaoMinutos() <= 0) {
            throw new RegraNegocioException("A duração do serviço deve ser maior que 0 minutos.");
        }
        if (s.getValor() < 0) {
            throw new RegraNegocioException("O valor do serviço não pode ser negativo.");
        }
    }
}
