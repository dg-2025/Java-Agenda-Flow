package br.com.fatec.agendaflow.controller;

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

/** Área do administrador: agendamentos, serviços e profissionais. */
public class AdminController implements Tela {

    @FXML private VBox menu;
    @FXML private HBox marca;
    @FXML private ToggleButton menuAgenda;
    @FXML private ToggleButton menuServicos;
    @FXML private ToggleButton menuProfissionais;
    @FXML private Label iniciais;
    @FXML private Label nomeUsuario;
    @FXML private Label emailUsuario;
    @FXML private Button botaoSair;

    @FXML private Region paginaAgenda;
    @FXML private Region paginaServicos;
    @FXML private Region paginaProfissionais;
    @FXML private AdminAgendaController paginaAgendaController;
    @FXML private AdminServicosController paginaServicosController;
    @FXML private AdminProfissionaisController paginaProfissionaisController;

    private Janela janela;

    @Override
    public void iniciar(Janela janela) {
        this.janela = janela;
        Usuario u = janela.getUsuario();

        janela.vidro(menu, 24);
        janela.vidro(paginaAgenda, 24);
        janela.vidro(paginaServicos, 24);
        janela.vidro(paginaProfissionais, 24);

        marca.getChildren().add(0, Logo.criar(40));
        ToggleGroup grupo = new ToggleGroup();
        for (Object[] item : new Object[][] {{menuAgenda, "calendario"}, {menuServicos, "tesoura"}, {menuProfissionais, "equipe"}}) {
            ToggleButton b = (ToggleButton) item[0];
            b.setGraphic(Icones.criar((String) item[1], 18));
            b.setToggleGroup(grupo);
        }
        grupo.selectedToggleProperty().addListener((o, antigo, novo) -> {
            if (novo == null) {
                antigo.setSelected(true);
            }
        });
        botaoSair.setGraphic(Icones.criar("sair", 18));

        iniciais.setText(Formatos.iniciais(u.getNome()));
        nomeUsuario.setText(u.getNome());
        emailUsuario.setText(u.getEmail());

        paginaAgendaController.iniciar(janela);
        paginaServicosController.iniciar(janela);
        paginaProfissionaisController.iniciar(janela);

        menuAgenda.setSelected(true);
        mostrar(paginaAgenda);
    }

    @FXML
    private void mostrarAgenda() {
        paginaAgendaController.recarregar();
        mostrar(paginaAgenda);
    }

    @FXML
    private void mostrarServicos() {
        paginaServicosController.recarregar();
        mostrar(paginaServicos);
    }

    @FXML
    private void mostrarProfissionais() {
        paginaProfissionaisController.recarregar();
        mostrar(paginaProfissionais);
    }

    private void mostrar(Node pagina) {
        for (Node p : new Node[] {paginaAgenda, paginaServicos, paginaProfissionais}) {
            p.setVisible(p == pagina);
        }
    }

    @FXML
    private void sair() {
        janela.sair();
    }
}
