package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la entidad {@link Agricultura}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class AgriculturaTest {

    @Test
    @DisplayName("El constructor asigna los atributos y el tipo es AGRICULTURA")
    void constructor() {
        Agricultura a = new Agricultura(4, "AGR-001", "DJI", "Agras", 30.0, 20.5);

        assertEquals(4, a.getId());
        assertEquals("AGR-001", a.getSerial());
        assertEquals("DJI", a.getFabricante());
        assertEquals("Agras", a.getModelo());
        assertEquals(30.0, a.getPeso(), 0.0001);
        assertEquals(20.5, a.getCapacidadTanque(), 0.0001);
        assertEquals("AGRICULTURA", a.getTipo());
    }

    @Test
    @DisplayName("El setter modifica la capacidad del tanque")
    void setter() {
        Agricultura a = new Agricultura(0, "S", "F", "M", 1.0, 10.0);

        a.setCapacidadTanque(25.0);

        assertEquals(25.0, a.getCapacidadTanque(), 0.0001);
    }

    @Test
    @DisplayName("Con el mismo id, un dron de agricultura no es igual a uno de vigilancia")
    void distintoTipo_noEsIgual() {
        Drone agricultura = new Agricultura(1, "S", "F", "M", 1.0, 10.0);
        Drone vigilancia = new Vigilancia(1, "S", "F", "M", 1.0, false);

        assertNotEquals(agricultura, vigilancia);
        assertEquals(agricultura, new Agricultura(1, "OTRO", "X", "Y", 2.0, 5.0));
    }
}
