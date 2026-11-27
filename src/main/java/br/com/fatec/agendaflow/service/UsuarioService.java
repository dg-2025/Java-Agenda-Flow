package br.com.fatec.agendaflow.service;

import br.com.fatec.agendaflow.dao.AgendamentoDAO;
import br.com.fatec.agendaflow.dao.UsuarioDAO;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.factory.UsuarioFactory;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.util.Senha;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

/** Cadastro, login e exclusão de conta (RF-01, RF-02, RNF-L04). */
public class UsuarioService {

    /** RN-01: no mínimo 8 caracteres, com pelo menos uma letra e um número. */
    private static final Pattern SENHA_VALIDA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioDAO usuarioDAO;
    private final AgendamentoDAO agendamentoDAO;

    public UsuarioService(UsuarioDAO usuarioDAO, AgendamentoDAO agendamentoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.agendamentoDAO = agendamentoDAO;
    }

    /** RF-01: cria um CLIENTE (o papel nunca vem da tela). */
    public Usuario cadastrarCliente(String nome, String email, String senha) throws SQLException {
        nome = nome == null ? "" : nome.trim();
        email = email == null ? "" : email.trim().toLowerCase();

        if (nome.length() < 2) {
            throw new RegraNegocioException("Informe o seu nome.");
        }
        if (!EMAIL_VALIDO.matcher(email).matches()) {
            throw new RegraNegocioException("Informe um e-mail válido.");
        }
        validarSenha(senha);
        if (usuarioDAO.existeEmail(email)) {                       // RN-02
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }

        Usuario novo = UsuarioFactory.novoCliente(nome, email, Senha.gerarHash(senha));
        usuarioDAO.inserir(novo);
        return novo;
    }

    /** RF-02: devolve o usuário (Cliente ou Administrador) se e-mail e senha conferem. */
    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario u = usuarioDAO.buscarPorEmail(email);
        if (u == null || !Senha.confere(senha, u.getSenhaHash())) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }
        return u;
    }

    /**
     * RNF-L04 (LGPD, art. 18): o cliente exclui a própria conta. Os agendamentos em
     * aberto são cancelados (liberando os horários) e os dados de identificação
     * (nome, e-mail, senha) são apagados. O histórico fica anônimo para o salão.
     */
    public void excluirConta(Usuario cliente, String senhaConfirmacao) throws SQLException {
        Permissao.exigirCliente(cliente);
        Usuario atual = usuarioDAO.buscarID(cliente.getId());
        if (atual == null) {
            throw new RegraNegocioException("Conta não encontrada.");
        }
        if (!Senha.confere(senhaConfirmacao, atual.getSenhaHash())) {
            throw new RegraNegocioException("Senha incorreta. A conta não foi excluída.");
        }
        agendamentoDAO.cancelarAbertosDoCliente(atual.getId());
        atual.setNome("Conta excluída");
        atual.setEmail("excluido-" + atual.getId() + "@agendaflow.local");
        atual.setSenhaHash(Senha.gerarHash(UUID.randomUUID().toString()));
        usuarioDAO.alterar(atual);
    }

    public static void validarSenha(String senha) {
        if (senha == null || !SENHA_VALIDA.matcher(senha).matches()) {
            throw new RegraNegocioException("A senha precisa ter no mínimo 8 caracteres, com letras e números.");
        }
    }
}
