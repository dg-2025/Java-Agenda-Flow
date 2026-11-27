package br.com.fatec.agendaflow.strategy;

/** Estratégia concreta: aplica um percentual de desconto ao cliente fiel (RN-11). */
public class DescontoFidelidadeStrategy implements CalculoPrecoStrategy {

    private final double percentual;

    public DescontoFidelidadeStrategy(double percentual) {
        if (percentual < 0 || percentual > 100) {
            throw new IllegalArgumentException("Percentual de desconto deve estar entre 0 e 100.");
        }
        this.percentual = percentual;
    }

    @Override
    public double calcular(double valorServico) {
        double comDesconto = valorServico * (1 - percentual / 100.0);
        return Math.round(comDesconto * 100.0) / 100.0; // arredonda em centavos
    }

    @Override
    public String descricao() {
        return "Desconto fidelidade " + (percentual % 1 == 0 ? String.valueOf((int) percentual) : String.valueOf(percentual)) + "%";
    }

    public double getPercentual() {
        return percentual;
    }
}
