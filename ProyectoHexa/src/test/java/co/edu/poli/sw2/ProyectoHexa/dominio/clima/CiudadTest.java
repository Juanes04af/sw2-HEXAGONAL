package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Pruebas del enum {@link Ciudad}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class CiudadTest {

    @ParameterizedTest
    @CsvSource({
        "BOGOTA, Bogota, CO, Bogota (Colombia)",
        "ZAGREB, Zagreb, HR, Zagreb (Croacia)",
        "MADRID, Madrid, ES, Madrid (Espana)",
        "TOKIO, Tokyo, JP, Tokio (Japon)",
        "BUENOS_AIRES, Buenos Aires, AR, Buenos Aires (Argentina)"
    })
    @DisplayName("Cada ciudad guarda su nombre de consulta, su pais y su nombre visible")
    void datosDeCadaCiudad(Ciudad ciudad, String consulta, String pais, String visible) {
        assertEquals(consulta, ciudad.getNombreConsulta());
        assertEquals(pais, ciudad.getCodigoPais());
        assertEquals(visible, ciudad.getNombreVisible());
    }

    @Test
    @DisplayName("Hay cinco ciudades y sus codigos de pais no se repiten")
    void cincoCiudadesCodigosUnicos() {
        Set<String> codigos = new HashSet<>();
        for (Ciudad c : Ciudad.values()) {
            codigos.add(c.getCodigoPais());
        }

        assertEquals(5, Ciudad.values().length);
        assertEquals(5, codigos.size());
    }

    @Test
    @DisplayName("toString devuelve el nombre visible (lo usan los ComboBox)")
    void toString_esNombreVisible() {
        for (Ciudad c : Ciudad.values()) {
            assertEquals(c.getNombreVisible(), c.toString());
        }
    }
}
