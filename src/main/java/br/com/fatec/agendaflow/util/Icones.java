package br.com.fatec.agendaflow.util;

import java.util.Map;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Ícones de traço (estilo Lucide, grade 24×24) desenhados com SVGPath. */
public final class Icones {

    private static final Map<String, String> PATHS = Map.ofEntries(
            Map.entry("calendario", "M8 2v4 M16 2v4 M3 10h18 M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"),
            Map.entry("lista", "M8 6h13 M8 12h13 M8 18h13 M3 6h.01 M3 12h.01 M3 18h.01"),
            Map.entry("usuario", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2 M12 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8z"),
            Map.entry("tesoura", "M6 3a3 3 0 1 0 0 6a3 3 0 1 0 0-6z M6 15a3 3 0 1 0 0 6a3 3 0 1 0 0-6z M20 4L8.12 15.88 M14.47 14.48L20 20 M8.12 8.12L12 12"),
            Map.entry("equipe", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8z M22 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75"),
            Map.entry("relogio", "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z M12 6v6l4 2"),
            Map.entry("sair", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9"),
            Map.entry("check", "M20 6L9 17l-5-5"),
            Map.entry("x", "M18 6L6 18 M6 6l12 12"),
            Map.entry("menos", "M5 12h14"),
            Map.entry("quadrado", "M5 5h14v14H5z"),
            Map.entry("mais", "M12 5v14 M5 12h14"),
            Map.entry("voltar", "M15 18l-6-6 6-6"),
            Map.entry("avancar", "M9 18l6-6-6-6"),
            Map.entry("lixeira", "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6 M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"),
            Map.entry("brilho", "M12 3l1.9 5.8L20 10.7l-6.1 1.9L12 18.5l-1.9-5.9L4 10.7l6.1-1.9z"),
            Map.entry("janela", "M8 8h12v12H8z M4 16V4h12"),
            Map.entry("falta", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 0 0 8a4 4 0 1 0 0-8z M17 8l5 5 M22 8l-5 5"),
            Map.entry("escudo", "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"),
            Map.entry("email", "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z M22 6l-10 7L2 6"),
            Map.entry("cadeado", "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z M7 11V7a5 5 0 0 1 10 0v4"),
            Map.entry("presente", "M20 12v10H4V12 M2 7h20v5H2z M12 22V7 M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"),
            Map.entry("info", "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0-20z M12 16v-4 M12 8h.01")
    );

    private Icones() {
    }

    /** Ícone com o tamanho em pixels. A cor vem do CSS (classe "icone" → -fx-stroke). */
    public static Node criar(String nome, double tamanho) {
        SVGPath path = new SVGPath();
        path.setContent(PATHS.getOrDefault(nome, PATHS.get("info")));
        path.getStyleClass().add("icone");
        path.setStrokeWidth(2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setScaleX(tamanho / 24.0);
        path.setScaleY(tamanho / 24.0);

        StackPane caixa = new StackPane(new Group(path));
        caixa.getStyleClass().add("icone-caixa");
        caixa.setMinSize(tamanho, tamanho);
        caixa.setPrefSize(tamanho, tamanho);
        caixa.setMaxSize(tamanho, tamanho);
        caixa.setMouseTransparent(true);
        return caixa;
    }
}
