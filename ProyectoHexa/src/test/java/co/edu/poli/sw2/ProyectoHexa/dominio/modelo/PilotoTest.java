package co.edu.poli.sw2.ProyectoHexa.dominio.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la entidad {@link Piloto}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class PilotoTest {

    private final Piloto piloto = new Piloto(3, "Ana Ruiz", 5, "3001234567");

    @Test
    @DisplayName("Guarda todos sus datos")
    void datos() {
        assertEquals(3, piloto.getId());
        assertEquals("Ana Ruiz", piloto.getNombre());
        assertEquals(5, piloto.getExperiencia());
        assertEquals("3001234567", piloto.getTelefono());
    }

    @Test
    @DisplayName("toString muestra el nombre y los anios de experiencia")
    void toString_muestraExperiencia() {
        assertEquals("Ana Ruiz (Exp: 5 anios)", piloto.toString());
    }

    @Test
    @DisplayName("Es serializable")
    void serializable() {
        assertTrue(piloto instanceof Serializable);
    }
}
