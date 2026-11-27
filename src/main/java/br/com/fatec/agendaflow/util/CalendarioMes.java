package br.com.fatec.agendaflow.util;

import br.com.fatec.agendaflow.model.Agendamento;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * Visualização de calendário mensal dos agendamentos (usada pelo admin e pelo
 * cliente). Mostra até 3 agendamentos por dia, coloridos pelo status; clicar
 * num dia avisa quem usa o componente para mostrar os detalhes daquele dia.
 */
public class CalendarioMes extends VBox {

    private static final int MAX_POR_DIA = 3;

    private final Label tituloMes = new Label();
    private final GridPane grade = new GridPane();
    private final Function<Agendamento, String> textoDoEvento;

    private YearMonth mes = YearMonth.now();
    private LocalDate selecionado = LocalDate.now();
    private Map<LocalDate, List<Agendamento>> porDia = new TreeMap<>();
    private Consumer<LocalDate> aoSelecionar = d -> { };

    /** @param textoDoEvento o que escrever em cada agendamento (ex.: "09:00 Marina") */
    public CalendarioMes(Function<Agendamento, String> textoDoEvento) {
        this.textoDoEvento = textoDoEvento;
        getStyleClass().add("calendario");
        setSpacing(12);

        tituloMes.getStyleClass().add("titulo");
        Button anterior = botao("voltar", "Mês anterior");
        anterior.setOnAction(e -> irPara(mes.minusMonths(1)));
        Button proximo = botao("avancar", "Próximo mês");
        proximo.setOnAction(e -> irPara(mes.plusMonths(1)));
        Button hoje = new Button("Hoje");
        hoje.getStyleClass().add("botao-pequeno");
        hoje.setOnAction(e -> {
            irPara(YearMonth.now());
            selecionar(LocalDate.now());
        });
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox cabecalho = new HBox(8, tituloMes, espaco, hoje, anterior, proximo);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        GridPane nomesDias = new GridPane();
        nomesDias.getStyleClass().add("cal-semana");
        for (int i = 0; i < 7; i++) {
            DayOfWeek dia = DayOfWeek.MONDAY.plus(i);
            Label l = new Label(dia.getDisplayName(TextStyle.SHORT, Formatos.BR).replace(".", "").toUpperCase());
            l.getStyleClass().add("cal-nome-dia");
            l.setMaxWidth(Double.MAX_VALUE);
            nomesDias.add(l, i, 0);
        }
        colunasIguais(nomesDias);

        grade.setHgap(6);
        grade.setVgap(6);
        nomesDias.setHgap(6);
        colunasIguais(grade);
        VBox.setVgrow(grade, Priority.ALWAYS);

        getChildren().addAll(cabecalho, nomesDias, grade);
        desenhar();
    }

    public void setAoSelecionar(Consumer<LocalDate> aoSelecionar) {
        this.aoSelecionar = aoSelecionar;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        porDia = new TreeMap<>();
        for (Agendamento a : agendamentos) {
            porDia.computeIfAbsent(a.getData(), d -> new ArrayList<>()).add(a);
        }
        porDia.values().forEach(l -> l.sort(Comparator.comparing(Agendamento::getHoraInicio)));
        desenhar();
    }

    public List<Agendamento> doDia(LocalDate dia) {
        return porDia.getOrDefault(dia, List.of());
    }

    public LocalDate getSelecionado() {
        return selecionado;
    }

    public void selecionar(LocalDate dia) {
        selecionado = dia;
        if (!YearMonth.from(dia).equals(mes)) {
            mes = YearMonth.from(dia);
        }
        desenhar();
        aoSelecionar.accept(dia);
    }

    private void irPara(YearMonth novo) {
        mes = novo;
        desenhar();
    }

    private void desenhar() {
        tituloMes.setText(Formatos.capitalizar(mes.getMonth().getDisplayName(TextStyle.FULL, Formatos.BR)) + " de " + mes.getYear());
        grade.getChildren().clear();
        grade.getRowConstraints().clear();

        LocalDate primeiro = mes.atDay(1);
        LocalDate inicio = primeiro.minusDays(primeiro.getDayOfWeek().getValue() - 1L); // segunda-feira
        int semanas = (int) Math.ceil((primeiro.getDayOfWeek().getValue() - 1 + mes.lengthOfMonth()) / 7.0);
        for (int s = 0; s < semanas; s++) {
            RowConstraints r = new RowConstraints();
            r.setVgrow(Priority.ALWAYS);
            r.setMinHeight(78);
            grade.getRowConstraints().add(r);
        }
        for (int i = 0; i < semanas * 7; i++) {
            LocalDate dia = inicio.plusDays(i);
            grade.add(celula(dia), i % 7, i / 7);
        }
    }

    private VBox celula(LocalDate dia) {
        Label numero = new Label(String.valueOf(dia.getDayOfMonth()));
        numero.getStyleClass().add("cal-numero");
        if (dia.equals(LocalDate.now())) {
            numero.getStyleClass().add("cal-numero-hoje");
        }
        VBox c = new VBox(4, numero);
        c.getStyleClass().add("cal-dia");
        if (!YearMonth.from(dia).equals(mes)) {
            c.getStyleClass().add("cal-dia-fora");
        }
        if (dia.equals(selecionado)) {
            c.getStyleClass().add("cal-dia-selecionado");
        }

        List<Agendamento> lista = doDia(dia);
        for (int i = 0; i < Math.min(MAX_POR_DIA, lista.size()); i++) {
            Agendamento a = lista.get(i);
            Label ev = new Label(textoDoEvento.apply(a));
            ev.getStyleClass().addAll("cal-evento", "cal-evento-" + a.getStatus().name().toLowerCase());
            ev.setMaxWidth(Double.MAX_VALUE);
            c.getChildren().add(ev);
        }
        if (lista.size() > MAX_POR_DIA) {
            Label mais = new Label("+" + (lista.size() - MAX_POR_DIA) + " mais");
            mais.getStyleClass().add("cal-mais");
            c.getChildren().add(mais);
        }
        c.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        c.setMinWidth(0);
        c.setOnMouseClicked(e -> selecionar(dia));
        return c;
    }

    private Button botao(String icone, String dica) {
        Button b = new Button();
        b.setGraphic(Icones.criar(icone, 16));
        b.getStyleClass().add("botao-pequeno");
        b.setTooltip(new javafx.scene.control.Tooltip(dica));
        return b;
    }

    private static void colunasIguais(GridPane g) {
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            g.getColumnConstraints().add(cc);
        }
    }
}
