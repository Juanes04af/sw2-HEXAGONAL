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
 * Pruebas de {@link MissionService}: crea el dron en memoria y lo
 * entrega al mediador, sin usar el repositorio de drones.
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
    @DisplayName("Crea un dron de vigilancia en memoria y lo entrega al mediador")
    void creaDronEnMemoria() {
        MediadorEspia mediador = new MediadorEspia();

        new MissionService(mediador).ejecutarMision("  VIG-OP-01 ", " Mavic 3 ", true, 75);

        assertTrue(mediador.drone instanceof Vigilancia);
        assertEquals(0, mediador.drone.getId());
        assertEquals("VIG-OP-01", mediador.drone.getSerial());
        assertEquals("Mavic 3", mediador.drone.getModelo());
        assertTrue(((Vigilancia) mediador.drone).isDeteccionTermica());
        assertEquals(75, mediador.nivel);
    }

    @Test
    @DisplayName("El modelo es opcional")
    void modeloOpcional() {
        MediadorEspia mediador = new MediadorEspia();

        new MissionService(mediador).ejecutarMision("VIG-1", "  ", false, 50);

        assertNull(mediador.drone.getModelo());
    }

    @Test
    @DisplayName("Un serial invalido es rechazado antes de llegar al mediador")
    void serialInvalido() {
        MediadorEspia mediador = new MediadorEspia();

        assertThrows(DronValidacionException.class,
                () -> new MissionService(mediador).ejecutarMision("", "M", false, 50));
        assertNull(mediador.drone);
    }
}
