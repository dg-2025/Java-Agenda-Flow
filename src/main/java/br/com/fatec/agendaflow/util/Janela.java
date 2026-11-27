package br.com.fatec.agendaflow.util;

import br.com.fatec.agendaflow.App;
import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.exception.AcessoNegadoException;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.observer.AgendamentoObserver;
import br.com.fatec.agendaflow.service.Permissao;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Moldura de cada janela do app: fundo do tema dark, painéis translúcidos (com
 * suporte a foto de fundo desfocada atrás deles, hoje desligado), barra de título própria, avisos (toasts),
 * diálogos e a navegação entre as telas FXML.
 * <p>
 * Cada janela tem o seu usuário logado. Dá para abrir uma segunda janela (botão
 * na barra de título) e ver, por exemplo, o admin concluir um atendimento e a
 * tela do cliente atualizar na hora — é o Observer funcionando.
 */
public class Janela {

    public enum TipoAviso { SUCESSO, ERRO, INFO }

    private static final Image FUNDO_LOGIN = carregar("img/fundo-login.jpg");
    private static final Image FUNDO_APP = carregar("img/fundo-app.jpg");
    private static final Image ICONE = carregar("img/icone.png");
    private static final double MARGEM_FOSCO = 80;
    private static int abertas = 0;

    private final Stage stage;
    private final StackPane raiz = new StackPane();
    private final Pane camadaFundo = new Pane();
    private final ImageView fundo = new ImageView();
    private final Pane camadaFosco = new Pane();
    private final ImageView fosco = new ImageView();
    private final Group formasVidro = new Group();
    private final StackPane conteudo = new StackPane();
    private final StackPane camadaModal = new StackPane();
    private final VBox camadaAvisos = new VBox(10);
    private final Label tituloUsuario = new Label();
    private final Map<Region, Rectangle> vidros = new LinkedHashMap<>();
    private final List<AgendamentoObserver> observadores = new ArrayList<>();
    private final AnimationTimer sincronizarVidros;

    private Usuario usuario;
    private Rectangle2D tamanhoAntesDeMaximizar;
    private double arrastoX;
    private double arrastoY;

    public Janela(Stage stage) {
        this.stage = stage;
        this.sincronizarVidros = new AnimationTimer() {
            @Override
            public void handle(long agora) {
                atualizarVidros();
            }
        };
    }

    // ------------------------------------------------------------------ montagem

    public void abrir() {
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Agenda Flow");
        if (ICONE != null) {
            stage.getIcons().add(ICONE);
        }

        camadaFundo.getChildren().add(fundo);
        camadaFundo.setMouseTransparent(true);

        Pane brilhos = new Pane();
        brilhos.getStyleClass().add("brilhos-fundo");
        brilhos.setMouseTransparent(true);

        ColorAdjust realce = new ColorAdjust(0, 0.2, 0.06, 0);
        realce.setInput(new GaussianBlur(48));
        fosco.setEffect(realce);
        camadaFosco.getChildren().add(fosco);
        camadaFosco.setClip(formasVidro);
        camadaFosco.setMouseTransparent(true);

        BorderPane moldura = new BorderPane();
        moldura.setTop(criarBarraTitulo());
        moldura.setCenter(conteudo);

        camadaModal.setVisible(false);
        camadaModal.getStyleClass().add("camada-modal");

        camadaAvisos.setAlignment(Pos.BOTTOM_RIGHT);
        camadaAvisos.setPadding(new Insets(0, 28, 28, 0));
        camadaAvisos.setPickOnBounds(false);
        camadaAvisos.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        StackPane.setAlignment(camadaAvisos, Pos.BOTTOM_RIGHT);

        Region alca = new Region();
        alca.getStyleClass().add("alca-redimensionar");
        alca.setCursor(Cursor.SE_RESIZE);
        alca.setMaxSize(18, 18);
        StackPane.setAlignment(alca, Pos.BOTTOM_RIGHT);
        ativarRedimensionamento(alca);

        raiz.getChildren().addAll(camadaFundo, brilhos, camadaFosco, moldura, camadaAvisos, camadaModal, alca);
        raiz.getStyleClass().add("janela");

        Rectangle2D tela = Screen.getPrimary().getVisualBounds();
        double largura = Math.min(1320, tela.getWidth() * 0.94);
        double altura = Math.min(840, tela.getHeight() * 0.94);
        Scene cena = new Scene(raiz, largura, altura, Color.web("#0B0B10"));
        cena.getStylesheets().add(App.class.getResource("styles/app.css").toExternalForm());
        stage.setScene(cena);
        stage.setMinWidth(1020);
        stage.setMinHeight(640);

        raiz.widthProperty().addListener((o, a, b) -> ajustarFundo());
        raiz.heightProperty().addListener((o, a, b) -> ajustarFundo());

        stage.setOnHidden(e -> {
            sincronizarVidros.stop();
            pararDeOuvir();
            abertas--;
        });
        abertas++;
        if (abertas > 1) {
            stage.setX(tela.getMinX() + (tela.getWidth() - largura) / 2 + 36 * (abertas - 1));
            stage.setY(tela.getMinY() + (tela.getHeight() - altura) / 2 + 28 * (abertas - 1));
        }

        ir("login");
        stage.show();
        sincronizarVidros.start();
    }

