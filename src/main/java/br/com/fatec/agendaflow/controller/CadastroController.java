package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.exception.RegraNegocioException;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import br.com.fatec.agendaflow.util.Tela;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** RF-01: cadastro de cliente (com aviso de finalidade da LGPD). */
public class CadastroController implements Tela {

    @FXML private VBox painel;
    @FXML private TextField campoNome;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;
    @FXML private PasswordField campoConfirmacao;
    @FXML private Label dicaSenha;
    @FXML private HBox caixaLgpd;

    private Janela janela;

    @Override
    public void iniciar(Janela janela) {
        this.janela = janela;
        janela.vidro(painel, 24);
        caixaLgpd.getChildren().add(0, Icones.criar("escudo", 20));
        campoNome.requestFocus();
    }

    @FXML
    private void cadastrar() {
        janela.executar(() -> {
            if (!campoSenha.getText().equals(campoConfirmacao.getText())) {
                throw new RegraNegocioException("As senhas não são iguais.");
            }
            Usuario novo = Sistema.USUARIOS.cadastrarCliente(campoNome.getText(), campoEmail.getText(), campoSenha.getText());
            janela.entrar(novo);
            janela.aviso("Conta criada! Agora é só escolher o seu horário.", Janela.TipoAviso.SUCESSO);
        });
    }

    @FXML
    private void voltar() {
        janela.ir("login");
    }
}
