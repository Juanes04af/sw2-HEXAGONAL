package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;

/**
 * Utilidad comun para hacer peticiones GET con el {@link HttpClient}
 * de Java y devolver el cuerpo de la respuesta como texto.
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class ClienteHttp {

    /** Tiempo maximo de espera de cada peticion. */
    private static final Duration TIEMPO_ESPERA = Duration.ofSeconds(10);

    /** Cliente HTTP compartido; es seguro entre hilos. */
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(TIEMPO_ESPERA)
            .build();

    /** Evita instancias de la utilidad. */
    private ClienteHttp() {
    }

    /**
     * Ejecuta una peticion GET.
     *
     * @param url      direccion completa a consultar
     * @param servicio nombre del servicio, usado en los mensajes de error
     * @return cuerpo de la respuesta en formato JSON
     * @throws ClimaException si no hay conexion o la respuesta no es 200
     */
    static String get(String url, String servicio) {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIEMPO_ESPERA)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> respuesta =
                    CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() != 200) {
                throw new ClimaException("La API de " + servicio
                        + " respondio con el codigo " + respuesta.statusCode() + ".");
            }
            return respuesta.body();

        } catch (IOException e) {
            throw new ClimaException("No fue posible conectarse con la API de "
                    + servicio + ". Revise la conexion a internet.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClimaException("La consulta a la API de " + servicio
                    + " fue interrumpida.", e);
        }
    }
}
