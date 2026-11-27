package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.util.CalendarioMes;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** RF-12 (lista com status, ordenada por data) e RF-13 (cancelar enquanto "Agendado"). */
public class ClienteAgendamentosController {

    @FXML private Label resumo;
    @FXML private VBox lista;
    @FXML private ScrollPane rolagemLista;
    @FXML private ToggleButton modoLista;
    @FXML private ToggleButton modoCalendario;
    @FXML private HBox visaoCalendario;
    @FXML private StackPane lugarCalendario;
    @FXML private Label tituloDia;
    @FXML private VBox painelDia;

    private Janela janela;
    private final CalendarioMes calendario = new CalendarioMes(a -> a.getHoraInicio() + " " + a.getServicoNome());
    private final Set<Integer> canceladosPorMim = new HashSet<>();

    public void iniciar(Janela janela) {
        this.janela = janela;
        lugarCalendario.getChildren().add(calendario);
        calendario.setAoSelecionar(this::mostrarDia);
        ToggleGroup modos = new ToggleGroup();
        modoLista.setToggleGroup(modos);
        modoCalendario.setToggleGroup(modos);
        modos.selectedToggleProperty().addListener((o, antigo, novo) -> {
            if (novo == null) {
                antigo.setSelected(true);
            }
        });
        modoLista.setSelected(true);
        recarregar();
    }

    @FXML
    private void mostrarLista() {
        rolagemLista.setVisible(true);
        visaoCalendario.setVisible(false);
    }

    @FXML
    private void mostrarCalendario() {
        rolagemLista.setVisible(false);
        visaoCalendario.setVisible(true);
    }

    private void mostrarDia(LocalDate dia) {
        tituloDia.setText(Formatos.dataLonga(dia));
        painelDia.getChildren().clear();
        List<Agendamento> doDia = calendario.doDia(dia);
        if (doDia.isEmpty()) {
            Label l = new Label("Nada marcado neste dia.");
            l.getStyleClass().add("texto-suave");
            painelDia.getChildren().add(l);
            return;
        }
        for (Agendamento a : doDia) {
            Label hora = new Label(a.getHoraInicio() + " – " + a.getHoraFim());
            hora.getStyleClass().add("subtitulo");
            Region espaco = new Region();
            HBox.setHgrow(espaco, Priority.ALWAYS);
            HBox topo = new HBox(8, hora, espaco, status(a.getStatus()));
            topo.setAlignment(Pos.CENTER_LEFT);
            Label servico = new Label(a.getServicoNome());
            Label detalhe = new Label("com " + a.getProfissionalNome() + " · " + Formatos.dinheiro(a.getValor()));
            detalhe.getStyleClass().add("texto-suave");
            VBox cartao = new VBox(6, topo, servico, detalhe);
            cartao.getStyleClass().add("item-lista");
            Button cancelar = botaoCancelar(a);
            if (cancelar != null) {
                cartao.getChildren().add(new HBox(cancelar));
            }
            painelDia.getChildren().add(cartao);
        }
    }

    /** Usado pelo Observer da tela para não avisar o cliente do que ele mesmo fez. */
    public boolean fuiEuQueCancelei(int agendamentoId) {
        return canceladosPorMim.remove(agendamentoId);
    }

