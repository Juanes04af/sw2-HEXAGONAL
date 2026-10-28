package co.edu.poli.sw2.ProyectoHexa.infraestructura.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.RutaLoader;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.util.LectorJson;

/**
 * Adaptador de salida que carga la ruta de vuelo desde un archivo
 * JSON incluido dentro del programa.
 *
 * <p>Formato esperado:</p>
 * <pre>
 * {
 *   "origen":  { "nombre": "...", "latitud": 4.60971, "longitud": -74.08175 },
 *   "destino": { "nombre": "...", "latitud": 4.70159, "longitud": -74.14690 }
 * }
 * </pre>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class JsonRutaLoader implements RutaLoader {

    /** Ubicacion del archivo de ruta por defecto dentro del programa. */
    public static final String RUTA_POR_DEFECTO = "/co/edu/poli/sw2/datos/ruta.json";

    /** Ubicacion del recurso a leer. */
    private final String recurso;

    /**
     * Crea el cargador con el archivo de ruta por defecto.
     */
    public JsonRutaLoader() {
        this(RUTA_POR_DEFECTO);
    }

    /**
     * Crea el cargador con otro archivo del programa.
     *
     * @param recurso ubicacion del recurso, por ejemplo "/datos/ruta.json"
     */
    public JsonRutaLoader(String recurso) {
        this.recurso = recurso;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ruta cargarRuta() {
        try (InputStream entrada = JsonRutaLoader.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new RutaException("No se encontro el archivo de ruta " + recurso + ".");
            }
            return parsear(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new RutaException("No fue posible leer el archivo de ruta " + recurso + ".", e);
        }
    }

    /**
     * Convierte el texto JSON en una ruta.
     *
     * @param json contenido del archivo
     * @return ruta con origen y destino
     * @throws RutaException si falta un bloque o un campo, o las
     *         coordenadas no son validas
     */
    static Ruta parsear(String json) {
        if (json == null || json.isBlank()) {
            throw new RutaException("El archivo de ruta esta vacio.");
        }
        return new Ruta(leerPunto(json, "origen"), leerPunto(json, "destino"));
    }

    /**
     * Lee uno de los dos puntos de la ruta.
     *
     * @param json  contenido del archivo
     * @param clave "origen" o "destino"
     * @return punto geografico
     * @throws RutaException si el bloque o alguno de sus campos falta
     */
    private static PuntoGeografico leerPunto(String json, String clave) {
        String bloque = LectorJson.bloque(json, clave);
        if (bloque == null) {
            throw new RutaException("El archivo de ruta no tiene el punto '" + clave + "'.");
        }

        String nombre = LectorJson.texto(bloque, "nombre");
        Double latitud = LectorJson.numero(bloque, "latitud");
        Double longitud = LectorJson.numero(bloque, "longitud");

        if (nombre == null || latitud == null || longitud == null) {
            throw new RutaException("El punto '" + clave
                    + "' debe tener nombre, latitud y longitud.");
        }
        return new PuntoGeografico(nombre, latitud, longitud);
    }
}
