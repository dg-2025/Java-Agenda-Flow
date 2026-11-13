package br.com.fatec.agenda_flow;

import br.com.fatec.agenda_flow.strategy.CalculoPrecoStrategy;
import br.com.fatec.agenda_flow.strategy.DescontoFidelidadeStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DescontoFidelidadeStrategyTest {

    @Test
    @DisplayName("Deve aplicar 10% de desconto sobre R$ 50.00 resultando em R$ 45.00")
    void deveAplicarDezPorCentoDeDesconto() {
        CalculoPrecoStrategy estrategia = new DescontoFidelidadeStrategy(10.0f);
        float resultado = estrategia.calcular(50.0f);
        assertEquals(45.0f, resultado, 0.001f);
    }

    @Test
    @DisplayName("Deve manter o valor original quando o desconto for 0%")
    void deveManterValorComZeroPorCento() {
        CalculoPrecoStrategy estrategia = new DescontoFidelidadeStrategy(0.0f);
        assertEquals(80.0f, estrategia.calcular(80.0f), 0.001f);
    }
}
