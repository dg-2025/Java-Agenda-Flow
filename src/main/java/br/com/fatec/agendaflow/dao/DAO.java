package br.com.fatec.agendaflow.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato genérico de acesso a dados, o mesmo da interface {@code DAO<MODEL>}
 * vista nas aulas: cada tabela tem um DAO que implementa estas operações.
 */
public interface DAO<MODEL> {

    boolean inserir(MODEL dado) throws SQLException;

    boolean alterar(MODEL dado) throws SQLException;

    boolean remover(MODEL dado) throws SQLException;

    MODEL buscarID(int id) throws SQLException;

    List<MODEL> listar() throws SQLException;
}
