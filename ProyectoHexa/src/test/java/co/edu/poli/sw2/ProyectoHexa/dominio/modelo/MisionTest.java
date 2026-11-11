package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la entidad {@link Mision}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class MisionTest {

    private final Mision mision = new Mision(7, "Patrulla", "Zona Norte", "2026-11-10");

    @Test
    @DisplayName("Guarda todos sus datos")
    void datos() {
        assertEquals(7, mision.getId());
        assertEquals("Patrulla", mision.getNombre());
        assertEquals("Zona Norte", mision.getUbicacion());
        assertEquals("2026-11-10", mision.getFecha());
    }

    @Test
    @DisplayName("toString muestra el nombre y la ubicacion")
    void toString_muestraUbicacion() {
        assertEquals("Patrulla (Zona Norte)", mision.toString());
    }
}
