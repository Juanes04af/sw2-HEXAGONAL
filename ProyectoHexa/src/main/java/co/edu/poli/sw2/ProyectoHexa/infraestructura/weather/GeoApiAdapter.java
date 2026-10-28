package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.util.LectorJson;

/**
 * Adaptador de la geocodificacion: convierte una {@link Ciudad} en sus
 * {@link Coordenadas} usando {@link GeoApiClient}.
 *
 * <p>
 * Como las coordenadas de una ciudad no cambian, se guardan en cache
 * despues de la primera consulta y no se vuelven a pedir.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class GeoApiAdapter {

    /** Cliente de la API de geocodificacion. */
    private final GeoApiClient cliente;

    /** Coordenadas ya resueltas por ciudad. */
    private final Map<Ciudad, Coordenadas> cache = new ConcurrentHashMap<>();

    /**
     * Construye el adaptador.
     *
     * @param cliente cliente de geocodificacion
     */
    public GeoApiAdapter(GeoApiClient cliente) {
        this.cliente = Objects.requireNonNull(cliente);
    }

    /**
     * Obtiene las coordenadas de una ciudad.
     *
     * @param ciudad ciudad a ubicar
     * @return coordenadas de la ciudad
     * @throws ClimaException si la API falla o no encuentra la ciudad
     */
    public Coordenadas obtenerCoordenadas(Ciudad ciudad) {
        Coordenadas guardadas = cache.get(ciudad);
        if (guardadas != null) {
            return guardadas;
        }
        String json = cliente.buscar(ciudad.getNombreConsulta(), ciudad.getCodigoPais());
        Coordenadas coordenadas = parsear(json, ciudad);
        cache.put(ciudad, coordenadas);
        return coordenadas;
    }

    /**
     * Interpreta la respuesta JSON de la geocodificacion y toma el
     * primer resultado.
     *
     * @param json   respuesta de la API
     * @param ciudad ciudad buscada, usada en los mensajes de error
     * @return coordenadas del primer resultado
     * @throws ClimaException si el JSON no trae resultados validos
     */
    static Coordenadas parsear(String json, Ciudad ciudad) {
        String primero = json == null ? null : LectorJson.primerElemento(json, "results");
        if (primero == null) {
            throw new ClimaException("No se encontro la ciudad " + ciudad + ".");
        }

        Double latitud = LectorJson.numero(primero, "latitude");
        Double longitud = LectorJson.numero(primero, "longitude");
        if (latitud == null || longitud == null) {
            throw new ClimaException("Respuesta invalida de la geocodificacion para "
                    + ciudad + ".");
        }
        return new Coordenadas(latitud, longitud);
    }
}
