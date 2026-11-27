package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.util.CalendarioMes;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** RF-14 e RF-15: todos os agendamentos, filtro por data e mudança de status. */
public class AdminAgendaController {

    @FXML private Label resumo;
    @FXML private Button botaoHoje;
    @FXML private DatePicker filtroData;
    @FXML private Button botaoTodos;
    @FXML private TableView<Agendamento> tabela;
    @FXML private TableColumn<Agendamento, String> colData;
    @FXML private TableColumn<Agendamento, String> colHorario;
    @FXML private TableColumn<Agendamento, String> colCliente;
    @FXML private TableColumn<Agendamento, String> colServico;
    @FXML private TableColumn<Agendamento, String> colProfissional;
    @FXML private TableColumn<Agendamento, String> colValor;
    @FXML private TableColumn<Agendamento, StatusAgendamento> colStatus;
    @FXML private TableColumn<Agendamento, Agendamento> colAcoes;
    @FXML private HBox filtros;
    @FXML private ToggleButton modoLista;
    @FXML private ToggleButton modoCalendario;
    @FXML private HBox visaoCalendario;
    @FXML private StackPane lugarCalendario;
    @FXML private Label tituloDia;
    @FXML private VBox painelDia;

    private Janela janela;
    private final CalendarioMes calendario = new CalendarioMes(
            a -> a.getHoraInicio() + " " + a.getClienteNome().split(" ")[0]);

