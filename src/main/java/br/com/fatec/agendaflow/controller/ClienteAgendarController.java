package br.com.fatec.agendaflow.controller;

import br.com.fatec.agendaflow.Sistema;
import br.com.fatec.agendaflow.model.HorarioTrabalho;
import br.com.fatec.agendaflow.model.Profissional;
import br.com.fatec.agendaflow.model.Servico;
import br.com.fatec.agendaflow.service.AgendamentoService.Resumo;
import br.com.fatec.agendaflow.util.Formatos;
import br.com.fatec.agendaflow.util.Icones;
import br.com.fatec.agendaflow.util.Janela;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Fluxo de agendamento do cliente em 4 passos:
 * 1 serviço (RF-07) → 2 profissional (RF-08) → 3 data e horário (RF-09) → 4 resumo e confirmação (RF-10/11).
 */
public class ClienteAgendarController {

    private static final String[] NOMES_ETAPAS = {"Serviço", "Profissional", "Data e horário", "Confirmar"};
    private static final int DIAS_NA_FAIXA = 14;

    @FXML private Label titulo;
    @FXML private Label subtitulo;
    @FXML private Button botaoVoltar;
    @FXML private HBox etapas;
    @FXML private ScrollPane rolagem;
    @FXML private VBox corpo;

    private Janela janela;
    private Runnable aoConcluir;

    private int etapa = 1;
    private Servico servico;
    private Profissional profissional;
    private boolean profissionalAutomatico;
    private LocalDate data;
    private LocalTime horario;
    private Set<DayOfWeek> diasDeTrabalho = EnumSet.noneOf(DayOfWeek.class);

    public void iniciar(Janela janela, Runnable aoConcluir) {
        this.janela = janela;
        this.aoConcluir = aoConcluir;
        botaoVoltar.setGraphic(Icones.criar("voltar", 16));
        recomecar();
    }

    public void recomecar() {
        servico = null;
        profissional = null;
        data = null;
        horario = null;
        irPara(1);
    }

    private void irPara(int novaEtapa) {
        etapa = novaEtapa;
        desenharEtapas();
        botaoVoltar.setVisible(etapa > 1);
        corpo.getChildren().clear();
        rolagem.setVvalue(0);
        switch (etapa) {
            case 1 -> mostrarServicos();
            case 2 -> mostrarProfissionais();
            case 3 -> mostrarDataHorario();
            default -> mostrarResumo();
        }
    }

    @FXML
    private void voltar() {
        if (etapa == 3 && profissionalAutomatico) {
            irPara(1);
        } else if (etapa > 1) {
            irPara(etapa - 1);
        }
    }

    // ------------------------------------------------------------ passo 1: serviço (RF-07)

    private void mostrarServicos() {
        subtitulo.setText("Qual serviço você quer fazer?");
        janela.executar(() -> {
            List<Servico> ativos = Sistema.SERVICOS.listarAtivos();
            if (ativos.isEmpty()) {
                corpo.getChildren().add(vazio("tesoura", "Nenhum serviço disponível no momento."));
                return;
            }
            FlowPane grade = grade();
            for (Servico s : ativos) {
                Label nome = new Label(s.getNome());
                nome.getStyleClass().add("subtitulo");
                Label desc = new Label(s.getDescricao() == null ? "" : s.getDescricao());
                desc.getStyleClass().add("texto-suave");
                desc.setWrapText(true);
                Label duracao = new Label(Formatos.duracao(s.getDuracaoMinutos()));
                duracao.getStyleClass().add("etiqueta");
                Label preco = new Label(Formatos.dinheiro(s.getValor()));
                preco.getStyleClass().add("preco");
                Region espaco = new Region();
                HBox.setHgrow(espaco, Priority.ALWAYS);
                HBox rodape = new HBox(8, duracao, espaco, preco);
                rodape.setAlignment(Pos.CENTER_LEFT);
                Region cresce = new Region();
                VBox.setVgrow(cresce, Priority.ALWAYS);

                VBox cartao = cartao(iconeCartao("tesoura"), nome, desc, cresce, rodape);
                cartao.setOnMouseClicked(e -> escolherServico(s));
                grade.getChildren().add(cartao);
            }
            corpo.getChildren().add(grade);
        });
    }

