package br.com.fatec.agendaflow.factory;

import static org.junit.jupiter.api.Assertions.*;

import br.com.fatec.agendaflow.model.Administrador;
import br.com.fatec.agendaflow.model.Cliente;
import br.com.fatec.agendaflow.model.Papel;
import br.com.fatec.agendaflow.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UsuarioFactory — padrão Factory")
class UsuarioFactoryTest {

    @Test
    @DisplayName("Papel ADMIN gera um Administrador")
    void criaAdministrador() {
        Usuario u = UsuarioFactory.criar(Papel.ADMIN, 1, "Ana", "ana@x.com", "hash");
        assertInstanceOf(Administrador.class, u);
        assertTrue(u.isAdmin());
    }

    @Test
    @DisplayName("Papel CLIENTE gera um Cliente com os dados informados")
    void criaCliente() {
        Usuario u = UsuarioFactory.criar(Papel.CLIENTE, 7, "Bia", "bia@x.com", "hash");
        assertInstanceOf(Cliente.class, u);
        assertEquals(7, u.getId());
        assertEquals("bia@x.com", u.getEmail());
        assertFalse(u.isAdmin());
    }

    @ParameterizedTest(name = "texto \"{0}\" vira Cliente")
    @ValueSource(strings = {"CLIENTE", "cliente", " Cliente "})
    @DisplayName("Texto vindo do banco é aceito sem diferenciar maiúsculas")
    void aceitaTextoDoBanco(String papel) {
        assertInstanceOf(Cliente.class, UsuarioFactory.criar(papel, 1, "X", "x@x.com", "h"));
    }

    @Test
    @DisplayName("Novo cadastro pela tela é sempre CLIENTE")
    void novoClienteSempreCliente() {
        assertEquals(Papel.CLIENTE, UsuarioFactory.novoCliente("X", "x@x.com", "h").getPapel());
    }

    @Test
    @DisplayName("Papel desconhecido ou vazio lança exceção")
    void papelInvalido() {
        assertThrows(IllegalArgumentException.class, () -> UsuarioFactory.criar("GERENTE", 1, "X", "x@x.com", "h"));
        assertThrows(IllegalArgumentException.class, () -> UsuarioFactory.criar("", 1, "X", "x@x.com", "h"));
        assertThrows(IllegalArgumentException.class, () -> UsuarioFactory.criar((Papel) null, 1, "X", "x@x.com", "h"));
    }
}
