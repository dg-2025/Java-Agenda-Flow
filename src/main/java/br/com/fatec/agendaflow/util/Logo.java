package br.com.fatec.agendaflow.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** A logo do Agenda Flow (a mesma de assets/logo.svg), desenhada em vetor. */
public final class Logo {

    public static final LinearGradient GRADIENTE = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#3B82F6")), new Stop(1, Color.web("#00C2FF")));

    private Logo() {
    }

    public static Node criar(double tamanho) {
        Rectangle fundo = new Rectangle(4, 4, 120, 120);
        fundo.setArcWidth(64);
        fundo.setArcHeight(64);
        fundo.setFill(GRADIENTE);

        Rectangle brilho = new Rectangle(4, 4, 120, 120);
        brilho.setArcWidth(64);
        brilho.setArcHeight(64);
        brilho.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 255, 0.38)), new Stop(0.5, Color.rgb(255, 255, 255, 0))));
        brilho.setStroke(Color.rgb(255, 255, 255, 0.35));
        brilho.setStrokeWidth(1.5);

        SVGPath arco = traco("M88.04 41.96 A34 34 0 1 0 98 66", 9);
        SVGPath ponteiros = traco("M64 48 V66 L77 74", 8);
        SVGPath estrela = new SVGPath();
        estrela.setContent("M96 22 Q97.8 34.2 110 36 Q97.8 37.8 96 50 Q94.2 37.8 82 36 Q94.2 34.2 96 22 Z");
        estrela.setFill(Color.WHITE);

        Group desenho = new Group(fundo, brilho, arco, ponteiros, estrela);
        double escala = tamanho / 128.0;
        desenho.setScaleX(escala);
        desenho.setScaleY(escala);

        StackPane caixa = new StackPane(new Group(desenho));
        caixa.setMinSize(tamanho, tamanho);
        caixa.setPrefSize(tamanho, tamanho);
        caixa.setMaxSize(tamanho, tamanho);
        return caixa;
    }

    private static SVGPath traco(String d, double largura) {
        SVGPath p = new SVGPath();
        p.setContent(d);
        p.setFill(null);
        p.setStroke(Color.WHITE);
        p.setStrokeWidth(largura);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return p;
    }
}
