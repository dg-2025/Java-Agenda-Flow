package br.com.fatec.agendaflow;

import br.com.fatec.agendaflow.util.Janela;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javax.imageio.ImageIO;

/**
 * Passeio automático pelas telas que salva um print de cada uma (usado para as
 * imagens do README). Só roda com a variável de ambiente AGENDAFLOW_PRINT
 * apontando para uma pasta; no uso normal não faz nada.
 */
final class Demonstracao {

    private final Janela janela;
    private final Path pasta;
    private final List<Runnable> passos = new ArrayList<>();
    private int atual = 0;

    private Demonstracao(Janela janela, Path pasta) {
        this.janela = janela;
        this.pasta = pasta;
    }

    static void seAtiva(Janela janela) {
        String destino = System.getenv("AGENDAFLOW_PRINT");
        if (destino == null || destino.isBlank()) {
            return;
        }
        new Demonstracao(janela, Paths.get(destino)).roteiro();
    }

    private void roteiro() {
        passo(() -> print("01-login"));
        passo(() -> clicarTexto(".link", "Criar conta"));
        passo(() -> print("02-cadastro"));
        passo(() -> janela.ir("login"));
        passo(() -> entrar("cliente@agendaflow.com", "cliente123"));
        passo(() -> print("03-cliente-servicos"));
        passo(() -> clicarCartao("Design de sobrancelha"));
        passo(() -> print("04-cliente-profissional"));
        passo(() -> clicarCartao("Carla Mendes"));
        passo(() -> print("05-cliente-horarios"));
        passo(() -> clicarPrimeiro(".horario"));
        passo(() -> print("06-cliente-resumo"));
        passo(() -> clicarTexto(".botao-primario", "Confirmar agendamento"));
        passo(() -> print("07-cliente-agendamentos"));
        passo(() -> clicarId("menuConta"));
        passo(() -> print("08-cliente-conta"));
        passo(() -> janela.sair());
        passo(() -> entrar("admin@agendaflow.com", "admin123"));
        passo(() -> print("09-admin-agendamentos"));
        passo(() -> clicarId("menuServicos"));
        passo(() -> print("10-admin-servicos"));
        passo(() -> clicarId("menuProfissionais"));
        passo(() -> print("11-admin-profissionais"));
        passo(Platform::exit);
        proximo();
    }

    private void passo(Runnable r) {
        passos.add(r);
    }

    private void proximo() {
        if (atual >= passos.size()) {
            return;
        }
        PauseTransition espera = new PauseTransition(Duration.millis(atual == 0 ? 1600 : 1100));
        espera.setOnFinished(e -> {
            try {
                passos.get(atual++).run();
            } catch (Exception ex) {
                System.err.println("Demonstração: passo " + atual + " falhou: " + ex);
            }
            proximo();
        });
        espera.play();
    }

    private Scene cena() {
        return janela.getStage().getScene();
    }

    private void entrar(String email, String senha) {
        ((TextField) cena().lookup("#campoEmail")).setText(email);
        ((PasswordField) cena().lookup("#campoSenha")).setText(senha);
        ((ButtonBase) cena().lookup("#botaoEntrar")).fire();
    }

    private void clicarId(String id) {
        ((ButtonBase) cena().lookup("#" + id)).fire();
    }

    private void clicarTexto(String seletor, String texto) {
        for (Node n : cena().getRoot().lookupAll(seletor)) {
            if (n instanceof ButtonBase b && texto.equals(b.getText()) && b.isVisible()) {
                b.fire();
                return;
            }
        }
    }

    private void clicarPrimeiro(String seletor) {
        Node n = cena().getRoot().lookup(seletor);
        if (n instanceof ButtonBase b) {
            b.fire();
        }
    }

    private void clicarCartao(String titulo) {
        for (Node cartao : cena().getRoot().lookupAll(".cartao-opcao")) {
            for (Node filho : cartao.lookupAll(".label")) {
                if (filho instanceof Label l && titulo.equals(l.getText())) {
                    Event.fireEvent(cartao, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                            false, false, false, false, true, false, false, true, false, false, null));
                    return;
                }
            }
        }
    }

    private void print(String nome) {
        try {
            WritableImage img = cena().snapshot(null);
            int w = (int) img.getWidth();
            int h = (int) img.getHeight();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            PixelReader pr = img.getPixelReader();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    bi.setRGB(x, y, pr.getArgb(x, y));
                }
            }
            Files.createDirectories(pasta);
            ImageIO.write(bi, "png", pasta.resolve(nome + ".png").toFile());
        } catch (Exception e) {
            System.err.println("Não foi possível salvar o print " + nome + ": " + e.getMessage());
        }
    }
}
