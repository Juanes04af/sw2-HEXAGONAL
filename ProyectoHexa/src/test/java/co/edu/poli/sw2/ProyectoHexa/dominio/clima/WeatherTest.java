package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la entidad inmutable {@link Weather}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class WeatherTest {

    private final Weather clima = new Weather(18.5, 60, 12.3, 1005.2, 24.1);

    @Test
    @DisplayName("El constructor guarda los cinco aspectos")
    void getters() {
        assertEquals(18.5, clima.getTemperatura(), 0.0001);
        assertEquals(60, clima.getHumedad(), 0.0001);
        assertEquals(12.3, clima.getVelocidadViento(), 0.0001);
        assertEquals(1005.2, clima.getPresion(), 0.0001);
        assertEquals(24.1, clima.getVisibilidad(), 0.0001);
    }

    @Test
    @DisplayName("toString resume los cinco aspectos en una linea")
    void toString_resume() {
        assertEquals("18.5 C | 60 % | 12.3 km/h | 1005.2 hPa | 24.1 km", clima.toString());
        assertEquals("-2.5 C | 91 % | 41.3 km/h | 752.4 hPa | 3.2 km",
                new Weather(-2.5, 91, 41.3, 752.4, 3.2).toString());
    }

    @Test
    @DisplayName("toString no depende del idioma del equipo (siempre punto decimal)")
    void toString_independienteDelIdioma() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("18.5 C | 60 % | 12.3 km/h | 1005.2 hPa | 24.1 km", clima.toString());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("Es una clase final y serializable")
    void esFinalYSerializable() {
        assertTrue(Modifier.isFinal(Weather.class.getModifiers()));
        assertTrue(clima instanceof Serializable);
    }

    @Test
    @DisplayName("Sobrevive a una serializacion de ida y vuelta")
    void serializacion() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(clima);
        }

        Weather copia;
        try (ObjectInputStream entrada =
                     new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copia = (Weather) entrada.readObject();
        }

        assertEquals(clima.toString(), copia.toString());
    }
}
