package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoNotificacion;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.Suscripcion;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ClimaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de {@link WeatherNotificationService}. Usa un proveedor del
 * clima falso, por lo que no necesita internet.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class WeatherNotificationServiceTest {

    private final List<Ciudad> consultadas = new ArrayList<>();
    private WeatherNotificationService servicio;
    private Drone vig1;
    private Drone vig2;

    @BeforeEach
    void preparar() {
        consultadas.clear();
        // Proveedor falso: cada ciudad da una temperatura distinta;
        // Tokio simula una caida de la API.
        GetWeatherByCityServicio getWeather = new GetWeatherByCityServicio(ciudad -> {
            consultadas.add(ciudad);
            if (ciudad == Ciudad.TOKIO) {
                throw new ClimaException("API no disponible");
            }
            return new Weather(10 + ciudad.ordinal(), 50, 10, 1010, 20);
        });
        servicio = new WeatherNotificationService(getWeather);
        vig1 = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
        vig2 = new Vigilancia(2, "VIG-002", "Autel", "EVO II", 1.1, false);
    }

    @Test
    @DisplayName("Suscribir registra el dron en el notificador de la ciudad")
    void suscribir_registraEnCiudad() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        assertEquals(Optional.of(Ciudad.BOGOTA), servicio.ciudadDe(vig1));
        assertTrue(servicio.getNotificador(Ciudad.BOGOTA).estaSuscrito(vig1));
    }

    @Test
    @DisplayName("Una ciudad puede tener varios drones")
    void ciudad_admiteVariosDrones() {
        servicio.suscribir(vig1, Ciudad.MADRID);
        servicio.suscribir(vig2, Ciudad.MADRID);

        assertEquals(2, servicio.getNotificador(Ciudad.MADRID).getObservadores().size());
    }

    @Test
    @DisplayName("Suscribir a otra ciudad traslada el dron (una ciudad por dron)")
    void suscribir_otraCiudad_traslada() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);
        servicio.suscribir(vig1, Ciudad.ZAGREB);

        assertEquals(Optional.of(Ciudad.ZAGREB), servicio.ciudadDe(vig1));
        assertFalse(servicio.getNotificador(Ciudad.BOGOTA).estaSuscrito(vig1));
        assertEquals(1, servicio.listarSuscripciones().size());
    }

    @Test
    @DisplayName("Suscribir dos veces a la misma ciudad es rechazado")
    void suscribir_mismaCiudad_rechazado() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        assertThrows(DronValidacionException.class,
                () -> servicio.suscribir(vig1, Ciudad.BOGOTA));
    }

    @Test
    @DisplayName("Suscribir sin dron o sin ciudad es rechazado")
    void suscribir_faltanDatos_rechazado() {
        assertThrows(DronValidacionException.class, () -> servicio.suscribir(null, Ciudad.BOGOTA));
        assertThrows(DronValidacionException.class, () -> servicio.suscribir(vig1, null));
    }

    @Test
    @DisplayName("Desuscribir retira el dron de su ciudad")
    void desuscribir_retira() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        servicio.desuscribir(vig1);

        assertTrue(servicio.ciudadDe(vig1).isEmpty());
        assertTrue(servicio.listarSuscripciones().isEmpty());
    }

    @Test
    @DisplayName("Desuscribir funciona con otra instancia del mismo dron")
    void desuscribir_otraInstanciaMismoId() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        servicio.desuscribir(new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true));

        assertTrue(servicio.ciudadDe(vig1).isEmpty());
    }

    @Test
    @DisplayName("Desuscribir un dron no suscrito es rechazado")
    void desuscribir_noSuscrito_rechazado() {
        assertThrows(DronValidacionException.class, () -> servicio.desuscribir(vig1));
    }

    @Test
    @DisplayName("Notificar consulta solo las ciudades con drones suscritos")
    void notificar_soloCiudadesConSuscriptores() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);
        servicio.suscribir(vig2, Ciudad.MADRID);

        servicio.notificarSuscritos();

        assertEquals(List.of(Ciudad.BOGOTA, Ciudad.MADRID), consultadas);
    }

    @Test
    @DisplayName("Cada dron recibe el clima de su propia ciudad")
    void notificar_cadaDronRecibeSuCiudad() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);
        servicio.suscribir(vig2, Ciudad.MADRID);

        servicio.notificarSuscritos();

        assertEquals(10 + Ciudad.BOGOTA.ordinal(), vig1.getClimaActual().getTemperatura(), 0.001);
        assertEquals(10 + Ciudad.MADRID.ordinal(), vig2.getClimaActual().getTemperatura(), 0.001);
    }

    @Test
    @DisplayName("Un dron no suscrito no recibe notificaciones")
    void notificar_noSuscrito_noRecibe() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        servicio.notificarSuscritos();

        assertNull(vig2.getClimaActual());
    }

    @Test
    @DisplayName("Sin suscriptores no se consulta la API")
    void notificar_sinSuscriptores_noConsulta() {
        ResultadoNotificacion resultado = servicio.notificarSuscritos();

        assertTrue(resultado.estaVacio());
        assertTrue(consultadas.isEmpty());
    }

    @Test
    @DisplayName("Si una ciudad falla, se registra el error y las demas se notifican")
    void notificar_falloEnUnaCiudad_noDetieneLasDemas() {
        servicio.suscribir(vig1, Ciudad.TOKIO);
        servicio.suscribir(vig2, Ciudad.ZAGREB);

        ResultadoNotificacion resultado = servicio.notificarSuscritos();

        assertTrue(resultado.getErrores().containsKey(Ciudad.TOKIO));
        assertTrue(resultado.getNotificadas().containsKey(Ciudad.ZAGREB));
        assertNull(vig1.getClimaActual());
        assertNotNull(vig2.getClimaActual());
    }

    @Test
    @DisplayName("Retirar quita la suscripcion y no falla si no existia")
    void retirar_quitaSinError() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);

        servicio.retirar(vig1);

        assertTrue(servicio.ciudadDe(vig1).isEmpty());
        assertDoesNotThrow(() -> servicio.retirar(vig2));
    }

    @Test
    @DisplayName("Sincronizar reemplaza el dron suscrito y conserva ciudad y clima")
    void sincronizar_reemplazaYConservaClima() {
        servicio.suscribir(vig1, Ciudad.BOGOTA);
        servicio.notificarSuscritos();
        Drone editado = new Vigilancia(1, "VIG-001-EDIT", "DJI", "Mavic 3", 0.9, true);

        servicio.sincronizar(editado);

        Suscripcion unica = servicio.listarSuscripciones().get(0);
        assertSame(editado, unica.getDrone());
        assertEquals(Ciudad.BOGOTA, unica.getCiudad());
        assertNotNull(editado.getClimaActual());
    }
}
