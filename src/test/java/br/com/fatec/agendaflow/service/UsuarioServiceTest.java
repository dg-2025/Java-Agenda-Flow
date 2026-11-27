package br.com.fatec.agendaflow.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Cliente;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Cadastro e autenticação (RF-01, RF-02, RN-01, RN-02, RNF-02, RNF-L04). */
@DisplayName("UsuarioService — cadastro e login")
class UsuarioServiceTest extends BancoDeTeste {

    @Test
    @DisplayName("CT-01: cadastro válido cria um CLIENTE com senha em hash")
    void cadastroValido() throws Exception {
        Usuario u = usuarioService.cadastrarCliente("Maria Silva", "maria@teste.com", "senha1234");

        assertTrue(u.getId() > 0);
        assertInstanceOf(Cliente.class, u, "a Factory criou a classe certa");
        assertEquals(Papel.CLIENTE, usuarioDAO.buscarID(u.getId()).getPapel());
        assertNotEquals("senha1234", u.getSenhaHash(), "RNF-02: nunca em texto puro");
        assertTrue(u.getSenhaHash().startsWith("$2a$"), "hash BCrypt");
    }

    @Test
    @DisplayName("CT-02: e-mail já cadastrado é rejeitado (sem diferenciar maiúsculas)")
    void emailDuplicado() throws Exception {
        usuarioService.cadastrarCliente("Maria", "maria@teste.com", "senha1234");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> usuarioService.cadastrarCliente("Outra Maria", "MARIA@teste.com", "outra1234"));
        assertTrue(erro.getMessage().contains("e-mail"));
    }

    @ParameterizedTest(name = "senha \"{0}\" é recusada")
    @ValueSource(strings = {"", "abc123", "somenteletras", "12345678", "a1"})
    @DisplayName("RN-01: senha precisa de 8+ caracteres com letras e números")
    void senhaForaDoPadrao(String senha) {
        assertThrows(RegraNegocioException.class,
                () -> usuarioService.cadastrarCliente("João", "joao@teste.com", senha));
    }

    @Test
    @DisplayName("CT-03: login válido devolve o usuário com o papel certo")
    void loginValido() throws Exception {
        criarAdmin();
        usuarioService.cadastrarCliente("Maria", "maria@teste.com", "senha1234");

        assertEquals(Papel.CLIENTE, usuarioService.autenticar("maria@teste.com", "senha1234").getPapel());
        assertEquals(Papel.ADMIN, usuarioService.autenticar("admin@teste.com", "admin123").getPapel());
    }

    @Test
    @DisplayName("CT-04: senha errada não autentica")
    void loginInvalido() throws Exception {
        usuarioService.cadastrarCliente("Maria", "maria@teste.com", "senha1234");

        assertThrows(RegraNegocioException.class, () -> usuarioService.autenticar("maria@teste.com", "errada123"));
        assertThrows(RegraNegocioException.class, () -> usuarioService.autenticar("ninguem@teste.com", "senha1234"));
    }

    @Test
    @DisplayName("RNF-L04: excluir a conta apaga a identificação e impede novo login")
    void excluirConta() throws Exception {
        Usuario u = usuarioService.cadastrarCliente("Maria", "maria@teste.com", "senha1234");

        usuarioService.excluirConta(u, "senha1234");

        Usuario depois = usuarioDAO.buscarID(u.getId());
        assertEquals("Conta excluída", depois.getNome());
        assertFalse(depois.getEmail().contains("maria"));
        assertThrows(RegraNegocioException.class, () -> usuarioService.autenticar("maria@teste.com", "senha1234"));
    }
}
