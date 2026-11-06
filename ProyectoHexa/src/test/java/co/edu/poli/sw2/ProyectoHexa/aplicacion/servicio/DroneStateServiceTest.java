package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del caso de uso {@link DroneStateService}. No requieren BD.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class DroneStateServiceTest {

    private DroneStateService servicio;
    private Drone drone;

    @BeforeEach
    void preparar() {
        servicio = new DroneStateService();
        drone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
    }

    private static Drone otraInstancia(int id) {
        return new Vigilancia(id, "VIG-00" + id, "DJI", "Mavic 3", 0.9, true);
    }

    private String estado(Drone d) {
        return servicio.estadoDe(d).toString();
    }

    @Test
    @DisplayName("Un dron que nunca se ha operado esta APAGADO")
    void estadoInicial_apagado() {
        assertEquals("APAGADO", estado(drone));
    }

    @Test
    @DisplayName("Encender, despegar, aterrizar y finalizar recorren el ciclo")
    void cicloCompleto() {
        servicio.encender(drone);
        assertEquals("EN ESPERA", estado(drone));

        servicio.despegar(drone);
        assertEquals("VOLANDO", estado(drone));

        servicio.iniciarAterrizaje(drone);
        assertEquals("ATERRIZANDO", estado(drone));

        servicio.finalizarAterrizaje(drone);
        assertEquals("EN ESPERA", estado(drone));

        servicio.apagar(drone);
        assertEquals("APAGADO", estado(drone));
    }

    @Test
    @DisplayName("Una operacion no permitida se rechaza y el estado no cambia")
    void operacionInvalida_rechazada() {
        assertThrows(TransicionInvalidaException.class, () -> servicio.despegar(drone));
        assertEquals("APAGADO", estado(drone));

        servicio.encender(drone);
        assertThrows(TransicionInvalidaException.class, () -> servicio.encender(drone));
        assertThrows(TransicionInvalidaException.class, () -> servicio.finalizarAterrizaje(drone));
        assertEquals("EN ESPERA", estado(drone));
    }

    @Test
    @DisplayName("Un dron aterrizando no puede despegar hasta finalizar el aterrizaje")
    void aterrizando_noPuedeDespegar() {
        servicio.encender(drone);
        servicio.despegar(drone);
        servicio.iniciarAterrizaje(drone);

        assertThrows(TransicionInvalidaException.class, () -> servicio.despegar(drone));
        assertEquals("ATERRIZANDO", estado(drone));
    }

    @Test
    @DisplayName("El estado se conserva aunque la BD entregue otra instancia del mismo dron")
    void estadoSeConserva_porId() {
        servicio.encender(drone);

        Drone recargado = otraInstancia(1);

        assertEquals("EN ESPERA", estado(recargado));
        servicio.despegar(recargado);
        assertEquals("VOLANDO", estado(drone));
    }

    @Test
    @DisplayName("Cada dron tiene su propio estado")
    void estadosIndependientes() {
        Drone otro = otraInstancia(2);

        servicio.encender(drone);

        assertEquals("EN ESPERA", estado(drone));
        assertEquals("APAGADO", estado(otro));
    }

    @Test
    @DisplayName("Operar o consultar sin dron es rechazado")
    void sinDron_rechazado() {
        assertThrows(DronValidacionException.class, () -> servicio.encender(null));
        assertThrows(DronValidacionException.class, () -> servicio.estadoDe(null));
    }
}
