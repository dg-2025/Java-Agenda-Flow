package br.com.fatec.agendaflow.strategy;

/** Estratégia concreta: cobra o valor cheio do serviço. */
public class PrecoIntegralStrategy implements CalculoPrecoStrategy {

    @Override
    public double calcular(double valorServico) {
        return valorServico;
    }

    @Override
    public String descricao() {
        return "Preço integral";
    }
}
