package br.com.fatec.agendaflow.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatação em português do Brasil para as telas. */
public final class Formatos {

    public static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd 'de' MMMM", BR);

    private Formatos() {
    }

    public static String dinheiro(double valor) {
        return NumberFormat.getCurrencyInstance(BR).format(valor).replace(' ', ' ');
    }

    public static String data(LocalDate d) {
        return d == null ? "" : d.format(DATA);
    }

    /** Ex.: "Sexta, 27 de novembro". */
    public static String dataLonga(LocalDate d) {
        String dia = d.getDayOfWeek().getDisplayName(TextStyle.FULL, BR).replace("-feira", "");
        return capitalizar(dia) + ", " + d.format(DIA_MES);
    }

    public static String diaSemanaCurto(LocalDate d) {
        return capitalizar(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, BR).replace(".", ""));
    }

    public static String duracao(int minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        int h = minutos / 60;
        int m = minutos % 60;
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }

    public static String capitalizar(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String iniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "?";
        }
        String[] partes = nome.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[partes.length - 1].substring(0, 1);
        }
        return ini.toUpperCase();
    }
}
