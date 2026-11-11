package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;

/**
 * Integracion con las APIs reales de Open-Meteo. Necesita internet.
 *
 * @author Alejandra Cano y Juan Rosero
 */
@Tag("network")
class OpenMeteoRealIT {

    private final GeoApiAdapter geo = new GeoApiAdapter(new GeoApiClient());
    private final OpenWeatherAdapter adapter =
            new OpenWeatherAdapter(geo, new OpenWeatherClient());

    /** Omite la prueba si el problema es de red; en otro caso relanza el error. */
    private static void omitirSiNoHayRed(ClimaException e) {
        String mensaje = e.getMessage();
        assumeTrue(!(mensaje.startsWith("No fue posible conectarse")
                        || mensaje.contains("respondio con el codigo")),
                "Open-Meteo no disponible: " + mensaje);
        throw e;
    }

    @ParameterizedTest
    @EnumSource(Ciudad.class)
    @DisplayName("Cada ciudad devuelve un clima con valores plausibles en las unidades del dominio")
    void climaPlausible(Ciudad ciudad) {
        Weather w;
        try {
            w = adapter.obtenerClima(ciudad);
        } catch (ClimaException e) {
            omitirSiNoHayRed(e);
            return;
        }

        assertTrue(w.getTemperatura() > -90 && w.getTemperatura() < 60, "temperatura");
        assertTrue(w.getHumedad() >= 0 && w.getHumedad() <= 100, "humedad");
        assertTrue(w.getVelocidadViento() >= 0 && w.getVelocidadViento() < 300, "viento km/h");
        assertTrue(w.getPresion() > 300 && w.getPresion() < 1100, "presion hPa");
        assertTrue(w.getVisibilidad() >= 0 && w.getVisibilidad() < 200, "visibilidad km");
    }

    @Test
    @DisplayName("La geocodificacion de Bogota da coordenadas cercanas y se guarda en cache")
    void geocodificacionBogota() {
        Coordenadas primera;
        try {
            primera = geo.obtenerCoordenadas(Ciudad.BOGOTA);
        } catch (ClimaException e) {
            omitirSiNoHayRed(e);
            return;
        }

        assertEquals(4.6, primera.getLatitud(), 0.5);
        assertEquals(-74.08, primera.getLongitud(), 0.5);
        assertSame(primera, geo.obtenerCoordenadas(Ciudad.BOGOTA));
    }
}
