package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Pruebas del caso de uso {@link GetWeatherByCityServicio}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class GetWeatherByCityServicioTest {

    @Test
    @DisplayName("Consultar delega en el proveedor y devuelve su clima")
    void delegaEnElProveedor() {
        Weather esperado = new Weather(20, 50, 10, 1010, 20);
        List<Ciudad> pedidas = new ArrayList<>();
        GetWeatherByCityServicio servicio = new GetWeatherByCityServicio(ciudad -> {
            pedidas.add(ciudad);
            return esperado;
        });

        assertSame(esperado, servicio.consultar(Ciudad.MADRID));
        assertEquals(List.of(Ciudad.MADRID), pedidas);
    }

    @Test
    @DisplayName("Una ciudad nula es rechazada y no se llama al proveedor")
    void ciudadNula() {
        boolean[] llamado = { false };
        GetWeatherByCityServicio servicio = new GetWeatherByCityServicio(ciudad -> {
            llamado[0] = true;
            return new Weather(0, 0, 0, 0, 0);
        });

        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> servicio.consultar(null));

        assertEquals("Debe seleccionar una ciudad.", ex.getMessage());
        assertFalse(llamado[0]);
    }

    @Test
    @DisplayName("Un error del proveedor se propaga como ClimaException")
    void errorDelProveedor() {
        GetWeatherByCityServicio servicio = new GetWeatherByCityServicio(ciudad -> {
            throw new ClimaException("API caida");
        });

        ClimaException ex = assertThrows(ClimaException.class,
                () -> servicio.consultar(Ciudad.TOKIO));

        assertEquals("API caida", ex.getMessage());
    }

    @Test
    @DisplayName("No se acepta un proveedor nulo")
    void proveedorNulo() {
        assertThrows(NullPointerException.class, () -> new GetWeatherByCityServicio(null));
    }
}
