package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;

/**
 * Pruebas de {@link GeoApiAdapter} y {@link OpenWeatherAdapter} con
 * respuestas JSON de ejemplo. No hacen llamadas reales a internet.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class OpenMeteoAdaptersTest {

    /** Respuesta real (resumida) de la geocodificacion de Zagreb. */
    private static final String JSON_GEO =
            "{\"results\":[{\"id\":3186886,\"name\":\"Zagreb\",\"latitude\":45.81444,"
          + "\"longitude\":15.97798,\"country_code\":\"HR\"}],\"generationtime_ms\":0.5}";

    /** Respuesta real (resumida) del clima actual. */
    private static final String JSON_CLIMA =
            "{\"latitude\":45.8125,\"longitude\":15.975,\"current\":{\"time\":\"2026-10-21T12:00\","
          + "\"interval\":900,\"temperature_2m\":14.6,\"relative_humidity_2m\":72,"
          + "\"wind_speed_10m\":9.4,\"surface_pressure\":998.3,\"visibility\":24140.0}}";

    @Test
    @DisplayName("La geocodificacion devuelve latitud y longitud del primer resultado")
    void geo_parsea_coordenadas() {
        Coordenadas c = GeoApiAdapter.parsear(JSON_GEO, Ciudad.ZAGREB);

        assertEquals(45.81444, c.getLatitud(), 0.00001);
        assertEquals(15.97798, c.getLongitud(), 0.00001);
    }

    @Test
    @DisplayName("Si la geocodificacion no encuentra la ciudad, lanza ClimaException")
    void geo_sinResultados_lanzaExcepcion() {
        assertThrows(ClimaException.class,
                () -> GeoApiAdapter.parsear("{\"generationtime_ms\":0.3}", Ciudad.ZAGREB));
        assertThrows(ClimaException.class,
                () -> GeoApiAdapter.parsear("{\"results\":[]}", Ciudad.ZAGREB));
    }

    @Test
    @DisplayName("Un JSON de geocodificacion mal formado lanza ClimaException")
    void geo_jsonInvalido_lanzaExcepcion() {
        assertThrows(ClimaException.class, () -> GeoApiAdapter.parsear("no es json", Ciudad.ZAGREB));
    }

    @Test
    @DisplayName("El clima trae los cinco aspectos y la visibilidad en km")
    void clima_parsea_cincoAspectos() {
        Weather w = OpenWeatherAdapter.parsear(JSON_CLIMA);

        assertEquals(14.6, w.getTemperatura(), 0.0001);
        assertEquals(72, w.getHumedad(), 0.0001);
        assertEquals(9.4, w.getVelocidadViento(), 0.0001);
        assertEquals(998.3, w.getPresion(), 0.0001);
        assertEquals(24.14, w.getVisibilidad(), 0.0001);
    }

    @Test
    @DisplayName("Ignora el bloque current_units y lee solo los valores de current")
    void clima_ignoraCurrentUnits() {
        String completo = "{\"latitude\":45.8125,\"current_units\":{\"time\":\"iso8601\","
              + "\"temperature_2m\":\"\u00B0C\",\"relative_humidity_2m\":\"%\","
              + "\"wind_speed_10m\":\"km/h\",\"surface_pressure\":\"hPa\",\"visibility\":\"m\"},"
              + "\"current\":{\"time\":\"2026-10-21T12:00\",\"interval\":900,"
              + "\"temperature_2m\":-2.5,\"relative_humidity_2m\":91,\"wind_speed_10m\":41.3,"
              + "\"surface_pressure\":752.4,\"visibility\":3200.0}}";

        Weather w = OpenWeatherAdapter.parsear(completo);

        assertEquals(-2.5, w.getTemperatura(), 0.0001);
        assertEquals(91, w.getHumedad(), 0.0001);
        assertEquals(41.3, w.getVelocidadViento(), 0.0001);
        assertEquals(752.4, w.getPresion(), 0.0001);
        assertEquals(3.2, w.getVisibilidad(), 0.0001);
    }

    @Test
    @DisplayName("Un dato nulo en la respuesta lanza ClimaException")
    void clima_datoNulo_lanzaExcepcion() {
        String conNulo = JSON_CLIMA.replace("\"visibility\":24140.0", "\"visibility\":null");

        assertThrows(ClimaException.class, () -> OpenWeatherAdapter.parsear(conNulo));
    }

    @Test
    @DisplayName("Si falta un dato del clima, lanza ClimaException")
    void clima_faltaDato_lanzaExcepcion() {
        String sinVisibilidad = JSON_CLIMA.replace(",\"visibility\":24140.0", "");

        ClimaException ex = assertThrows(ClimaException.class,
                () -> OpenWeatherAdapter.parsear(sinVisibilidad));
        assertTrue(ex.getMessage().contains("visibility"));
    }

    @Test
    @DisplayName("Si falta el bloque current, lanza ClimaException")
    void clima_sinCurrent_lanzaExcepcion() {
        assertThrows(ClimaException.class,
                () -> OpenWeatherAdapter.parsear("{\"latitude\":1.0}"));
    }

    @Test
    @DisplayName("El adaptador consulta primero la geocodificacion y luego el clima")
    void obtenerClima_ordenDeConsultas() {
        List<String> llamadas = new ArrayList<>();

        GeoApiClient geoFalso = new GeoApiClient() {
            @Override
            public String buscar(String nombre, String codigoPais) {
                llamadas.add("geo:" + nombre + "/" + codigoPais);
                return JSON_GEO;
            }
        };
        OpenWeatherClient climaFalso = new OpenWeatherClient() {
            @Override
            public String consultarActual(double latitud, double longitud) {
                llamadas.add("clima:" + latitud);
                return JSON_CLIMA;
            }
        };

        OpenWeatherAdapter adapter =
                new OpenWeatherAdapter(new GeoApiAdapter(geoFalso), climaFalso);
        Weather w = adapter.obtenerClima(Ciudad.ZAGREB);

        assertEquals(List.of("geo:Zagreb/HR", "clima:45.81444"), llamadas);
        assertEquals(14.6, w.getTemperatura(), 0.0001);
    }

    @Test
    @DisplayName("Las coordenadas se guardan en cache: la geocodificacion se pide una vez")
    void geo_usaCache() {
        int[] llamadas = {0};
        GeoApiClient geoFalso = new GeoApiClient() {
            @Override
            public String buscar(String nombre, String codigoPais) {
                llamadas[0]++;
                return JSON_GEO;
            }
        };
        GeoApiAdapter adapter = new GeoApiAdapter(geoFalso);

        adapter.obtenerCoordenadas(Ciudad.ZAGREB);
        adapter.obtenerCoordenadas(Ciudad.ZAGREB);

        assertEquals(1, llamadas[0]);
    }
}
