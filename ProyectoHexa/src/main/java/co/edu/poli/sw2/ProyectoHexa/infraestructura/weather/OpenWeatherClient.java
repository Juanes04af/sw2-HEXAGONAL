package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.util.Locale;

/**
 * Cliente HTTP de la API de clima actual de Open-Meteo.
 *
 * <p>
 * Pide los cinco aspectos que necesita el sistema: temperatura,
 * humedad relativa, velocidad del viento (en km/h), presion en
 * superficie y visibilidad. Devuelve el JSON sin interpretarlo; la
 * interpretacion la hace {@link OpenWeatherAdapter}.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class OpenWeatherClient {

    /** Direccion base del servicio de pronostico. */
    private static final String URL_BASE = "https://api.open-meteo.com/v1/forecast";

    /** Variables del clima actual solicitadas a la API. */
    private static final String VARIABLES =
            "temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,visibility";

    /**
     * Consulta el clima actual en unas coordenadas.
     *
     * @param latitud  latitud en grados
     * @param longitud longitud en grados
     * @return respuesta JSON de la API
     */
    public String consultarActual(double latitud, double longitud) {
        String url = String.format(Locale.US,
                "%s?latitude=%.4f&longitude=%.4f&current=%s&wind_speed_unit=kmh&timezone=auto",
                URL_BASE, latitud, longitud, VARIABLES);
        return ClienteHttp.get(url, "clima");
    }
}
