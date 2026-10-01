package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la entidad de dominio {@link Vigilancia}.
 * No requieren base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class VigilanciaTest {

    @Test
    @DisplayName("El constructor asigna todos los atributos")
    void constructor_asignaAtributos() {
        Vigilancia v = new Vigilancia(7, "VIG-001", "DJI", "Mavic 3", 0.9, true);

        assertEquals(7, v.getId());
        assertEquals("VIG-001", v.getSerial());
        assertEquals("DJI", v.getFabricante());
        assertEquals("Mavic 3", v.getModelo());
        assertEquals(0.9, v.getPeso(), 0.0001);
        assertTrue(v.isDeteccionTermica());
    }

    @Test
    @DisplayName("El tipo siempre es VIGILANCIA")
    void getTipo_esVigilancia() {
        Vigilancia v = new Vigilancia(0, "S", "F", "M", 1.0, false);

        assertEquals("VIGILANCIA", v.getTipo());
    }

    @Test
    @DisplayName("Los setters modifican los atributos")
    void setters_modificanAtributos() {
        Vigilancia v = new Vigilancia(0, "S", "F", "M", 1.0, false);

        v.setId(10);
        v.setSerial("VIG-999");
        v.setFabricante("Autel");
        v.setModelo("EVO II");
        v.setPeso(1.2);
        v.setDeteccionTermica(true);

        assertEquals(10, v.getId());
        assertEquals("VIG-999", v.getSerial());
        assertEquals("Autel", v.getFabricante());
        assertEquals("EVO II", v.getModelo());
        assertEquals(1.2, v.getPeso(), 0.0001);
        assertTrue(v.isDeteccionTermica());
    }

    @Test
    @DisplayName("El modelo es opcional y admite null")
    void modelo_admiteNull() {
        Vigilancia v = new Vigilancia(0, "S", "F", null, 1.0, false);

        assertNull(v.getModelo());
    }

    @Test
    @DisplayName("Vigilancia es un Drone serializable")
    void vigilancia_esDroneSerializable() {
        Vigilancia v = new Vigilancia(0, "S", "F", "M", 1.0, false);

        assertTrue(v instanceof Drone);
        assertTrue(v instanceof Serializable);
    }
}
