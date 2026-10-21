package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del sujeto concreto {@link WeatherNotifier} del patron Observer.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class WeatherNotifierTest {

    private WeatherNotifier notifier;
    private Weather clima;

    /** Observador de prueba que guarda todo lo que recibe. */
    private static class ObservadorEspia implements WeatherObserver {
        final List<Weather> recibidos = new ArrayList<>();

        @Override
        public void actualizarClima(Weather clima) {
            recibidos.add(clima);
        }
    }

    @BeforeEach
    void preparar() {
        notifier = new WeatherNotifier(Ciudad.ZAGREB);
        clima = new Weather(18.5, 60, 12.3, 1005.2, 24.1);
    }

    @Test
    @DisplayName("Notificar avisa a todos los observadores suscritos")
    void notificar_avisaATodos() {
        ObservadorEspia a = new ObservadorEspia();
        ObservadorEspia b = new ObservadorEspia();
        notifier.suscribir(a);
        notifier.suscribir(b);

        notifier.notificar(clima);

        assertEquals(1, a.recibidos.size());
        assertEquals(1, b.recibidos.size());
        assertSame(clima, a.recibidos.get(0));
    }

    @Test
    @DisplayName("La notificacion incluye los cinco aspectos climaticos")
    void notificar_incluyeCincoAspectos() {
        ObservadorEspia espia = new ObservadorEspia();
        notifier.suscribir(espia);

        notifier.notificar(clima);

        Weather recibido = espia.recibidos.get(0);
        assertEquals(18.5, recibido.getTemperatura(), 0.0001);
        assertEquals(60, recibido.getHumedad(), 0.0001);
        assertEquals(12.3, recibido.getVelocidadViento(), 0.0001);
        assertEquals(1005.2, recibido.getPresion(), 0.0001);
        assertEquals(24.1, recibido.getVisibilidad(), 0.0001);
    }

    @Test
    @DisplayName("Un observador desuscrito deja de recibir notificaciones")
    void desuscribir_dejaDeRecibir() {
        ObservadorEspia a = new ObservadorEspia();
        ObservadorEspia b = new ObservadorEspia();
        notifier.suscribir(a);
        notifier.suscribir(b);

        notifier.desuscribir(a);
        notifier.notificar(clima);

        assertTrue(a.recibidos.isEmpty());
        assertEquals(1, b.recibidos.size());
        assertFalse(notifier.estaSuscrito(a));
    }

    @Test
    @DisplayName("Suscribir dos veces el mismo observador no lo duplica")
    void suscribir_noDuplica() {
        ObservadorEspia espia = new ObservadorEspia();
        notifier.suscribir(espia);
        notifier.suscribir(espia);

        notifier.notificar(clima);

        assertEquals(1, notifier.getObservadores().size());
        assertEquals(1, espia.recibidos.size());
    }

    @Test
    @DisplayName("El mismo dron leido dos veces de la BD se reconoce como uno solo")
    void suscribir_dronPorId_noDuplica() {
        notifier.suscribir(new Vigilancia(5, "VIG-005", "DJI", "M", 1.0, false));
        notifier.suscribir(new Vigilancia(5, "VIG-005", "DJI", "M", 1.0, false));

        assertEquals(1, notifier.getObservadores().size());
    }

    @Test
    @DisplayName("Notificar sin observadores no falla")
    void notificar_sinObservadores_noFalla() {
        assertFalse(notifier.tieneObservadores());
        assertDoesNotThrow(() -> notifier.notificar(clima));
    }

    @Test
    @DisplayName("Desuscribir un observador no registrado no falla")
    void desuscribir_noRegistrado_noFalla() {
        assertDoesNotThrow(() -> notifier.desuscribir(new ObservadorEspia()));
    }

    @Test
    @DisplayName("No se aceptan observadores ni climas nulos")
    void nulos_sonRechazados() {
        assertThrows(NullPointerException.class, () -> notifier.suscribir(null));
        assertThrows(NullPointerException.class, () -> notifier.notificar(null));
    }

    @Test
    @DisplayName("La lista de observadores no se puede modificar desde fuera")
    void getObservadores_esSoloLectura() {
        assertThrows(UnsupportedOperationException.class,
                () -> notifier.getObservadores().add(new ObservadorEspia()));
    }

    @Test
    @DisplayName("El notificador conoce su ciudad")
    void getCiudad() {
        assertEquals(Ciudad.ZAGREB, notifier.getCiudad());
    }
}
