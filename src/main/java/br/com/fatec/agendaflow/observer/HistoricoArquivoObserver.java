package br.com.fatec.agendaflow.observer;

import br.com.fatec.agendaflow.model.Agendamento;
import br.com.fatec.agendaflow.model.StatusAgendamento;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observador concreto: grava cada evento de agendamento numa linha do arquivo
 * {@code historico.log}, ao lado do banco. Serve de trilha de auditoria.
 */
public class HistoricoArquivoObserver implements AgendamentoObserver {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final Path arquivo;

    public HistoricoArquivoObserver(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public void atualizar(Agendamento ag, StatusAgendamento statusAnterior) {
        String evento = statusAnterior == null
                ? "CRIADO como " + ag.getStatus().getTexto()
                : statusAnterior.getTexto() + " -> " + ag.getStatus().getTexto();
        String linha = String.format("[%s] Agendamento #%d (%s, %s %s): %s%n",
                LocalDateTime.now().format(FORMATO), ag.getId(),
                ag.getServicoNome() == null ? "serviço " + ag.getServicoId() : ag.getServicoNome(),
                ag.getData(), ag.getHoraInicio(), evento);
        try {
            Files.createDirectories(arquivo.getParent());
            Files.writeString(arquivo, linha, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Não foi possível gravar o histórico: " + e.getMessage());
        }
    }
}