    public void iniciar(Janela janela) {
        this.janela = janela;
        botaoHoje.setGraphic(Icones.criar("calendario", 16));

        texto(colData, a -> Formatos.data(a.getData()));
        texto(colHorario, a -> a.getHoraInicio() + " – " + a.getHoraFim());
        texto(colCliente, Agendamento::getClienteNome);
        texto(colServico, Agendamento::getServicoNome);
        texto(colProfissional, Agendamento::getProfissionalNome);
        texto(colValor, a -> Formatos.dinheiro(a.getValor()));

        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(StatusAgendamento s, boolean vazio) {
                super.updateItem(s, vazio);
                setGraphic(vazio || s == null ? null : ClienteAgendamentosController.status(s));
                setText(null);
            }
        });

        colAcoes.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colAcoes.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Agendamento a, boolean vazio) {
                super.updateItem(a, vazio);
                setText(null);
                if (vazio || a == null || a.getStatus().isFinalizado()) {   // RN-08
                    setGraphic(null);
                    return;
                }
                HBox acoes = new HBox(6,
                        acao("check", "Marcar como concluído", "botao-sucesso", a, StatusAgendamento.CONCLUIDO),
                        acao("falta", "Registrar falta do cliente", "botao-alerta", a, StatusAgendamento.FALTA),
                        acao("x", "Cancelar agendamento", "botao-perigo", a, StatusAgendamento.CANCELADO));
                acoes.setAlignment(Pos.CENTER_LEFT);
                setGraphic(acoes);
            }
        });
        tabela.setPlaceholder(new Label("Nenhum agendamento para mostrar."));

        filtroData.valueProperty().addListener((o, a, b) -> recarregar());

        // visualização em calendário (mês inteiro + painel do dia clicado)
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
        filtros.managedProperty().bind(filtros.visibleProperty());

        // Observer: agendamento criado ou alterado em qualquer janela atualiza a tabela
        janela.ouvir((ag, anterior) -> {
            recarregar();
            if (anterior == null) {
                janela.aviso("Novo agendamento: " + ag.getClienteNome() + " · " + ag.getServicoNome() + " em "
                        + Formatos.data(ag.getData()) + " às " + ag.getHoraInicio() + ".", Janela.TipoAviso.INFO);
            }
        });
        recarregar();
    }

    public void recarregar() {
        janela.executar(() -> {
            LocalDate filtro = filtroData.getValue();
            List<Agendamento> lista = Sistema.AGENDAMENTOS.listarTodos(janela.getUsuario(), filtro);
            tabela.setItems(FXCollections.observableArrayList(lista));
            long abertos = lista.stream().filter(a -> a.getStatus() == StatusAgendamento.AGENDADO).count();
            String quando = filtro == null ? "no total" : "em " + Formatos.data(filtro);
            resumo.setText(lista.size() + (lista.size() == 1 ? " agendamento " : " agendamentos ") + quando
                    + " · " + abertos + " em aberto");

            calendario.setAgendamentos(filtro == null ? lista : Sistema.AGENDAMENTOS.listarTodos(janela.getUsuario(), null));
            mostrarDia(calendario.getSelecionado());
        });
    }

    @FXML
    private void mostrarLista() {
        tabela.setVisible(true);
        visaoCalendario.setVisible(false);
        filtros.setVisible(true);
    }

    @FXML
    private void mostrarCalendario() {
        tabela.setVisible(false);
        visaoCalendario.setVisible(true);
        filtros.setVisible(false);
    }

    /** Painel ao lado do calendário: os agendamentos do dia clicado, com as ações do RF-15. */
    private void mostrarDia(LocalDate dia) {
        tituloDia.setText(Formatos.dataLonga(dia));
        painelDia.getChildren().clear();
        List<Agendamento> doDia = calendario.doDia(dia);
        if (doDia.isEmpty()) {
            Label l = new Label("Nenhum agendamento neste dia.");
            l.getStyleClass().add("texto-suave");
            painelDia.getChildren().add(l);
            return;
        }
        for (Agendamento a : doDia) {
            Label hora = new Label(a.getHoraInicio() + " – " + a.getHoraFim());
            hora.getStyleClass().add("subtitulo");
            Region espaco = new Region();
            HBox.setHgrow(espaco, Priority.ALWAYS);
            HBox topo = new HBox(8, hora, espaco, ClienteAgendamentosController.status(a.getStatus()));
            topo.setAlignment(Pos.CENTER_LEFT);
            Label quem = new Label(a.getClienteNome() + " · " + a.getServicoNome());
            quem.setWrapText(true);
            Label prof = new Label("com " + a.getProfissionalNome() + " · " + Formatos.dinheiro(a.getValor()));
            prof.getStyleClass().add("texto-suave");
            VBox cartao = new VBox(6, topo, quem, prof);
            cartao.getStyleClass().add("item-lista");
            if (!a.getStatus().isFinalizado()) {
                HBox acoes = new HBox(6,
                        acao("check", "Marcar como concluído", "botao-sucesso", a, StatusAgendamento.CONCLUIDO),
                        acao("falta", "Registrar falta do cliente", "botao-alerta", a, StatusAgendamento.FALTA),
                        acao("x", "Cancelar agendamento", "botao-perigo", a, StatusAgendamento.CANCELADO));
                cartao.getChildren().add(acoes);
            }
            painelDia.getChildren().add(cartao);
        }
    }

    @FXML
    private void filtrarHoje() {
        filtroData.setValue(LocalDate.now());
    }

    @FXML
    private void limparFiltro() {
        filtroData.setValue(null);
    }

    private Button acao(String icone, String dica, String estilo, Agendamento a, StatusAgendamento novo) {
        Button b = new Button();
        b.setGraphic(Icones.criar(icone, 15));
        b.getStyleClass().addAll(estilo, "botao-pequeno");
        b.setTooltip(new Tooltip(dica));
        b.setOnAction(e -> janela.confirmar(dica + "?",
                a.getClienteNome() + " · " + a.getServicoNome() + " em " + Formatos.data(a.getData()) + " às "
                        + a.getHoraInicio() + ". Depois disso o status não muda mais.",
                "Sim, " + novo.getTexto().toLowerCase(), novo == StatusAgendamento.CANCELADO, () -> {
                    Sistema.AGENDAMENTOS.alterarStatus(janela.getUsuario(), a.getId(), novo);
                    janela.aviso("Agendamento marcado como \"" + novo.getTexto() + "\".", Janela.TipoAviso.SUCESSO);
                }));
        return b;
    }

    private static void texto(TableColumn<Agendamento, String> col, Function<Agendamento, String> valor) {
        col.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
    }
}
