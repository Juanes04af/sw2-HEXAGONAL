package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Pruebas de la cadena de responsabilidad. No requieren red ni BD.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class CadenaValidacionClimaTest {

    private final WeatherHandler cadena = CadenaValidacionClima.crear();

    @Test
    @DisplayName("Clima dentro de todos los limites (incluidos los extremos) es apto")
    void climaApto() {
        assertNull(cadena.validar(new Weather(20, 60, 15, 1013, 10)));
        assertNull(cadena.validar(new Weather(0, 85, 40, 950, 5)));
        assertNull(cadena.validar(new Weather(40, 85, 40, 1050, 5)));
    }

    @Test
    @DisplayName("Cada regla incumplida rechaza con su motivo")
    void cadaReglaRechaza() {
        assertTrue(cadena.validar(new Weather(-1, 60, 15, 1013, 10)).startsWith("Temperatura"));
        assertTrue(cadena.validar(new Weather(41, 60, 15, 1013, 10)).startsWith("Temperatura"));
        assertTrue(cadena.validar(new Weather(20, 86, 15, 1013, 10)).startsWith("Humedad"));
        assertTrue(cadena.validar(new Weather(20, 60, 41, 1013, 10)).startsWith("Velocidad del viento"));
        assertTrue(cadena.validar(new Weather(20, 60, 15, 949, 10)).startsWith("Presión"));
        assertTrue(cadena.validar(new Weather(20, 60, 15, 1051, 10)).startsWith("Presión"));
        assertTrue(cadena.validar(new Weather(20, 60, 15, 1013, 4.9)).startsWith("Visibilidad"));
    }

    @Test
    @DisplayName("Si fallan varias reglas, informa la primera de la cadena")
    void informaLaPrimeraFallida() {
        assertTrue(cadena.validar(new Weather(50, 99, 90, 900, 1)).startsWith("Temperatura"));
    }

    @Test
    @DisplayName("La cadena se detiene en el primer rechazo")
    void seDetieneAlFallar() {
        int[] llamadas = { 0 };
        WeatherHandler contador = new WeatherHandler() {
            @Override
            protected String validarCondicion(Weather clima) {
                llamadas[0]++;
                return null;
            }
        };
        WeatherHandler primero = new TemperatureHandler();
        primero.setSiguiente(contador);

        primero.validar(new Weather(99, 60, 15, 1013, 10));   // falla: no avanza
        assertEquals(0, llamadas[0]);

        primero.validar(new Weather(20, 60, 15, 1013, 10));   // pasa: avanza
        assertEquals(1, llamadas[0]);
    }

    @Test
    @DisplayName("Los mensajes finales son los que exige el enunciado")
    void mensajes() {
        assertEquals("CLIMA APTO PARA OPERACIÓN DE DRONES",
                EvaluacionClima.aprobada().getMensaje());
        assertTrue(EvaluacionClima.rechazada("Viento").getMensaje()
                .startsWith("CLIMA NO APTO PARA OPERACIÓN DE DRONES"));
    }
}
