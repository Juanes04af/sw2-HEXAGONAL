package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.GetWeatherByCityUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.WeatherProvider;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Caso de uso que obtiene el clima actual de una ciudad a traves del
 * puerto de salida {@link WeatherProvider}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class GetWeatherByCityServicio implements GetWeatherByCityUseCase {

    /** Proveedor externo del clima. */
    private final WeatherProvider weatherProvider;

    /**
     * Construye el servicio.
     *
     * @param weatherProvider puerto de salida hacia la API del clima
     */
    public GetWeatherByCityServicio(WeatherProvider weatherProvider) {
        this.weatherProvider = Objects.requireNonNull(weatherProvider);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si la ciudad es nula
     */
    @Override
    public Weather consultar(Ciudad ciudad) {
        if (ciudad == null) {
            throw new DronValidacionException("Debe seleccionar una ciudad.");
        }
        return weatherProvider.obtenerClima(ciudad);
    }
}
