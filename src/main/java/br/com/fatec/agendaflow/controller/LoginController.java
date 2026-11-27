package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import br.com.fatec.agendaflow.util.Logo;
import br.com.fatec.agendaflow.util.Tela;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** RF-02: login de cliente e administrador. */
public class LoginController implements Tela {

    @FXML private StackPane logo;
    @FXML private VBox recursos;
    @FXML private VBox painel;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;
    @FXML private Button botaoEntrar;
    @FXML private FlowPane contasDemo;

    private Janela janela;

    @Override
    public void iniciar(Janela janela) {
        this.janela = janela;
        janela.vidro(painel, 24);
        logo.getChildren().add(Logo.criar(64));

        recursos.getChildren().addAll(
                recurso("relogio", "Horários livres calculados na hora"),
                recurso("escudo", "Sem conflito: um profissional, um atendimento por vez"),
                recurso("presente", "Desconto fidelidade a partir do 3º atendimento"));

        contasDemo.getChildren().addAll(
                chip("Cliente", "cliente@agendaflow.com", "cliente123"),
                chip("Administrador", "admin@agendaflow.com", "admin123"));
        campoEmail.requestFocus();
    }

    @FXML
    private void entrar() {
        janela.executar(() -> {
            Usuario u = Sistema.USUARIOS.autenticar(campoEmail.getText(), campoSenha.getText());
            janela.entrar(u);
            janela.aviso("Olá, " + u.getNome().split(" ")[0] + "!", Janela.TipoAviso.SUCESSO);
        });
    }

    @FXML
    private void irParaCadastro() {
        janela.ir("cadastro");
    }

    private HBox recurso(String icone, String texto) {
        StackPane caixa = new StackPane(Icones.criar(icone, 18));
        caixa.getStyleClass().add("recurso-icone");
        Label l = new Label(texto);
        l.setWrapText(true);
        HBox linha = new HBox(caixa, l);
        linha.getStyleClass().add("recurso");
        linha.setAlignment(Pos.CENTER_LEFT);
        return linha;
    }

    private Button chip(String nome, String email, String senha) {
        Button b = new Button(nome + " · " + email);
        b.getStyleClass().add("chip-demo");
        b.setOnAction(e -> {
            campoEmail.setText(email);
            campoSenha.setText(senha);
            botaoEntrar.requestFocus();
        });
        return b;
    }
}
