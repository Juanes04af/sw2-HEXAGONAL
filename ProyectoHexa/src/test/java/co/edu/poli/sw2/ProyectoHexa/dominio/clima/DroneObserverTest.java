package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del rol de observador que cumple {@link Drone}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class DroneObserverTest {

    @Test
    @DisplayName("Un dron es un WeatherObserver")
    void drone_esObserver() {
        assertTrue(new Vigilancia(1, "S", "F", "M", 1.0, false) instanceof WeatherObserver);
    }

    @Test
    @DisplayName("Un dron nuevo no tiene clima hasta ser notificado")
    void climaInicial_esNulo() {
        assertNull(new Vigilancia(1, "S", "F", "M", 1.0, false).getClimaActual());
    }

    @Test
    @DisplayName("actualizarClima guarda el clima recibido")
    void actualizarClima_guardaClima() {
        Drone drone = new Vigilancia(1, "S", "F", "M", 1.0, false);
        Weather clima = new Weather(20, 50, 10, 1010, 30);

        drone.actualizarClima(clima);

        assertSame(clima, drone.getClimaActual());
    }

    @Test
    @DisplayName("Dos drones con el mismo id son iguales")
    void equals_porId() {
        Drone a = new Vigilancia(3, "VIG-A", "DJI", "M", 1.0, false);
        Drone b = new Vigilancia(3, "VIG-A-MOD", "Autel", "X", 2.0, true);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Drones con distinto id no son iguales")
    void equals_idDistinto() {
        assertNotEquals(new Vigilancia(1, "S", "F", "M", 1.0, false),
                        new Vigilancia(2, "S", "F", "M", 1.0, false));
    }

    @Test
    @DisplayName("Drones sin guardar (id 0) solo son iguales a si mismos")
    void equals_idCero() {
        Drone a = new Vigilancia(0, "S", "F", "M", 1.0, false);

        assertEquals(a, a);
        assertNotEquals(a, new Vigilancia(0, "S", "F", "M", 1.0, false));
    }
}
