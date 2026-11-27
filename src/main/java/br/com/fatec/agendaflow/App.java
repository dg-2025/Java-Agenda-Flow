package br.com.fatec.agendaflow;

import br.com.fatec.agendaflow.persistencia.Banco;
import br.com.fatec.agendaflow.util.Janela;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/** Aplicação JavaFX: prepara o banco e abre a primeira janela no login. */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Banco.inicializar();
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "Não foi possível abrir o banco de dados:\n" + e.getMessage());
            alerta.setHeaderText("Agenda Flow");
            alerta.showAndWait();
            Platform.exit();
            return;
        }
        Janela janela = new Janela(stage);
        janela.abrir();
        Demonstracao.seAtiva(janela);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
