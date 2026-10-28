package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del receptor {@link DroneOperacionService}. Usa un proveedor
 * del clima falso, sin internet.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class DroneOperacionServiceTest {

    private WeatherNotificationService clima;
    private DroneOperacionService receptor;
    private Drone termico;
    private Drone sinTermica;

    @BeforeEach
    void preparar() {
        clima = new WeatherNotificationService(
                new GetWeatherByCityServicio(ciudad -> new Weather(14.6, 70, 10, 1010, 20)));
        receptor = new DroneOperacionService(clima, new Random(42));
        termico = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
        sinTermica = new Vigilancia(2, "VIG-002", "Autel", "EVO II", 1.1, false);
    }

    @Test
    @DisplayName("Las fotografias se numeran por dron")
    void fotografia_numeradaPorDron() {
        assertTrue(receptor.tomarFotografia(termico).startsWith("Fotografia #1 tomada por VIG-001"));
        assertTrue(receptor.tomarFotografia(termico).startsWith("Fotografia #2"));
        assertTrue(receptor.tomarFotografia(sinTermica).startsWith("Fotografia #1 tomada por VIG-002"));
    }

    @Test
    @DisplayName("La fotografia y el escaneo usan camara termica solo si el dron la tiene")
    void camaraTermica() {
        assertTrue(receptor.tomarFotografia(termico).contains("camara termica"));
        assertFalse(receptor.tomarFotografia(sinTermica).contains("camara termica"));
        assertTrue(receptor.escanearZona(termico).contains("camara termica"));
        assertFalse(receptor.escanearZona(sinTermica).contains("camara termica"));
    }

    @Test
    @DisplayName("El escaneo informa el dron y el radio")
    void escaneo_informaRadio() {
        String r = receptor.escanearZona(termico);

        assertTrue(r.startsWith("Zona escaneada por VIG-001 en un radio de "));
        assertTrue(r.contains(" m"));
    }

    @Test
    @DisplayName("Si el dron esta suscrito y notificado, mide la temperatura de su ciudad")
    void temperatura_usaClimaNotificado() {
        clima.suscribir(termico, Ciudad.ZAGREB);
        clima.notificarSuscritos();

        // Otra instancia del mismo dron, como la que entrega la base de datos.
        Drone mismoDrone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
        String r = receptor.medirTemperatura(mismoDrone);

        assertTrue(r.contains("14.6"));
        assertTrue(r.contains("clima notificado"));
    }

    @Test
    @DisplayName("Si el dron no tiene clima notificado, la lectura es simulada entre 10 y 30 grados")
    void temperatura_simulada() {
        String r = receptor.medirTemperatura(sinTermica);

        assertTrue(r.contains("lectura simulada"));
        String numero = r.replaceAll(".*: (\\d+\\.\\d) .*", "$1");
        double valor = Double.parseDouble(numero);
        assertTrue(valor >= 10 && valor <= 30);
    }
}
