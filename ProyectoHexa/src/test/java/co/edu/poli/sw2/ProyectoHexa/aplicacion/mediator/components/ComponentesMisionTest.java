package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.DroneMediator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.EventoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Pruebas de cada componente por separado, con un mediador espia que
 * solo registra los eventos que recibe.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ComponentesMisionTest {

    /** Mediador de prueba que guarda los eventos recibidos. */
    private static class MediadorEspia implements DroneMediator {
        final List<EventoMision> eventos = new ArrayList<>();

        @Override
        public ResultadoMision iniciarMision(Drone drone, int nivelBateria) {
            return null;
        }

        @Override
        public void notificar(ComponenteMision emisor, EventoMision evento, String detalle) {
            eventos.add(evento);
        }
    }

    private MediadorEspia mediador;
    private Drone conTermica;
    private Drone sinTermica;

    @BeforeEach
    void preparar() {
        mediador = new MediadorEspia();
        conTermica = new Vigilancia(0, "VIG-T", "Simulado", null, 1.0, true);
        sinTermica = new Vigilancia(0, "VIG-N", "Simulado", null, 1.0, false);
    }

    @Test
    @DisplayName("Navegacion prepara la ruta e informa RUTA_PREPARADA")
    void navegacion() {
        Ruta ruta = new Ruta(new PuntoGeografico("Base", 0, 0), new PuntoGeografico("Zona", 0, 1));
        NavigationSystem n = new NavigationSystem(() -> ruta, (a, b) -> 12.5);
        n.setMediator(mediador);

        String detalle = n.prepararRuta(conTermica);

        assertEquals(List.of(EventoMision.RUTA_PREPARADA), mediador.eventos);
        assertEquals(12.5, n.getDistanciaKm(), 0.0001);
        assertTrue(detalle.contains("Base -> Zona"));
    }

    @Test
    @DisplayName("Bateria informa SUFICIENTE o INSUFICIENTE segun el nivel")
    void bateria() {
        BatterySystem b = new BatterySystem();
        b.setMediator(mediador);

        assertTrue(b.verificarBateria(conTermica, 50, 10));    // requiere 30 %
        assertFalse(b.verificarBateria(conTermica, 29, 10));

        assertEquals(List.of(EventoMision.BATERIA_SUFICIENTE, EventoMision.BATERIA_INSUFICIENTE),
                mediador.eventos);
    }

    @Test
    @DisplayName("Sensores activa la camara termica solo si el dron la tiene")
    void sensores() {
        SensorSystem s = new SensorSystem();
        s.setMediator(mediador);

        assertTrue(s.activarSensores(conTermica).contains("camara termica"));
        assertFalse(s.activarSensores(sinTermica).contains("camara termica"));
        assertEquals(EventoMision.SENSORES_ACTIVADOS, mediador.eventos.get(0));
    }

    @Test
    @DisplayName("Comunicacion establece el enlace e informa ENLACE_ESTABLECIDO")
    void comunicacion() {
        CommunicationSystem c = new CommunicationSystem();
        c.setMediator(mediador);

        assertTrue(c.establecerComunicacion(conTermica).contains("VIG-T"));
        assertEquals(List.of(EventoMision.ENLACE_ESTABLECIDO), mediador.eventos);
    }

    @Test
    @DisplayName("Un componente sin mediador no puede trabajar")
    void sinMediador_falla() {
        assertThrows(IllegalStateException.class,
                () -> new CommunicationSystem().establecerComunicacion(conTermica));
    }
}
