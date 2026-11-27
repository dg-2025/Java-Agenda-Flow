package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.model.Usuario;
import br.com.fatec.agendaflow.strategy.CalculoPrecoStrategy;
import br.com.fatec.agendaflow.strategy.SeletorPrecoStrategy;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Dados do cliente, progresso da fidelidade (RN-11) e exclusão de conta (RNF-L04). */
public class ClienteContaController {

    @FXML private Label iniciais;
    @FXML private Label nome;
    @FXML private Label email;
    @FXML private VBox cartaoFidelidade;
    @FXML private HBox cabecalhoFidelidade;
    @FXML private Label textoFidelidade;
    @FXML private Region barraFidelidade;
    @FXML private Label regraPreco;
    @FXML private Button botaoExcluir;

    private Janela janela;

    public void iniciar(Janela janela) {
        this.janela = janela;
        Usuario u = janela.getUsuario();
        iniciais.setText(Formatos.iniciais(u.getNome()));
        nome.setText(u.getNome());
        email.setText(u.getEmail());
        cabecalhoFidelidade.getChildren().add(0, Icones.criar("presente", 20));
        botaoExcluir.setGraphic(Icones.criar("lixeira", 16));
        recarregar();
    }

    public void recarregar() {
        janela.executar(() -> {
            int concluidos = Sistema.AGENDAMENTOS.listarDoCliente(janela.getUsuario()).stream()
                    .filter(a -> a.getStatus() == br.com.fatec.agendaflow.model.StatusAgendamento.CONCLUIDO)
                    .mapToInt(a -> 1).sum();
            int meta = SeletorPrecoStrategy.ATENDIMENTOS_PARA_FIDELIDADE;
            CalculoPrecoStrategy estrategia = Sistema.AGENDAMENTOS.estrategiaPara(janela.getUsuario());

            if (concluidos >= meta) {
                textoFidelidade.setText("Você já tem " + concluidos + " atendimentos concluídos: ganha "
                        + (int) SeletorPrecoStrategy.PERCENTUAL_FIDELIDADE + "% de desconto em todo novo agendamento.");
            } else {
                int faltam = meta - concluidos;
                textoFidelidade.setText(concluidos + " de " + meta + " atendimentos concluídos. Falta"
                        + (faltam > 1 ? "m " : " ") + faltam + " para ganhar "
                        + (int) SeletorPrecoStrategy.PERCENTUAL_FIDELIDADE + "% de desconto.");
            }
            double proporcao = Math.min(1.0, concluidos / (double) meta);
            barraFidelidade.maxWidthProperty().unbind();
            barraFidelidade.maxWidthProperty().bind(cartaoFidelidade.widthProperty().subtract(40).multiply(proporcao));
            regraPreco.setText("Regra de preço atual: " + estrategia.descricao());
        });
    }

    @FXML
    private void excluirConta() {
        PasswordField senha = new PasswordField();
        senha.setPromptText("Digite sua senha para confirmar");
        janela.dialogo("Excluir sua conta?",
                "Seu nome e e-mail serão apagados e os agendamentos em aberto, cancelados. Não dá para desfazer.",
                senha, "Excluir conta", true, () -> {
                    Sistema.USUARIOS.excluirConta(janela.getUsuario(), senha.getText());
                    janela.sair();
                    janela.aviso("Sua conta foi excluída.", Janela.TipoAviso.INFO);
                });
        senha.requestFocus();
    }
}