    public void recarregar() {
        janela.executar(() -> {
            List<Agendamento> todos = Sistema.AGENDAMENTOS.listarDoCliente(janela.getUsuario());
            LocalDateTime agora = LocalDateTime.now();

            List<Agendamento> proximos = todos.stream()
                    .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO && !a.getData().atTime(a.getHoraFim()).isBefore(agora))
                    .sorted(Comparator.comparing(Agendamento::getData).thenComparing(Agendamento::getHoraInicio))
                    .collect(Collectors.toList());
            List<Agendamento> anteriores = todos.stream()
                    .filter(a -> !proximos.contains(a))
                    .sorted(Comparator.comparing(Agendamento::getData).thenComparing(Agendamento::getHoraInicio).reversed())
                    .collect(Collectors.toList());

            resumo.setText(proximos.isEmpty()
                    ? "Você não tem atendimentos marcados."
                    : proximos.size() == 1 ? "1 atendimento marcado." : proximos.size() + " atendimentos marcados.");

            calendario.setAgendamentos(todos);
            mostrarDia(calendario.getSelecionado());

            lista.getChildren().clear();
            if (todos.isEmpty()) {
                lista.getChildren().add(ClienteAgendarController.vazio("calendario",
                        "Nenhum agendamento ainda. Use \"Agendar\" no menu para marcar o primeiro."));
                return;
            }
            if (!proximos.isEmpty()) {
                lista.getChildren().add(secao("PRÓXIMOS"));
                proximos.forEach(a -> lista.getChildren().add(cartao(a)));
            }
            if (!anteriores.isEmpty()) {
                lista.getChildren().add(secao("HISTÓRICO"));
                anteriores.forEach(a -> lista.getChildren().add(cartao(a)));
            }
        });
    }

    private Node cartao(Agendamento a) {
        Label dia = new Label(String.valueOf(a.getData().getDayOfMonth()));
        dia.getStyleClass().add("data-bloco-dia");
        Label mes = new Label(a.getData().getMonth().getDisplayName(TextStyle.SHORT, Formatos.BR).replace(".", "").toUpperCase());
        mes.getStyleClass().add("data-bloco-mes");
        VBox bloco = new VBox(dia, mes);
        bloco.getStyleClass().add("data-bloco");

        Label servico = new Label(a.getServicoNome());
        servico.getStyleClass().add("subtitulo");
        Label detalhe = new Label(Formatos.dataLonga(a.getData()) + "  ·  " + a.getHoraInicio() + "–" + a.getHoraFim()
                + "  ·  com " + a.getProfissionalNome());
        detalhe.getStyleClass().add("texto-suave");
        VBox textos = new VBox(4, servico, detalhe);
        HBox.setHgrow(textos, Priority.ALWAYS);

        Label valor = new Label(Formatos.dinheiro(a.getValor()));
        valor.getStyleClass().add("preco");

        HBox cartao = new HBox(bloco, textos, valor, status(a.getStatus()));
        cartao.getStyleClass().add("cartao-agendamento");

        Button cancelar = botaoCancelar(a);
        if (cancelar != null) {
            cartao.getChildren().add(cancelar);
        }
        return cartao;
    }

    /** RF-13 + RN-08: só dá para cancelar o que ainda está "Agendado". */
    private Button botaoCancelar(Agendamento a) {
        if (a.getStatus() == StatusAgendamento.AGENDADO && !a.getData().isBefore(LocalDate.now())) {
            Button cancelar = new Button("Cancelar", Icones.criar("x", 14));
            cancelar.getStyleClass().addAll("botao-perigo", "botao-pequeno");
            cancelar.setOnAction(e -> janela.confirmar("Cancelar agendamento?",
                    a.getServicoNome() + " em " + Formatos.dataLonga(a.getData()).toLowerCase() + " às " + a.getHoraInicio()
                            + ". O horário fica livre para outra pessoa.",
                    "Cancelar agendamento", true, () -> {
                        canceladosPorMim.add(a.getId());
                        Sistema.AGENDAMENTOS.cancelarPeloCliente(janela.getUsuario(), a.getId());
                        janela.aviso("Agendamento cancelado.", Janela.TipoAviso.SUCESSO);
                    }));
            return cancelar;
        }
        return null;
    }

    static Label status(StatusAgendamento s) {
        Label l = new Label(s.getTexto());
        l.getStyleClass().addAll("status", "status-" + s.name().toLowerCase());
        l.setMinWidth(Region.USE_PREF_SIZE);
        return l;
    }

    private Label secao(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("secao-titulo");
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }
}