    private void escolherServico(Servico s) {
        janela.executar(() -> {
            List<Profissional> disponiveis = Sistema.PROFISSIONAIS.listarParaServico(s.getId());
            if (disponiveis.isEmpty()) {
                janela.aviso("Nenhum profissional faz \"" + s.getNome() + "\" no momento.", Janela.TipoAviso.ERRO);
                return;
            }
            servico = s;
            if (disponiveis.size() == 1) {           // RF-08: um só profissional → seleção automática
                profissionalAutomatico = true;
                escolherProfissional(disponiveis.get(0));
                janela.aviso(profissional.getNome() + " é quem faz esse serviço — já selecionamos.", Janela.TipoAviso.INFO);
            } else {
                profissionalAutomatico = false;
                irPara(2);
            }
        });
    }

    // ------------------------------------------------------------ passo 2: profissional (RF-08)

    private void mostrarProfissionais() {
        subtitulo.setText("Com quem? Mostrando quem faz " + servico.getNome().toLowerCase() + ".");
        janela.executar(() -> {
            FlowPane grade = grade();
            for (Profissional p : Sistema.PROFISSIONAIS.listarParaServico(servico.getId())) {
                StackPane avatar = new StackPane(new Label(Formatos.iniciais(p.getNome())));
                avatar.getStyleClass().add("avatar");
                avatar.setMinSize(52, 52);
                avatar.setMaxSize(52, 52);
                Label nome = new Label(p.getNome());
                nome.getStyleClass().add("subtitulo");
                Label dias = new Label(textoDias(Sistema.PROFISSIONAIS.listarHorarios(p.getId())));
                dias.getStyleClass().add("texto-suave");
                dias.setWrapText(true);
                VBox cartao = cartao(avatar, nome, dias);
                cartao.setOnMouseClicked(e -> escolherProfissional(p));
                grade.getChildren().add(cartao);
            }
            corpo.getChildren().add(grade);
        });
    }

    private void escolherProfissional(Profissional p) {
        profissional = p;
        data = null;
        irPara(3);
    }

    // ------------------------------------------------------------ passo 3: data e horário (RF-09)

    private void mostrarDataHorario() {
        subtitulo.setText(servico.getNome() + " com " + profissional.getNome() + " · "
                + Formatos.duracao(servico.getDuracaoMinutos()));
        janela.executar(() -> {
            diasDeTrabalho = EnumSet.noneOf(DayOfWeek.class);
            Sistema.PROFISSIONAIS.listarHorarios(profissional.getId()).forEach(h -> diasDeTrabalho.add(h.getDiaSemana()));
        });
        if (diasDeTrabalho.isEmpty()) {
            corpo.getChildren().add(vazio("calendario", profissional.getNome() + " ainda não tem horário de trabalho cadastrado."));
            return;
        }
        if (data == null) {
            data = proximoDiaDeTrabalho(LocalDate.now());
        }

        HBox faixa = new HBox(10);
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < DIAS_NA_FAIXA; i++) {
            faixa.getChildren().add(botaoDia(hoje.plusDays(i)));
        }
        ScrollPane rolagemDias = new ScrollPane(faixa);
        rolagemDias.setFitToHeight(true);
        rolagemDias.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagemDias.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rolagemDias.setMinHeight(92);

