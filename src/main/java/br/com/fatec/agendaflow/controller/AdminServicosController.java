package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

/** RF-03: cadastro, edição e ativação de serviços (RN-04 validada no service). */
public class AdminServicosController {

    @FXML private Button botaoNovo;
    @FXML private TableView<Servico> tabela;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDuracao;
    @FXML private TableColumn<Servico, String> colValor;
    @FXML private TableColumn<Servico, Servico> colStatus;
    @FXML private Label tituloFormulario;
    @FXML private TextField campoNome;
    @FXML private TextField campoDescricao;
    @FXML private TextField campoDuracao;
    @FXML private TextField campoValor;
    @FXML private ToggleButton campoAtivo;

    private Janela janela;
    private Servico emEdicao;

    public void iniciar(Janela janela) {
        this.janela = janela;
        botaoNovo.setGraphic(Icones.criar("mais", 16));

        colNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNome()));
        colDuracao.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formatos.duracao(c.getValue().getDuracaoMinutos())));
        colValor.setCellValueFactory(c -> new ReadOnlyStringWrapper(Formatos.dinheiro(c.getValue().getValor())));
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Servico s, boolean vazio) {
                super.updateItem(s, vazio);
                setText(null);
                if (vazio || s == null) {
                    setGraphic(null);
                    return;
                }
                Label l = new Label(s.isAtivo() ? "Ativo" : "Inativo");
                l.getStyleClass().addAll("status", s.isAtivo() ? "status-concluido" : "status-cancelado");
                setGraphic(l);
            }
        });
        tabela.setPlaceholder(new Label("Nenhum serviço cadastrado."));
        tabela.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) {
                editar(s);
            }
        });
        campoAtivo.selectedProperty().addListener((o, a, ativo) -> campoAtivo.setText(ativo ? "Ativo" : "Inativo"));
        novo();
        recarregar();
    }

    public void recarregar() {
        janela.executar(() -> {
            int selecionado = emEdicao == null ? 0 : emEdicao.getId();
            tabela.setItems(FXCollections.observableArrayList(Sistema.SERVICOS.listarTodos()));
            tabela.getItems().stream().filter(s -> s.getId() == selecionado).findFirst()
                    .ifPresent(s -> tabela.getSelectionModel().select(s));
        });
    }

    @FXML
    private void novo() {
        emEdicao = null;
        tabela.getSelectionModel().clearSelection();
        tituloFormulario.setText("Novo serviço");
        campoNome.clear();
        campoDescricao.clear();
        campoDuracao.clear();
        campoValor.clear();
        campoAtivo.setSelected(true);
        campoAtivo.setDisable(true); // RF-03: todo serviço novo nasce ativo
        campoNome.requestFocus();
    }

    private void editar(Servico s) {
        emEdicao = s;
        tituloFormulario.setText("Editar serviço");
        campoNome.setText(s.getNome());
        campoDescricao.setText(s.getDescricao());
        campoDuracao.setText(String.valueOf(s.getDuracaoMinutos()));
        campoValor.setText(String.format(Formatos.BR, "%.2f", s.getValor()));
        campoAtivo.setDisable(false);
        campoAtivo.setSelected(s.isAtivo());
    }

    @FXML
    private void salvar() {
        janela.executar(() -> {
            Servico s = emEdicao == null ? new Servico() : emEdicao;
            s.setNome(campoNome.getText());
            s.setDescricao(campoDescricao.getText());
            s.setDuracaoMinutos(inteiro(campoDuracao.getText(), "duração"));
            s.setValor(decimal(campoValor.getText()));
            boolean novo = s.getId() == 0;
            Sistema.SERVICOS.salvar(janela.getUsuario(), s);
            if (!novo && s.isAtivo() != campoAtivo.isSelected()) {
                Sistema.SERVICOS.alterarAtivo(janela.getUsuario(), s, campoAtivo.isSelected());
            }
            emEdicao = s;
            janela.aviso(novo ? "Serviço cadastrado." : "Serviço atualizado.", Janela.TipoAviso.SUCESSO);
            recarregar();
        });
    }

    static int inteiro(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Informe a " + campo + " em minutos (número inteiro).");
        }
    }

    static double decimal(String texto) {
        try {
            String t = texto.trim().replace("R$", "").replace(" ", "");
            if (t.contains(",")) {
                t = t.replace(".", "").replace(',', '.');
            }
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Informe o valor em reais (ex.: 90,00).");
        }
    }
}
