package br.com.fatec.agendaflow.strategy;

/**
 * Decide qual estratégia de preço vale para o cliente (RN-11): a partir de
 * {@value #ATENDIMENTOS_PARA_FIDELIDADE} atendimentos concluídos, o cliente
 * ganha {@value #PERCENTUAL_FIDELIDADE}% de desconto.
 */
public final class SeletorPrecoStrategy {

    public static final int ATENDIMENTOS_PARA_FIDELIDADE = 3;
    public static final double PERCENTUAL_FIDELIDADE = 10.0;

    private SeletorPrecoStrategy() {
    }

    public static CalculoPrecoStrategy para(int atendimentosConcluidos) {
        if (atendimentosConcluidos >= ATENDIMENTOS_PARA_FIDELIDADE) {
            return new DescontoFidelidadeStrategy(PERCENTUAL_FIDELIDADE);
        }
        return new PrecoIntegralStrategy();
    }
}