        DatePicker outraData = new DatePicker(data);
        outraData.setPromptText("Outra data");
        outraData.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()) || !diasDeTrabalho.contains(item.getDayOfWeek()));
            }
        });
        outraData.valueProperty().addListener((o, a, nova) -> {
            if (nova != null && !nova.equals(data)) {
                data = nova;
                irPara(3);
            }
        });
        Label outraRotulo = new Label("Outra data:");
        outraRotulo.getStyleClass().add("texto-suave");
        HBox linhaOutra = new HBox(10, outraRotulo, outraData);
        linhaOutra.setAlignment(Pos.CENTER_LEFT);

        Label tituloHorarios = new Label("Horários livres em " + Formatos.dataLonga(data).toLowerCase());
        tituloHorarios.getStyleClass().add("subtitulo");

        corpo.getChildren().addAll(rotulo("Escolha o dia"), rolagemDias, linhaOutra, tituloHorarios);

        janela.executar(() -> {
            List<LocalTime> livres = Sistema.AGENDA.horariosDisponiveis(profissional.getId(),
                    servico.getDuracaoMinutos(), data);
            if (livres.isEmpty()) {
                String motivo = diasDeTrabalho.contains(data.getDayOfWeek())
                        ? "Não sobrou horário livre neste dia para um serviço de " + Formatos.duracao(servico.getDuracaoMinutos()) + "."
                        : profissional.getNome() + " não trabalha neste dia da semana.";
                corpo.getChildren().add(vazio("relogio", motivo + " Tente outro dia."));
                return;
            }
            FlowPane horas = new FlowPane(10, 10);
            for (LocalTime h : livres) {
                Button b = new Button(h.toString());
                b.getStyleClass().add("horario");
                b.setOnAction(e -> {
                    horario = h;
                    irPara(4);
                });
                horas.getChildren().add(b);
            }
            corpo.getChildren().add(horas);
        });
    }

    private Node botaoDia(LocalDate dia) {
        Label semana = new Label(dia.equals(LocalDate.now()) ? "HOJE" : Formatos.diaSemanaCurto(dia).toUpperCase());
        semana.getStyleClass().add("dia-semana");
        Label numero = new Label(String.valueOf(dia.getDayOfMonth()));
        numero.getStyleClass().add("dia-numero");
        Label mes = new Label(dia.getMonth().getDisplayName(TextStyle.SHORT, Formatos.BR).replace(".", ""));
        mes.getStyleClass().add("dia-mes");
        VBox caixa = new VBox(semana, numero, mes);
        caixa.getStyleClass().add("dia");
        if (dia.equals(data)) {
            caixa.getStyleClass().add("dia-selecionado");
        }
        if (!diasDeTrabalho.contains(dia.getDayOfWeek())) {
            caixa.getStyleClass().add("dia-fechado");
        }
        caixa.setOnMouseClicked(e -> {
            data = dia;
            irPara(3);
        });
        return caixa;
    }

    /** Primeiro dia (a partir de hoje) em que o profissional ainda tem horário livre para o serviço. */
    private LocalDate proximoDiaDeTrabalho(LocalDate inicio) {
        LocalDate d = inicio;
        for (int i = 0; i < DIAS_NA_FAIXA; i++) {
            if (diasDeTrabalho.contains(d.getDayOfWeek())) {
                try {
                    if (!Sistema.AGENDA.horariosDisponiveis(profissional.getId(), servico.getDuracaoMinutos(), d).isEmpty()) {
                        return d;
                    }
                } catch (java.sql.SQLException e) {
                    return d;
                }
            }
            d = d.plusDays(1);
        }
        return inicio;
    }

    // ------------------------------------------------------------ passo 4: resumo e confirmação (RF-10)

    private void mostrarResumo() {
        subtitulo.setText("Confira e confirme. Nada é marcado antes disso.");
        janela.executar(() -> {
            Resumo r = Sistema.AGENDAMENTOS.montarResumo(janela.getUsuario(), servico, profissional, data, horario);

            VBox linhas = new VBox(
                    linhaResumo("tesoura", "Serviço", r.servico().getNome()),
                    linhaResumo("usuario", "Profissional", r.profissional().getNome()),
                    linhaResumo("calendario", "Data", Formatos.dataLonga(r.data())),
                    linhaResumo("relogio", "Horário", r.inicio() + " às " + r.fim() + "  ·  "
                            + Formatos.duracao(r.servico().getDuracaoMinutos())));

            Label total = new Label(Formatos.dinheiro(r.valorFinal()));
            total.getStyleClass().add("valor-grande");
            HBox valores = new HBox(12, total);
            valores.setAlignment(Pos.CENTER_LEFT);
            if (r.temDesconto()) {
                Label original = new Label(Formatos.dinheiro(r.valorServico()));
                original.getStyleClass().add("riscado");
                Label selo = new Label(r.regraPreco(), Icones.criar("presente", 14));
                selo.getStyleClass().add("selo-desconto");
                valores.getChildren().addAll(original, selo);
            }
            Label rotuloValor = new Label("Valor");
            rotuloValor.getStyleClass().add("rotulo");

            Button confirmar = new Button("Confirmar agendamento", Icones.criar("check", 18));
            confirmar.getStyleClass().add("botao-primario");
            confirmar.setDefaultButton(true);
            confirmar.setOnAction(e -> confirmar());
            Button trocar = new Button("Trocar horário");
            trocar.setOnAction(e -> irPara(3));
            HBox botoes = new HBox(10, confirmar, trocar);

            VBox cartao = new VBox(14, linhas, rotuloValor, valores, botoes);
            cartao.getStyleClass().add("vidro-leve");
            cartao.setStyle("-fx-padding: 22 26;");
            cartao.setMaxWidth(620);
            corpo.getChildren().add(cartao);
        });
    }

    private void confirmar() {
        janela.executar(() -> {
            Sistema.AGENDAMENTOS.confirmar(janela.getUsuario(), servico.getId(), profissional.getId(), data, horario);
            janela.aviso("Agendado! " + servico.getNome() + " em " + Formatos.data(data) + " às " + horario + ".",
                    Janela.TipoAviso.SUCESSO);
            recomecar();
            aoConcluir.run();
        });
    }

    // ------------------------------------------------------------ peças visuais

    private void desenharEtapas() {
        etapas.getChildren().clear();
        for (int i = 1; i <= NOMES_ETAPAS.length; i++) {
            StackPane numero = new StackPane();
            numero.getStyleClass().add("etapa-numero");
            if (i < etapa) {
                numero.getChildren().add(Icones.criar("check", 14));
            } else {
                numero.getChildren().add(new Label(String.valueOf(i)));
            }
            Label nome = new Label(NOMES_ETAPAS[i - 1]);
            nome.getStyleClass().add("etapa-nome");
            HBox e = new HBox(numero, nome);
            e.getStyleClass().add("etapa");
            if (i == etapa) {
                e.getStyleClass().add("etapa-ativa");
            } else if (i < etapa) {
                e.getStyleClass().add("etapa-feita");
            }
            etapas.getChildren().add(e);
            if (i < NOMES_ETAPAS.length) {
                Region linha = new Region();
                linha.getStyleClass().add("etapa-linha");
                etapas.getChildren().add(linha);
            }
        }
    }

    private FlowPane grade() {
        FlowPane grade = new FlowPane(14, 14);
        grade.setPrefWrapLength(800);
        return grade;
    }

    private VBox cartao(Node... filhos) {
        VBox c = new VBox(filhos);
        c.getStyleClass().add("cartao-opcao");
        c.setPrefSize(252, 170);
        c.setMinHeight(150);
        return c;
    }

    private Node iconeCartao(String icone) {
        StackPane s = new StackPane(Icones.criar(icone, 22));
        s.getStyleClass().add("cartao-icone");
        return s;
    }

    private Node linhaResumo(String icone, String rotulo, String valor) {
        Label r = new Label(rotulo);
        r.getStyleClass().add("texto-suave");
        r.setMinWidth(110);
        Label v = new Label(valor);
        v.getStyleClass().add("subtitulo");
        HBox h = new HBox(Icones.criar(icone, 18), r, v);
        h.getStyleClass().add("resumo-linha");
        return h;
    }

    private Label rotulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo");
        return l;
    }

    static Node vazio(String icone, String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("texto-suave");
        l.setWrapText(true);
        VBox v = new VBox(Icones.criar(icone, 34), l);
        v.getStyleClass().add("vazio");
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    private static String textoDias(List<HorarioTrabalho> horarios) {
        if (horarios.isEmpty()) {
            return "Sem horário cadastrado";
        }
        StringBuilder sb = new StringBuilder();
        DayOfWeek ultimo = null;
        for (HorarioTrabalho h : horarios) {
            if (h.getDiaSemana() == ultimo) {
                continue;
            }
            ultimo = h.getDiaSemana();
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(h.getDiaSemana().getDisplayName(TextStyle.SHORT, Formatos.BR).replace(".", ""));
        }
        return "Atende: " + sb;
    }
}
