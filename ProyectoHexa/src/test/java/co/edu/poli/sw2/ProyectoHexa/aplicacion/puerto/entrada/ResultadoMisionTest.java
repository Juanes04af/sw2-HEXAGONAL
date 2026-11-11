package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de {@link ResultadoMision} y {@link PasoMision}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ResultadoMisionTest {

    private final Drone drone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);

    private static PasoMision paso(int orden) {
        return new PasoMision(orden, "Sistema de bateria", "Bateria suficiente", "detalle", true);
    }

    @Test
    @DisplayName("Una mision iniciada usa el mensaje de iniciada y no tiene motivo")
    void iniciada() {
        ResultadoMision r = new ResultadoMision(drone, List.of(), true, null);

        assertTrue(r.isIniciada());
        assertNull(r.getMotivo());
        assertEquals(ResultadoMision.MENSAJE_INICIADA, r.getMensaje());
        assertSame(drone, r.getDrone());
    }

    @Test
    @DisplayName("Una mision cancelada incluye el motivo en el mensaje")
    void cancelada() {
        ResultadoMision r = new ResultadoMision(drone, List.of(), false, "bateria insuficiente");

        assertFalse(r.isIniciada());
        assertEquals(ResultadoMision.MENSAJE_CANCELADA + ": bateria insuficiente", r.getMensaje());
    }

    @Test
    @DisplayName("La lista de pasos es de solo lectura")
    void pasosDeSoloLectura() {
        ResultadoMision r = new ResultadoMision(drone, List.of(paso(1)), true, null);

        assertThrows(UnsupportedOperationException.class, () -> r.getPasos().add(paso(2)));
    }

    @Test
    @DisplayName("El resultado guarda una copia: cambiar la lista original no lo afecta")
    void copiaDefensiva() {
        List<PasoMision> origen = new ArrayList<>();
        origen.add(paso(1));
        ResultadoMision r = new ResultadoMision(drone, origen, true, null);

        origen.add(paso(2));

        assertEquals(1, r.getPasos().size());
    }

    @Test
    @DisplayName("PasoMision conserva todos sus datos")
    void pasoMision() {
        PasoMision p = new PasoMision(2, "Sistema de sensores", "Sensores activados", "GPS", false);

        assertEquals(2, p.getOrden());
        assertEquals("Sistema de sensores", p.getComponente());
        assertEquals("Sensores activados", p.getEvento());
        assertEquals("GPS", p.getDetalle());
        assertFalse(p.isExitoso());
    }
}