    private Node criarBarraTitulo() {
        Label nome = new Label("Agenda Flow");
        nome.getStyleClass().add("barra-titulo-nome");
        tituloUsuario.getStyleClass().add("barra-titulo-usuario");

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Button novaJanela = botaoBarra("janela", "Abrir outra janela (ex.: admin e cliente lado a lado)");
        novaJanela.setOnAction(e -> new Janela(new Stage()).abrir());
        Button minimizar = botaoBarra("menos", "Minimizar");
        minimizar.setOnAction(e -> stage.setIconified(true));
        Button maximizar = botaoBarra("quadrado", "Maximizar");
        maximizar.setOnAction(e -> alternarMaximizado());
        Button fechar = botaoBarra("x", "Fechar");
        fechar.getStyleClass().add("fechar");
        fechar.setOnAction(e -> stage.close());

        HBox barra = new HBox(10, Logo.criar(18), nome, tituloUsuario, espaco, novaJanela, minimizar, maximizar, fechar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-titulo");

        barra.setOnMousePressed(e -> {
            arrastoX = e.getScreenX() - stage.getX();
            arrastoY = e.getScreenY() - stage.getY();
        });
        barra.setOnMouseDragged(e -> {
            if (tamanhoAntesDeMaximizar == null) {
                stage.setX(e.getScreenX() - arrastoX);
                stage.setY(e.getScreenY() - arrastoY);
            }
        });
        barra.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                alternarMaximizado();
            }
        });
        return barra;
    }

    private Button botaoBarra(String icone, String dica) {
        Button b = new Button();
        b.setGraphic(Icones.criar(icone, 14));
        b.getStyleClass().add("botao-barra");
        b.setTooltip(new javafx.scene.control.Tooltip(dica));
        b.setFocusTraversable(false);
        return b;
    }

    private void alternarMaximizado() {
        if (tamanhoAntesDeMaximizar == null) {
            tamanhoAntesDeMaximizar = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            Rectangle2D area = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                    .stream().findFirst().orElse(Screen.getPrimary()).getVisualBounds();
            stage.setX(area.getMinX());
            stage.setY(area.getMinY());
            stage.setWidth(area.getWidth());
            stage.setHeight(area.getHeight());
        } else {
            stage.setX(tamanhoAntesDeMaximizar.getMinX());
            stage.setY(tamanhoAntesDeMaximizar.getMinY());
            stage.setWidth(tamanhoAntesDeMaximizar.getWidth());
            stage.setHeight(tamanhoAntesDeMaximizar.getHeight());
            tamanhoAntesDeMaximizar = null;
        }
    }

    private void ativarRedimensionamento(Region alca) {
        final double[] inicio = new double[4];
        alca.setOnMousePressed(e -> {
            inicio[0] = e.getScreenX();
            inicio[1] = e.getScreenY();
            inicio[2] = stage.getWidth();
            inicio[3] = stage.getHeight();
        });
        alca.setOnMouseDragged(e -> {
            stage.setWidth(Math.max(stage.getMinWidth(), inicio[2] + e.getScreenX() - inicio[0]));
            stage.setHeight(Math.max(stage.getMinHeight(), inicio[3] + e.getScreenY() - inicio[1]));
            tamanhoAntesDeMaximizar = null;
        });
    }

    // ------------------------------------------------------------------ fundo e vidro

    private void ajustarFundo() {
        cobrir(fundo, 0);
        cobrir(fosco, MARGEM_FOSCO);
    }

    /** Faz a imagem cobrir a janela inteira (como background-size: cover). */
    private void cobrir(ImageView iv, double margem) {
        Image img = iv.getImage();
        if (img == null || img.getWidth() == 0) {
            return;
        }
        double w = raiz.getWidth() + 2 * margem;
        double h = raiz.getHeight() + 2 * margem;
        double escala = Math.max(w / img.getWidth(), h / img.getHeight());
        iv.setFitWidth(img.getWidth() * escala);
        iv.setFitHeight(img.getHeight() * escala);
        iv.setLayoutX((raiz.getWidth() - img.getWidth() * escala) / 2);
        iv.setLayoutY((raiz.getHeight() - img.getHeight() * escala) / 2);
    }

    /**
     * Transforma um painel em vidro fosco: atrás dele aparece a foto de fundo
     * desfocada, recortada exatamente no formato do painel.
     */
    public void vidro(Region painel, double raio) {
        Rectangle forma = new Rectangle();
        forma.setArcWidth(raio * 2);
        forma.setArcHeight(raio * 2);
        vidros.put(painel, forma);
        formasVidro.getChildren().add(forma);
    }

    private void atualizarVidros() {
        Iterator<Map.Entry<Region, Rectangle>> it = vidros.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Region, Rectangle> e = it.next();
            Region painel = e.getKey();
            Rectangle forma = e.getValue();
            if (painel.getScene() == null) {
                formasVidro.getChildren().remove(forma);
                it.remove();
                continue;
            }
            boolean visivel = visivelNaArvore(painel);
            forma.setVisible(visivel);
            if (visivel) {
                Bounds b = raiz.sceneToLocal(painel.localToScene(painel.getLayoutBounds()));
                forma.setX(b.getMinX());
                forma.setY(b.getMinY());
                forma.setWidth(b.getWidth());
                forma.setHeight(b.getHeight());
            }
        }
    }

    private static boolean visivelNaArvore(Node n) {
        for (Node atual = n; atual != null; atual = atual.getParent()) {
            if (!atual.isVisible() || atual.getOpacity() < 0.05) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ navegação

    /** Troca a tela da janela. "admin" exige papel ADMIN (RNF-05). */
    public void ir(String tela) {
        if ("admin".equals(tela)) {
            try {
                Permissao.exigirAdmin(usuario);
            } catch (AcessoNegadoException e) {
                aviso(e.getMessage(), TipoAviso.ERRO);
                return;
            }
        }
        pararDeOuvir();
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/" + tela + ".fxml"));
            Parent view = loader.load();
            Object controller = loader.getController();
            boolean telaDeEntrada = tela.equals("login") || tela.equals("cadastro");
            usarFundo(telaDeEntrada ? FUNDO_LOGIN : FUNDO_APP);
            conteudo.getChildren().setAll(view);
            if (controller instanceof Tela t) {
                t.iniciar(this);
            }
            atualizarTitulo();

            view.setOpacity(0);
            view.setTranslateY(14);
            FadeTransition fade = new FadeTransition(Duration.millis(320), view);
            fade.setToValue(1);
            TranslateTransition sobe = new TranslateTransition(Duration.millis(380), view);
            sobe.setToY(0);
            sobe.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fade, sobe).play();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível abrir a tela " + tela, e);
        }
    }

    private void usarFundo(Image img) {
        if (fundo.getImage() != img) {
            fundo.setImage(img);
            fosco.setImage(img);
            ajustarFundo();
        }
    }

    private void atualizarTitulo() {
        tituloUsuario.setText(usuario == null ? "" : "·  " + usuario.getNome()
                + (usuario.isAdmin() ? " (administrador)" : ""));
    }

    public void entrar(Usuario u) {
        this.usuario = u;
        ir(u.isAdmin() ? "admin" : "cliente");
    }

    public void sair() {
        this.usuario = null;
        ir("login");
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Stage getStage() {
        return stage;
    }

    // ------------------------------------------------------------------ Observer na interface

    /**
     * Inscreve um observador enquanto esta tela estiver aberta. A reação roda
     * na thread do JavaFX, e a inscrição é desfeita ao trocar de tela ou fechar.
     */
    public void ouvir(AgendamentoObserver observador) {
        AgendamentoObserver naTela = (ag, anterior) -> Platform.runLater(() -> observador.atualizar(ag, anterior));
        observadores.add(naTela);
        Sistema.EVENTOS.inscrever(naTela);
    }

    private void pararDeOuvir() {
        observadores.forEach(Sistema.EVENTOS::remover);
        observadores.clear();
    }

    // ------------------------------------------------------------------ ações, avisos e diálogos

    @FunctionalInterface
    public interface Acao {
        void executar() throws Exception;
    }

    /** Executa uma ação da tela e transforma erro de regra/banco em aviso. Devolve true se deu certo. */
    public boolean executar(Acao acao) {
        try {
            acao.executar();
            return true;
        } catch (RegraNegocioException e) {
            aviso(e.getMessage(), TipoAviso.ERRO);
        } catch (SQLException e) {
            aviso("Erro no banco de dados: " + e.getMessage(), TipoAviso.ERRO);
        } catch (Exception e) {
            aviso("Algo deu errado: " + e.getMessage(), TipoAviso.ERRO);
            e.printStackTrace();
        }
        return false;
    }

    public void aviso(String mensagem, TipoAviso tipo) {
        String icone = switch (tipo) {
            case SUCESSO -> "check";
            case ERRO -> "x";
            case INFO -> "brilho";
        };
        StackPane bolinha = new StackPane(Icones.criar(icone, 14));
        bolinha.getStyleClass().addAll("aviso-icone", "aviso-" + tipo.name().toLowerCase());
        Label texto = new Label(mensagem);
        texto.setWrapText(true);
        texto.setMaxWidth(340);
        HBox aviso = new HBox(12, bolinha, texto);
        aviso.setAlignment(Pos.CENTER_LEFT);
        aviso.getStyleClass().add("aviso");
        aviso.setMaxWidth(Region.USE_PREF_SIZE);

        camadaAvisos.getChildren().add(aviso);
        aviso.setOpacity(0);
        aviso.setTranslateY(16);
        FadeTransition entra = new FadeTransition(Duration.millis(220), aviso);
        entra.setToValue(1);
        TranslateTransition sobe = new TranslateTransition(Duration.millis(260), aviso);
        sobe.setToY(0);
        FadeTransition sai = new FadeTransition(Duration.millis(300), aviso);
        sai.setToValue(0);
        sai.setOnFinished(e -> camadaAvisos.getChildren().remove(aviso));
        new SequentialTransition(new ParallelTransition(entra, sobe),
                new PauseTransition(Duration.seconds(tipo == TipoAviso.ERRO ? 4.2 : 3.2)), sai).play();
    }

    /** Abre um diálogo de vidro por cima da tela. Devolve a ação que fecha o diálogo. */
    public Runnable dialogo(String titulo, String texto, Node corpo, String textoBotao, boolean perigoso, Acao aoConfirmar) {
        Label t = new Label(titulo);
        t.getStyleClass().add("dialogo-titulo");
        Label m = new Label(texto);
        m.setWrapText(true);
        m.getStyleClass().add("texto-suave");

        Button cancelar = new Button("Voltar");
        cancelar.getStyleClass().add("botao-secundario");
        Button confirmar = new Button(textoBotao);
        confirmar.getStyleClass().add(perigoso ? "botao-perigo" : "botao-primario");
        confirmar.setDefaultButton(true);
        HBox botoes = new HBox(10, cancelar, confirmar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        VBox caixa = new VBox(14, t, m);
        if (corpo != null) {
            caixa.getChildren().add(corpo);
        }
        caixa.getChildren().add(botoes);
        caixa.getStyleClass().addAll("vidro", "dialogo");
        caixa.setMaxSize(440, Region.USE_PREF_SIZE);
        caixa.setPrefWidth(440);

        camadaModal.getChildren().setAll(caixa);
        camadaModal.setVisible(true);

        caixa.setScaleX(0.94);
        caixa.setScaleY(0.94);
        caixa.setOpacity(0);
        ScaleTransition cresce = new ScaleTransition(Duration.millis(200), caixa);
        cresce.setToX(1);
        cresce.setToY(1);
        FadeTransition aparece = new FadeTransition(Duration.millis(200), caixa);
        aparece.setToValue(1);
        new ParallelTransition(cresce, aparece).play();

        Runnable fechar = () -> {
            camadaModal.setVisible(false);
            camadaModal.getChildren().clear();
        };
        cancelar.setOnAction(e -> fechar.run());
        camadaModal.setOnMouseClicked(e -> {
            if (e.getTarget() == camadaModal) {
                fechar.run();
            }
        });
        confirmar.setOnAction(e -> {
            if (executar(aoConfirmar)) {
                fechar.run();
            }
        });
        return fechar;
    }

    public void confirmar(String titulo, String texto, String textoBotao, boolean perigoso, Acao aoConfirmar) {
        dialogo(titulo, texto, null, textoBotao, perigoso, aoConfirmar);
    }

    private static Image carregar(String caminho) {
        InputStream in = App.class.getResourceAsStream(caminho);
        return in == null ? null : new Image(in);
    }
}
