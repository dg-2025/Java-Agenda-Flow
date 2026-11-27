package br.com.fatec.agendaflow.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("CalculoPrecoStrategy — padrão Strategy")
class CalculoPrecoStrategyTest {

    @Test
    @DisplayName("Preço integral devolve o valor sem mudança")
    void precoIntegral() {
        assertEquals(90.0, new PrecoIntegralStrategy().calcular(90.0), 0.001);
    }

    @ParameterizedTest(name = "{1}% sobre R$ {0} = R$ {2}")
    @CsvSource({"100, 10, 90", "50, 10, 45", "70, 10, 63", "99.99, 10, 89.99", "80, 0, 80", "80, 100, 0"})
    @DisplayName("Desconto fidelidade aplica o percentual e arredonda em centavos")
    void descontoFidelidade(double valor, double percentual, double esperado) {
        assertEquals(esperado, new DescontoFidelidadeStrategy(percentual).calcular(valor), 0.001);
    }

    @Test
    @DisplayName("Percentual fora de 0–100 é recusado")
    void percentualInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new DescontoFidelidadeStrategy(-5));
        assertThrows(IllegalArgumentException.class, () -> new DescontoFidelidadeStrategy(150));
    }

    @ParameterizedTest(name = "{0} concluídos → {1}")
    @CsvSource({"0, PrecoIntegralStrategy", "2, PrecoIntegralStrategy",
                "3, DescontoFidelidadeStrategy", "10, DescontoFidelidadeStrategy"})
    @DisplayName("RN-11: o seletor troca de estratégia no 3º atendimento concluído")
    void seletorEscolheEstrategia(int concluidos, String classeEsperada) {
        assertEquals(classeEsperada, SeletorPrecoStrategy.para(concluidos).getClass().getSimpleName());
    }
}
