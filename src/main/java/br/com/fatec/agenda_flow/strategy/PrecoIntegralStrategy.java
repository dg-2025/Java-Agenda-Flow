package br.com.fatec.agenda_flow.strategy;

public class PrecoIntegralStrategy implements CalculoPrecoStrategy {
    @Override
    public float calcular(float valorOriginal) {
        return valorOriginal;
    }
}
