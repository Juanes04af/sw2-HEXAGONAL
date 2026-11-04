package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.DroneMediator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.ComponenteMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de {@link MissionService}: recibe un dron existente y lo
 * entrega al mediador sin modificarlo.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class MissionServiceTest {

    /** Mediador de prueba que guarda lo que recibe. */
    private static class MediadorEspia implements DroneMediator {
        Drone drone;
        int nivel;

        @Override
        public ResultadoMision iniciarMision(Drone drone, int nivelBateria) {
            this.drone = drone;
            this.nivel = nivelBateria;
            return new ResultadoMision(drone, List.of(), true, null);
        }

        @Override
        public void notificar(ComponenteMision emisor, EventoMision evento, String detalle) {
            // No se usa en esta prueba.
        }
    }

    @Test
    @DisplayName("Entrega al mediador el mismo dron registrado y su bateria")
    void entregaDronExistente() {
        MediadorEspia mediador = new MediadorEspia();
        Drone registrado = new Vigilancia(7, "VIG-007", "DJI", "Mavic 3", 0.9, true);

        new MissionService(mediador).ejecutarMision(registrado, 75);

        assertSame(registrado, mediador.drone);
        assertEquals(75, mediador.nivel);
    }

    @Test
    @DisplayName("El dron no se modifica al pasar por el servicio")
    void noModificaElDron() {
        Drone registrado = new Vigilancia(7, "VIG-007", "DJI", "Mavic 3", 0.9, true);

        new MissionService(new MediadorEspia()).ejecutarMision(registrado, 50);

        assertEquals(7, registrado.getId());
        assertEquals("VIG-007", registrado.getSerial());
        assertEquals("Mavic 3", registrado.getModelo());
        assertEquals(0.9, registrado.getPeso(), 0.0001);
    }

    @Test
    @DisplayName("Sin dron seleccionado, la operacion es rechazada antes del mediador")
    void sinDron_rechazado() {
        MediadorEspia mediador = new MediadorEspia();

        assertThrows(DronValidacionException.class,
                () -> new MissionService(mediador).ejecutarMision(null, 50));
        assertNull(mediador.drone);
    }
}
