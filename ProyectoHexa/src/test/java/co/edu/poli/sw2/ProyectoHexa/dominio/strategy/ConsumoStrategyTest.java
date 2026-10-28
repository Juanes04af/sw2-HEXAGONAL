package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las tres estrategias concretas del patron Strategy.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ConsumoStrategyTest {

    @Test
    @DisplayName("Modo Ahorro: distancia x 0.5")
    void ahorro_multiplicaPorMedio() {
        assertEquals(5.0, new AhorroStrategy().calcularConsumo(10), 0.0001);
    }

    @Test
    @DisplayName("Modo Normal: distancia x 1")
    void normal_multiplicaPorUno() {
        assertEquals(10.0, new NormalStrategy().calcularConsumo(10), 0.0001);
    }

    @Test
    @DisplayName("Modo Alto Rendimiento: distancia x 1.5")
    void altoRendimiento_multiplicaPorUnoYMedio() {
        assertEquals(15.0, new AltoRendimientoStrategy().calcularConsumo(10), 0.0001);
    }

    @Test
    @DisplayName("Para la misma distancia: Ahorro < Normal < Alto Rendimiento")
    void ordenDeConsumo() {
        double d = 12.51;
        double ahorro = new AhorroStrategy().calcularConsumo(d);
        double normal = new NormalStrategy().calcularConsumo(d);
        double alto = new AltoRendimientoStrategy().calcularConsumo(d);

        assertTrue(ahorro < normal);
        assertTrue(normal < alto);
    }

    @Test
    @DisplayName("Distancia cero consume cero en todas las estrategias")
    void distanciaCero() {
        for (ConsumoStrategy e : new ConsumoStrategy[] {
                new AhorroStrategy(), new NormalStrategy(), new AltoRendimientoStrategy() }) {
            assertEquals(0.0, e.calcularConsumo(0), 0.0001);
        }
    }

    @Test
    @DisplayName("Una distancia negativa o NaN es rechazada")
    void distanciaInvalida_rechazada() {
        ConsumoStrategy e = new NormalStrategy();

        assertThrows(IllegalArgumentException.class, () -> e.calcularConsumo(-1));
        assertThrows(IllegalArgumentException.class, () -> e.calcularConsumo(Double.NaN));
    }

    @Test
    @DisplayName("Cada estrategia informa su nombre y su factor")
    void nombreYFactor() {
        assertEquals("Modo Ahorro", new AhorroStrategy().getNombre());
        assertEquals(0.5, new AhorroStrategy().getFactor(), 0.0001);
        assertEquals("Modo Normal", new NormalStrategy().getNombre());
        assertEquals(1.0, new NormalStrategy().getFactor(), 0.0001);
        assertEquals("Modo Alto Rendimiento", new AltoRendimientoStrategy().getNombre());
        assertEquals(1.5, new AltoRendimientoStrategy().getFactor(), 0.0001);
    }
}
