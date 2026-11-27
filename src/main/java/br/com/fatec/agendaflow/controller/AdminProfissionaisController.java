package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** RF-04, RF-05 e RF-06: profissionais, vínculos com serviços e expediente semanal. */
public class AdminProfissionaisController {

    @FXML private Button botaoNovo;
    @FXML private ListView<Profissional> lista;
    @FXML private Label tituloDetalhe;
    @FXML private TextField campoNome;
    @FXML private ToggleButton campoAtivo;
    @FXML private VBox blocoServicos;
    @FXML private FlowPane servicos;
    @FXML private VBox blocoHorarios;
    @FXML private ComboBox<DayOfWeek> campoDia;
    @FXML private ComboBox<LocalTime> campoInicio;
    @FXML private ComboBox<LocalTime> campoFim;
    @FXML private Button botaoAdicionarHorario;
    @FXML private VBox horarios;

    private Janela janela;
    private Profissional selecionado;

    public void iniciar(Janela janela) {
        this.janela = janela;
        botaoNovo.setGraphic(Icones.criar("mais", 16));
        botaoAdicionarHorario.setGraphic(Icones.criar("mais", 14));

        lista.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Profissional p, boolean vazio) {
                super.updateItem(p, vazio);
                setText(null);
                if (vazio || p == null) {
                    setGraphic(null);
                    return;
                }
                StackPane avatar = new StackPane(new Label(Formatos.iniciais(p.getNome())));
                avatar.getStyleClass().add("avatar");
                Label nome = new Label(p.getNome());
                nome.getStyleClass().add("subtitulo");
                Label sub = new Label(p.isAtivo() ? p.getServicoIds().size() + " serviço(s)" : "Inativo");
                sub.getStyleClass().add("texto-apagado");
                HBox item = new HBox(10, avatar, new VBox(2, nome, sub));
                item.setAlignment(Pos.CENTER_LEFT);
                item.getStyleClass().add("item-lista");
                if (!p.isAtivo()) {
                    item.setOpacity(0.55);
                }
                setGraphic(item);
            }
        });
        lista.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> {
            if (p != null) {
                selecionar(p);
            }
        });

        campoDia.setItems(FXCollections.observableArrayList(DayOfWeek.values()));
        campoDia.setConverter(new StringConverter<>() {
            @Override
            public String toString(DayOfWeek d) {
                return d == null ? "" : Formatos.capitalizar(d.getDisplayName(TextStyle.FULL, Formatos.BR));
            }

            @Override
            public DayOfWeek fromString(String s) {
                return null;
            }
        });
        List<LocalTime> horas = new ArrayList<>();
        for (LocalTime t = LocalTime.of(6, 0); !t.isAfter(LocalTime.of(23, 0)); t = t.plusMinutes(30)) {
            horas.add(t);
        }
        campoInicio.setItems(FXCollections.observableArrayList(horas));
        campoFim.setItems(FXCollections.observableArrayList(horas));
        campoAtivo.selectedProperty().addListener((o, a, ativo) -> campoAtivo.setText(ativo ? "Ativo" : "Inativo"));

        recarregar();
        if (!lista.getItems().isEmpty()) {
            lista.getSelectionModel().selectFirst();
        } else {
            novo();
        }
    }

    public void recarregar() {
        janela.executar(() -> {
            int id = selecionado == null ? 0 : selecionado.getId();
            lista.setItems(FXCollections.observableArrayList(Sistema.PROFISSIONAIS.listarTodos()));
            lista.getItems().stream().filter(p -> p.getId() == id).findFirst()
                    .ifPresent(p -> lista.getSelectionModel().select(p));
        });
    }

    @FXML
    private void novo() {
        selecionado = null;
        lista.getSelectionModel().clearSelection();
        tituloDetalhe.setText("Novo profissional");
        campoNome.clear();
        campoAtivo.setSelected(true);
        campoAtivo.setDisable(true);
        blocoServicos.setDisable(true);
        blocoHorarios.setDisable(true);
        servicos.getChildren().setAll(new Label("Salve o profissional para escolher os serviços."));
        horarios.getChildren().clear();
        campoNome.requestFocus();
    }

    private void selecionar(Profissional p) {
        selecionado = p;
        tituloDetalhe.setText(p.getNome());
        campoNome.setText(p.getNome());
        campoAtivo.setDisable(false);
        campoAtivo.setSelected(p.isAtivo());
        blocoServicos.setDisable(false);
        blocoHorarios.setDisable(false);
        desenharServicos();
        desenharHorarios();
    }

    @FXML
    private void salvar() {
        janela.executar(() -> {
            boolean novo = selecionado == null;
            Profissional p = novo ? new Profissional() : selecionado;
            p.setNome(campoNome.getText());
            Sistema.PROFISSIONAIS.salvar(janela.getUsuario(), p);
            if (!novo && p.isAtivo() != campoAtivo.isSelected()) {
                Sistema.PROFISSIONAIS.alterarAtivo(janela.getUsuario(), p, campoAtivo.isSelected());
            }
            selecionado = p;
            janela.aviso(novo ? "Profissional cadastrado. Agora marque os serviços e os horários."
                    : "Profissional atualizado.", Janela.TipoAviso.SUCESSO);
            recarregar();
            selecionar(p);
        });
    }

    // ------------------------------------------------------------ RF-05

    private void desenharServicos() {
        janela.executar(() -> {
            servicos.getChildren().clear();
            for (Servico s : Sistema.SERVICOS.listarTodos()) {
                CheckBox c = new CheckBox(s.getNome() + (s.isAtivo() ? "" : " (inativo)"));
                c.setSelected(selecionado.atendeServico(s.getId()));
                c.selectedProperty().addListener((o, antes, marcado) -> janela.executar(() -> {
                    Sistema.PROFISSIONAIS.definirVinculo(janela.getUsuario(), selecionado, s.getId(), marcado);
                    lista.refresh();
                }));
                servicos.getChildren().add(c);
            }
        });
    }

    // ------------------------------------------------------------ RF-06

    private void desenharHorarios() {
        janela.executar(() -> {
            horarios.getChildren().clear();
            List<HorarioTrabalho> lista = Sistema.PROFISSIONAIS.listarHorarios(selecionado.getId());
            if (lista.isEmpty()) {
                Label l = new Label("Sem horário cadastrado: este profissional não aparece com horários livres.");
                l.getStyleClass().add("texto-suave");
                horarios.getChildren().add(l);
                return;
            }
            for (HorarioTrabalho h : lista) {
                Label dia = new Label(h.getDiaSemanaTexto());
                dia.getStyleClass().add("subtitulo");
                dia.setMinWidth(150);
                Label faixa = new Label(h.getHoraInicio() + " às " + h.getHoraFim());
                faixa.getStyleClass().add("texto-suave");
                Region espaco = new Region();
                HBox.setHgrow(espaco, Priority.ALWAYS);
                Button remover = new Button();
                remover.setGraphic(Icones.criar("lixeira", 14));
                remover.getStyleClass().addAll("botao-perigo", "botao-pequeno");
                remover.setOnAction(e -> janela.executar(() -> {
                    Sistema.PROFISSIONAIS.removerHorario(janela.getUsuario(), h);
                    desenharHorarios();
                }));
                HBox linha = new HBox(12, dia, faixa, espaco, remover);
                linha.setAlignment(Pos.CENTER_LEFT);
                linha.getStyleClass().add("item-lista");
                horarios.getChildren().add(linha);
            }
        });
    }

    @FXML
    private void adicionarHorario() {
        janela.executar(() -> {
            if (campoDia.getValue() == null || campoInicio.getValue() == null || campoFim.getValue() == null) {
                throw new RegraNegocioException("Escolha o dia, a hora de início e a de término.");
            }
            Sistema.PROFISSIONAIS.adicionarHorario(janela.getUsuario(), new HorarioTrabalho(0, selecionado.getId(),
                    campoDia.getValue(), campoInicio.getValue(), campoFim.getValue()));
            janela.aviso("Horário adicionado.", Janela.TipoAviso.SUCESSO);
            desenharHorarios();
        });
    }
}
