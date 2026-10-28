package co.edu.poli.sw2.ProyectoHexa.infraestructura.loader;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Pruebas del cargador de la ruta desde JSON.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class JsonRutaLoaderTest {

    private static final String JSON_VALIDO =
            "{\n  \"origen\": {\n    \"nombre\": \"Punto Origen\",\n    \"latitud\": 4.60971,\n"
          + "    \"longitud\": -74.08175\n  },\n  \"destino\": {\n    \"nombre\": \"Punto Destino\",\n"
          + "    \"latitud\": 4.70159,\n    \"longitud\": -74.14690\n  }\n}";

    @Test
    @DisplayName("Lee origen y destino de un JSON valido")
    void parsea_jsonValido() {
        Ruta ruta = JsonRutaLoader.parsear(JSON_VALIDO);

        assertEquals("Punto Origen", ruta.getOrigen().getNombre());
        assertEquals(4.60971, ruta.getOrigen().getLatitud(), 0.000001);
        assertEquals(-74.08175, ruta.getOrigen().getLongitud(), 0.000001);
        assertEquals("Punto Destino", ruta.getDestino().getNombre());
        assertEquals(4.70159, ruta.getDestino().getLatitud(), 0.000001);
        assertEquals(-74.1469, ruta.getDestino().getLongitud(), 0.000001);
    }

    @Test
    @DisplayName("El orden de los campos no importa")
    void parsea_ordenDistinto() {
        String json = "{\"destino\":{\"longitud\":2,\"latitud\":1,\"nombre\":\"B\"},"
                    + "\"origen\":{\"latitud\":3,\"nombre\":\"A\",\"longitud\":4}}";

        Ruta ruta = JsonRutaLoader.parsear(json);

        assertEquals("A", ruta.getOrigen().getNombre());
        assertEquals(4.0, ruta.getOrigen().getLongitud(), 0.000001);
        assertEquals("B", ruta.getDestino().getNombre());
    }

    @Test
    @DisplayName("Si falta el destino, lanza RutaException")
    void faltaDestino() {
        String json = "{\"origen\":{\"nombre\":\"A\",\"latitud\":1,\"longitud\":2}}";

        RutaException ex = assertThrows(RutaException.class, () -> JsonRutaLoader.parsear(json));
        assertTrue(ex.getMessage().contains("destino"));
    }

    @Test
    @DisplayName("Si a un punto le falta un campo, lanza RutaException")
    void faltaCampo() {
        String json = JSON_VALIDO.replace("\"latitud\": 4.70159,", "");

        assertThrows(RutaException.class, () -> JsonRutaLoader.parsear(json));
    }

    @Test
    @DisplayName("Una coordenada fuera de rango en el JSON es rechazada")
    void coordenadaFueraDeRango() {
        String json = JSON_VALIDO.replace("4.70159", "95.0");

        assertThrows(RutaException.class, () -> JsonRutaLoader.parsear(json));
    }

    @Test
    @DisplayName("Un archivo vacio es rechazado")
    void archivoVacio() {
        assertThrows(RutaException.class, () -> JsonRutaLoader.parsear("  "));
    }

    @Test
    @DisplayName("Carga el archivo ruta.json incluido en el programa")
    void cargaArchivoDelPrograma() {
        Ruta ruta = new JsonRutaLoader().cargarRuta();

        assertEquals("Centro de Bogota", ruta.getOrigen().getNombre());
        assertEquals("Aeropuerto El Dorado", ruta.getDestino().getNombre());
    }

    @Test
    @DisplayName("Si el archivo no existe, lanza RutaException")
    void archivoInexistente() {
        assertThrows(RutaException.class,
                () -> new JsonRutaLoader("/no/existe/ruta.json").cargarRuta());
    }
}
