package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.model.StatusAgendamento;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import br.com.fatec.agendaflow.util.Logo;
import br.com.fatec.agendaflow.util.Tela;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Área do cliente: menu lateral + páginas Agendar, Meus agendamentos e Minha conta. */
public class ClienteController implements Tela {

    @FXML private VBox menu;
    @FXML private HBox marca;
    @FXML private ToggleButton menuAgendar;
    @FXML private ToggleButton menuAgendamentos;
    @FXML private ToggleButton menuConta;
    @FXML private Label iniciais;
    @FXML private Label nomeUsuario;
    @FXML private Label emailUsuario;
    @FXML private Button botaoSair;

    @FXML private Region paginaAgendar;
    @FXML private Region paginaAgendamentos;
    @FXML private Region paginaConta;
    @FXML private ClienteAgendarController paginaAgendarController;
    @FXML private ClienteAgendamentosController paginaAgendamentosController;
    @FXML private ClienteContaController paginaContaController;

    private Janela janela;

    @Override
    public void iniciar(Janela janela) {
        this.janela = janela;
        Usuario u = janela.getUsuario();

        janela.vidro(menu, 24);
        janela.vidro(paginaAgendar, 24);
        janela.vidro(paginaAgendamentos, 24);
        janela.vidro(paginaConta, 24);

        marca.getChildren().add(0, Logo.criar(40));
        ToggleGroup grupo = new ToggleGroup();
        configurarItem(menuAgendar, "calendario", grupo);
        configurarItem(menuAgendamentos, "lista", grupo);
        configurarItem(menuConta, "usuario", grupo);
        botaoSair.setGraphic(Icones.criar("sair", 18));
        // impede "desmarcar" o item atual clicando de novo
        grupo.selectedToggleProperty().addListener((o, antigo, novo) -> {
            if (novo == null) {
                antigo.setSelected(true);
            }
        });

        iniciais.setText(Formatos.iniciais(u.getNome()));
        nomeUsuario.setText(u.getNome());
        emailUsuario.setText(u.getEmail());

        paginaAgendarController.iniciar(janela, this::aposAgendar);
        paginaAgendamentosController.iniciar(janela);
        paginaContaController.iniciar(janela);

        // Observer: qualquer criação/mudança de agendamento (desta ou de outra janela) atualiza as telas
        janela.ouvir((ag, anterior) -> {
            if (ag.getClienteId() != u.getId()) {
                return;
            }
            paginaAgendamentosController.recarregar();
            paginaContaController.recarregar();
            boolean mudadoPorOutro = anterior != null && !paginaAgendamentosController.fuiEuQueCancelei(ag.getId());
            if (mudadoPorOutro) {
                janela.aviso("Seu agendamento de " + ag.getServicoNome() + " (" + Formatos.data(ag.getData())
                        + ") agora está como \"" + ag.getStatus().getTexto() + "\".",
                        ag.getStatus() == StatusAgendamento.CONCLUIDO ? Janela.TipoAviso.SUCESSO : Janela.TipoAviso.INFO);
            }
        });

        menuAgendar.setSelected(true);
        mostrar(paginaAgendar);
    }

    private void configurarItem(ToggleButton item, String icone, ToggleGroup grupo) {
        item.setGraphic(Icones.criar(icone, 18));
        item.setToggleGroup(grupo);
    }

    private void aposAgendar() {
        menuAgendamentos.setSelected(true);
        mostrarAgendamentos();
    }

    @FXML
    private void mostrarAgendar() {
        mostrar(paginaAgendar);
    }

    @FXML
    private void mostrarAgendamentos() {
        paginaAgendamentosController.recarregar();
        mostrar(paginaAgendamentos);
    }

    @FXML
    private void mostrarConta() {
        paginaContaController.recarregar();
        mostrar(paginaConta);
    }

    private void mostrar(Node pagina) {
        for (Node p : new Node[] {paginaAgendar, paginaAgendamentos, paginaConta}) {
            p.setVisible(p == pagina);
        }
    }

    @FXML
    private void sair() {
        janela.sair();
    }
}
