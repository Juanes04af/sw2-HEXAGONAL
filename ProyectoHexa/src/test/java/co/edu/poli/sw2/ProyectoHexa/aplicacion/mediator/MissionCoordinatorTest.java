package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.BatterySystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.CommunicationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.ComponenteMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.NavigationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.SensorSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.PasoMision;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Pruebas del mediador concreto {@link MissionCoordinator}. La ruta es
 * falsa y mide 20 km, por lo que la bateria requerida es
 * 20 % de consumo + 20 % de reserva = 40 %.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class MissionCoordinatorTest {

    private NavigationSystem navegacion;
    private SensorSystem sensores;
    private BatterySystem bateria;
    private CommunicationSystem comunicacion;
    private MissionCoordinator coordinador;
    private Drone drone;

    @BeforeEach
    void preparar() {
        Ruta ruta = new Ruta(new PuntoGeografico("Base", 0, 0), new PuntoGeografico("Zona", 1, 1));
        navegacion = new NavigationSystem(() -> ruta, (a, b) -> 20.0);
        sensores = new SensorSystem();
        bateria = new BatterySystem();
        comunicacion = new CommunicationSystem();
        coordinador = new MissionCoordinator(navegacion, sensores, bateria, comunicacion);
        drone = new Vigilancia(0, "VIG-OP-01", "Simulado", "Mavic 3", 1.0, true);
    }

    @Test
    @DisplayName("Con bateria suficiente, la mision inicia y sigue la secuencia completa")
    void misionIniciada_secuenciaCompleta() {
        ResultadoMision r = coordinador.iniciarMision(drone, 80);

        assertTrue(r.isIniciada());
        assertEquals("MISION DE VIGILANCIA INICIADA", r.getMensaje());

        List<PasoMision> p = r.getPasos();
        assertEquals(5, p.size());
        assertEquals("Sistema de navegacion", p.get(0).getComponente());
        assertEquals("Sistema de bateria", p.get(1).getComponente());
        assertEquals("Sistema de sensores", p.get(2).getComponente());
        assertEquals("Sistema de comunicacion", p.get(3).getComponente());
        assertEquals("Coordinador de mision", p.get(4).getComponente());
        assertTrue(p.stream().allMatch(PasoMision::isExitoso));
    }

    @Test
    @DisplayName("Con bateria insuficiente, la mision se cancela y no activa sensores ni enlace")
    void bateriaInsuficiente_cancela() {
        ResultadoMision r = coordinador.iniciarMision(drone, 30);

        assertFalse(r.isIniciada());
        assertTrue(r.getMensaje().startsWith("MISION CANCELADA: bateria insuficiente"));
        assertEquals(2, r.getPasos().size());
        assertFalse(r.getPasos().get(1).isExitoso());
        assertTrue(r.getPasos().stream()
                .noneMatch(p -> p.getComponente().equals("Sistema de sensores")));
    }

    @Test
    @DisplayName("El limite exacto (40 %) es suficiente")
    void limiteExacto() {
        assertTrue(coordinador.iniciarMision(drone, 40).isIniciada());
        assertFalse(coordinador.iniciarMision(drone, 39).isIniciada());
    }

    @Test
    @DisplayName("La distancia de navegacion llega a bateria a traves del mediador")
    void distanciaPasaPorElMediador() {
        ResultadoMision r = coordinador.iniciarMision(drone, 80);

        assertTrue(r.getPasos().get(0).getDetalle().contains("20.00 km"));
        assertTrue(r.getPasos().get(1).getDetalle().contains("consumo 20.0 %"));
    }

    @Test
    @DisplayName("Si la ruta no se puede cargar, la mision se cancela con el error")
    void rutaFalla_cancela() {
        NavigationSystem rota = new NavigationSystem(() -> {
            throw new RutaException("No se encontro el archivo de ruta.");
        }, (a, b) -> 0);
        MissionCoordinator c = new MissionCoordinator(rota, new SensorSystem(),
                new BatterySystem(), new CommunicationSystem());

        ResultadoMision r = c.iniciarMision(drone, 100);

        assertFalse(r.isIniciada());
        assertTrue(r.getMensaje().contains("No se encontro el archivo de ruta"));
    }

    @Test
    @DisplayName("El coordinador se registra como mediador de los cuatro componentes")
    void registraMediador() {
        assertSame(coordinador, navegacion.getMediator());
        assertSame(coordinador, sensores.getMediator());
        assertSame(coordinador, bateria.getMediator());
        assertSame(coordinador, comunicacion.getMediator());
    }

    @Test
    @DisplayName("Ningun componente tiene referencias a otro componente")
    void componentesNoSeConocen() {
        Class<?>[] clases = { NavigationSystem.class, SensorSystem.class,
                BatterySystem.class, CommunicationSystem.class, ComponenteMision.class };
        for (Class<?> clase : clases) {
            for (Field campo : clase.getDeclaredFields()) {
                assertFalse(ComponenteMision.class.isAssignableFrom(campo.getType()),
                        clase.getSimpleName() + " conoce a otro componente: " + campo.getName());
            }
        }
    }

    @Test
    @DisplayName("Cada mision empieza limpia, sin pasos de la anterior")
    void misionesIndependientes() {
        coordinador.iniciarMision(drone, 10);
        ResultadoMision segunda = coordinador.iniciarMision(drone, 90);

        assertTrue(segunda.isIniciada());
        assertEquals(5, segunda.getPasos().size());
    }

    @Test
    @DisplayName("Un dron con camara termica la activa en sus sensores")
    void sensoresTermicos() {
        ResultadoMision r = coordinador.iniciarMision(drone, 80);

        assertTrue(r.getPasos().get(2).getDetalle().contains("camara termica"));
    }

    @Test
    @DisplayName("Datos invalidos son rechazados")
    void datosInvalidos() {
        assertThrows(DronValidacionException.class, () -> coordinador.iniciarMision(null, 50));
        assertThrows(DronValidacionException.class, () -> coordinador.iniciarMision(drone, -1));
        assertThrows(DronValidacionException.class, () -> coordinador.iniciarMision(drone, 101));
    }
}
