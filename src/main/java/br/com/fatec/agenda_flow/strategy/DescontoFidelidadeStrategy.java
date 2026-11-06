package br.com.fatec.agenda_flow.strategy;

public class DescontoFidelidadeStrategy implements CalculoPrecoStrategy {
    private float percentual;

    public DescontoFidelidadeStrategy(float percentual) {
        this.percentual = percentual;
    }

    @Override
    public float calcular(float valorOriginal) {
        return valorOriginal - (valorOriginal * (percentual / 100.0f));
    }
}
