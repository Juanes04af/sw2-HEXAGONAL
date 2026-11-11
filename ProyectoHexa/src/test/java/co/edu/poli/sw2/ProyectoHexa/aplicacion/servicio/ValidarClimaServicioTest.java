package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.chain.EvaluacionClima;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Pruebas del caso de uso {@link ValidarClimaServicio}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ValidarClimaServicioTest {

    private static final Logger LOG =
            Logger.getLogger(ValidarClimaServicio.class.getName());

    private final List<LogRecord> registros = new ArrayList<>();

    private final Handler espia = new Handler() {
        @Override
        public void publish(LogRecord registro) {
            registros.add(registro);
        }

        @Override
        public void flush() { }

        @Override
        public void close() { }
    };

    private ValidarClimaServicio servicio;

    @BeforeEach
    void preparar() {
        servicio = new ValidarClimaServicio();
        LOG.addHandler(espia);
    }

    @AfterEach
    void limpiar() {
        LOG.removeHandler(espia);
    }

    @Test
    @DisplayName("Un clima dentro de todos los limites es apto y no se registra nada")
    void climaApto() {
        EvaluacionClima e = servicio.validar(new Weather(20, 60, 15, 1013, 10));

        assertTrue(e.isApto());
        assertNull(e.getMotivo());
        assertEquals(EvaluacionClima.MENSAJE_APTO, e.getMensaje());
        assertTrue(registros.isEmpty());
    }

    @Test
    @DisplayName("Los limites exactos de cada regla son aptos")
    void limitesExactos() {
        assertTrue(servicio.validar(new Weather(0, 85, 40, 950, 5)).isApto());
        assertTrue(servicio.validar(new Weather(40, 85, 40, 1050, 5)).isApto());
    }

    @Test
    @DisplayName("Un clima que incumple una regla no es apto y trae el motivo")
    void climaNoApto() {
        EvaluacionClima e = servicio.validar(new Weather(45, 60, 15, 1013, 10));

        assertFalse(e.isApto());
        assertTrue(e.getMotivo().startsWith("Temperatura"));
        assertTrue(e.getMensaje().startsWith(EvaluacionClima.MENSAJE_NO_APTO));
    }

    @Test
    @DisplayName("Si fallan varias reglas, se informa la primera de la cadena")
    void informaLaPrimera() {
        EvaluacionClima e = servicio.validar(new Weather(50, 99, 90, 900, 1));

        assertTrue(e.getMotivo().startsWith("Temperatura"));
    }

    @Test
    @DisplayName("El motivo del rechazo se registra en el log con nivel WARNING")
    void registraElRechazo() {
        servicio.validar(new Weather(20, 90, 15, 1013, 10));

        assertEquals(1, registros.size());
        assertEquals(Level.WARNING, registros.get(0).getLevel());
        assertTrue(registros.get(0).getMessage().contains("Clima rechazado: Humedad"));
    }

    @Test
    @DisplayName("Un clima nulo es rechazado con DronValidacionException")
    void climaNulo() {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> servicio.validar(null));

        assertEquals("No hay clima para validar.", ex.getMessage());
    }
}
