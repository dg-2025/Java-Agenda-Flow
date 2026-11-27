package br.com.fatec.agendaflow.strategy;

/**
 * Padrão GoF <b>Strategy</b> (comportamental) — a "estratégia".
 * <p>
 * Cada implementação é uma forma de calcular o valor cobrado por um serviço.
 * O {@code AgendamentoService} (o "contexto") escolhe a estratégia conforme o
 * cliente (RN-11) e só chama {@link #calcular}, sem {@code if} de desconto
 * espalhado pelo código.
 */
public interface CalculoPrecoStrategy {

    double calcular(double valorServico);

    /** Texto mostrado no resumo do agendamento (ex.: "Desconto fidelidade 10%"). */
    String descricao();
}
