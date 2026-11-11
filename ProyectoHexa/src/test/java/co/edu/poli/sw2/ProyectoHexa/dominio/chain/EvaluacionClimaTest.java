package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del resultado inmutable {@link EvaluacionClima}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class EvaluacionClimaTest {

    @Test
    @DisplayName("Una evaluacion aprobada es apta, no tiene motivo y usa el mensaje de apto")
    void aprobada() {
        EvaluacionClima e = EvaluacionClima.aprobada();

        assertTrue(e.isApto());
        assertNull(e.getMotivo());
        assertEquals(EvaluacionClima.MENSAJE_APTO, e.getMensaje());
    }

    @Test
    @DisplayName("Una evaluacion rechazada no es apta y el mensaje incluye el motivo")
    void rechazada() {
        EvaluacionClima e = EvaluacionClima.rechazada("Viento");

        assertFalse(e.isApto());
        assertEquals("Viento", e.getMotivo());
        assertTrue(e.getMensaje().startsWith(EvaluacionClima.MENSAJE_NO_APTO));
        assertTrue(e.getMensaje().endsWith("Viento"));
    }

    @Test
    @DisplayName("Los dos mensajes finales son distintos")
    void mensajesDistintos() {
        assertNotEquals(EvaluacionClima.MENSAJE_APTO, EvaluacionClima.MENSAJE_NO_APTO);
    }
}
