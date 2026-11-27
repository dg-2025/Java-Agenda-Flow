package br.com.fatec.agendaflow;

/**
 * Ponto de entrada do .jar/.exe. Fica separado de {@link App} porque o JavaFX
 * só sobe a partir de um jar "tudo em um" quando a classe main não estende
 * {@code Application}.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
