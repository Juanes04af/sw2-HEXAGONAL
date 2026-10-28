package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;

/**
 * Pruebas de {@link PuntoGeografico} y {@link Ruta}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class PuntoGeograficoTest {

    @Test
    @DisplayName("Un punto valido guarda nombre y coordenadas")
    void puntoValido() {
        PuntoGeografico p = new PuntoGeografico("  Centro ", 4.60971, -74.08175);

        assertEquals("Centro", p.getNombre());
        assertEquals(4.60971, p.getLatitud(), 0.000001);
        assertEquals(-74.08175, p.getLongitud(), 0.000001);
    }

    @Test
    @DisplayName("Los limites de latitud y longitud son validos")
    void limitesValidos() {
        assertDoesNotThrow(() -> new PuntoGeografico("Polo", 90, 180));
        assertDoesNotThrow(() -> new PuntoGeografico("Polo", -90, -180));
    }

    @Test
    @DisplayName("Una latitud fuera de -90..90 es rechazada")
    void latitudInvalida() {
        assertThrows(RutaException.class, () -> new PuntoGeografico("X", 90.1, 0));
        assertThrows(RutaException.class, () -> new PuntoGeografico("X", -91, 0));
    }

    @Test
    @DisplayName("Una longitud fuera de -180..180 es rechazada")
    void longitudInvalida() {
        assertThrows(RutaException.class, () -> new PuntoGeografico("X", 0, 180.5));
        assertThrows(RutaException.class, () -> new PuntoGeografico("X", 0, -181));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void nombreObligatorio() {
        assertThrows(RutaException.class, () -> new PuntoGeografico(null, 0, 0));
        assertThrows(RutaException.class, () -> new PuntoGeografico("  ", 0, 0));
    }

    @Test
    @DisplayName("La ruta exige origen y destino")
    void rutaExigePuntos() {
        PuntoGeografico p = new PuntoGeografico("A", 0, 0);

        assertThrows(RutaException.class, () -> new Ruta(null, p));
        assertThrows(RutaException.class, () -> new Ruta(p, null));
    }
}
