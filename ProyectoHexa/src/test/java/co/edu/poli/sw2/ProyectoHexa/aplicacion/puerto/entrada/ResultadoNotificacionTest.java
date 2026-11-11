package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Pruebas del resultado de una ronda de notificaciones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ResultadoNotificacionTest {

    private final Weather clima = new Weather(20, 50, 10, 1010, 20);

    @Test
    @DisplayName("Un resultado nuevo esta vacio")
    void nuevo_estaVacio() {
        ResultadoNotificacion r = new ResultadoNotificacion();

        assertTrue(r.estaVacio());
        assertTrue(r.getNotificadas().isEmpty());
        assertTrue(r.getErrores().isEmpty());
    }

    @Test
    @DisplayName("registrarExito guarda el clima de la ciudad")
    void registrarExito() {
        ResultadoNotificacion r = new ResultadoNotificacion();

        r.registrarExito(Ciudad.BOGOTA, clima);

        assertFalse(r.estaVacio());
        assertSame(clima, r.getNotificadas().get(Ciudad.BOGOTA));
        assertTrue(r.getErrores().isEmpty());
    }

    @Test
    @DisplayName("registrarError guarda el motivo de la ciudad que fallo")
    void registrarError() {
        ResultadoNotificacion r = new ResultadoNotificacion();

        r.registrarError(Ciudad.TOKIO, "API no disponible");

        assertFalse(r.estaVacio());
        assertEquals("API no disponible", r.getErrores().get(Ciudad.TOKIO));
        assertTrue(r.getNotificadas().isEmpty());
    }

    @Test
    @DisplayName("Los mapas son de solo lectura")
    void mapasDeSoloLectura() {
        ResultadoNotificacion r = new ResultadoNotificacion();

        assertThrows(UnsupportedOperationException.class,
                () -> r.getNotificadas().put(Ciudad.MADRID, clima));
        assertThrows(UnsupportedOperationException.class,
                () -> r.getErrores().put(Ciudad.MADRID, "x"));
    }

    @Test
    @DisplayName("Las ciudades se listan en el orden del enum, no en el de registro")
    void ordenDelEnum() {
        ResultadoNotificacion r = new ResultadoNotificacion();

        r.registrarExito(Ciudad.ZAGREB, clima);
        r.registrarExito(Ciudad.BOGOTA, clima);

        assertEquals(List.of(Ciudad.BOGOTA, Ciudad.ZAGREB),
                new ArrayList<>(r.getNotificadas().keySet()));
    }
}
