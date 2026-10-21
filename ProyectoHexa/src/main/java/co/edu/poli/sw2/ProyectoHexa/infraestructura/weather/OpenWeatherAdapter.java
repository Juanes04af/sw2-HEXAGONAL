package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.WeatherProvider;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;

/**
 * Adaptador de salida que implementa {@link WeatherProvider} con las
 * APIs de Open-Meteo.
 *
 * <p>
 * Flujo: primero obtiene las coordenadas de la ciudad con
 * {@link GeoApiAdapter} (geocodificacion) y luego consulta el clima
 * actual con {@link OpenWeatherClient}. Finalmente convierte el JSON
 * en la entidad de dominio {@link Weather}.
 * </p>
 *
 * <p>
 * La API entrega la visibilidad en metros; aqui se convierte a
 * kilometros, que es la unidad que usa el dominio.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class OpenWeatherAdapter implements WeatherProvider {

    /** Metros que tiene un kilometro. */
    private static final double METROS_POR_KM = 1000.0;

    /** Adaptador de geocodificacion. */
    private final GeoApiAdapter geoApiAdapter;

    /** Cliente de la API de clima. */
    private final OpenWeatherClient cliente;

    /**
     * Construye el adaptador.
     *
     * @param geoApiAdapter adaptador de geocodificacion
     * @param cliente       cliente de la API de clima
     */
    public OpenWeatherAdapter(GeoApiAdapter geoApiAdapter, OpenWeatherClient cliente) {
        this.geoApiAdapter = Objects.requireNonNull(geoApiAdapter);
        this.cliente = Objects.requireNonNull(cliente);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Weather obtenerClima(Ciudad ciudad) {
        Coordenadas coordenadas = geoApiAdapter.obtenerCoordenadas(ciudad);
        String json = cliente.consultarActual(coordenadas.getLatitud(), coordenadas.getLongitud());
        return parsear(json);
    }

    /**
     * Interpreta el bloque {@code current} de la respuesta del clima.
     *
     * @param json respuesta de la API
     * @return clima con la visibilidad en kilometros
     * @throws ClimaException si falta alguno de los cinco datos
     */
    static Weather parsear(String json) {
        String actual = json == null ? null : LectorJson.bloque(json, "current");
        if (actual == null) {
            throw new ClimaException("La respuesta del clima no trae datos actuales.");
        }

        return new Weather(
                leer(actual, "temperature_2m"),
                leer(actual, "relative_humidity_2m"),
                leer(actual, "wind_speed_10m"),
                leer(actual, "surface_pressure"),
                leer(actual, "visibility") / METROS_POR_KM);
    }

    /**
     * Lee un valor numerico obligatorio del bloque {@code current}.
     *
     * @param bloque contenido del bloque current
     * @param campo  nombre del campo
     * @return valor del campo
     * @throws ClimaException si el campo no existe o es nulo
     */
    private static double leer(String bloque, String campo) {
        Double valor = LectorJson.numero(bloque, campo);
        if (valor == null) {
            throw new ClimaException("La API del clima no entrego el dato '" + campo + "'.");
        }
        return valor;
    }
}
