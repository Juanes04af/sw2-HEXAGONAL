package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la entidad {@link Sensor}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class SensorTest {

    private final Sensor sensor = new Sensor(1, "Termico", "FLIR");

    @Test
    @DisplayName("Guarda sus datos y toString muestra tipo y fabricante")
    void datosYToString() {
        assertEquals(1, sensor.getId());
        assertEquals("Termico", sensor.getTipo());
        assertEquals("FLIR", sensor.getFabricante());
        assertEquals("Termico - FLIR", sensor.toString());
    }

    @Test
    @DisplayName("Dos sensores con el mismo id son iguales y tienen el mismo hash")
    void igualesPorId() {
        Sensor otro = new Sensor(1, "LiDAR", "Velodyne");

        assertEquals(sensor, otro);
        assertEquals(sensor.hashCode(), otro.hashCode());
    }

    @Test
    @DisplayName("Sensores con distinto id, null u otro tipo de objeto no son iguales")
    void noIguales() {
        assertEquals(sensor, sensor);
        assertNotEquals(sensor, new Sensor(2, "Termico", "FLIR"));
        assertNotEquals(sensor, null);
        assertNotEquals(sensor, "texto");
    }

    @Test
    @DisplayName("Es serializable")
    void serializable() {
        assertTrue(sensor instanceof Serializable);
    }
}
