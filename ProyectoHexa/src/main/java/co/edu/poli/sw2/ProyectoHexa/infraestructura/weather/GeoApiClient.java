package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP de la API de geocodificacion de Open-Meteo.
 *
 * <p>
 * Solo se encarga de la comunicacion: arma la URL, hace la peticion y
 * devuelve el JSON sin interpretarlo. La interpretacion la hace
 * {@link GeoApiAdapter}.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class GeoApiClient {

    /** Direccion base del servicio de geocodificacion. */
    private static final String URL_BASE = "https://geocoding-api.open-meteo.com/v1/search";

    /**
     * Busca una ciudad por nombre dentro de un pais.
     *
     * @param nombre     nombre de la ciudad
     * @param codigoPais codigo ISO del pais (por ejemplo "HR")
     * @return respuesta JSON de la API
     */
    public String buscar(String nombre, String codigoPais) {
        String url = URL_BASE
                + "?name=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                + "&count=1&language=es&format=json"
                + "&countryCode=" + URLEncoder.encode(codigoPais, StandardCharsets.UTF_8);
        return ClienteHttp.get(url, "geocodificacion");
    }
}
