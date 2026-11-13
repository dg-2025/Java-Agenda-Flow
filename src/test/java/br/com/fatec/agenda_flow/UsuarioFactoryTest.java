package br.com.fatec.agenda_flow;

import br.com.fatec.agenda_flow.factory.UsuarioFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioFactoryTest {

    @Test
    @DisplayName("Deve criar uma instancia de Cliente quando o tipo for CLIENTE")
    void deveCriarCliente() {
        Usuario u = UsuarioFactory.criarUsuario("CLIENTE", 1, "Carlos", "carlos@email.com", "12345678");
        assertTrue(u instanceof Cliente);
        assertEquals("Carlos", u.getNome());
    }

    @Test
    @DisplayName("Deve criar uma instancia de Administrador quando o tipo for ADMIN")
    void deveCriarAdministrador() {
        Usuario u = UsuarioFactory.criarUsuario("ADMIN", 2, "Mariana", "admin@email.com", "admin123");
        assertTrue(u instanceof Administrador);
        assertEquals("Mariana", u.getNome());
    }

    @Test
    @DisplayName("Deve lancar IllegalArgumentException para tipo de usuario invalido")
    void deveLancarExcecaoParaTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            UsuarioFactory.criarUsuario("VISITANTE", 3, "Joao", "joao@email.com", "12345678");
        });
    }
}
