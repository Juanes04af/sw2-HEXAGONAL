package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del automata del patron State: cada estado permite solo sus
 * transiciones y rechaza las demas sin cambiar de estado.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class DroneStateTest {

    /** Orden: encender, apagar, despegar, iniciarAterrizaje, finalizarAterrizaje. */
    private static final List<Consumer<DroneConEstado>> OPERACIONES = List.of(
            DroneConEstado::encender, DroneConEstado::apagar, DroneConEstado::despegar,
            DroneConEstado::iniciarAterrizaje, DroneConEstado::finalizarAterrizaje);

    private static Drone drone() {
        return new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
    }

    private static DroneConEstado nuevo() {
        return new DroneConEstado(drone());
    }

    /** Por cada operacion: clase del estado destino, o null si debe rechazarse. */
    private static void verificar(Function<DroneConEstado, DroneState> inicial, Class<?>... esperados) {
        for (int i = 0; i < OPERACIONES.size(); i++) {
            DroneConEstado ctx = nuevo();
            ctx.cambiarEstado(inicial.apply(ctx));
            Class<?> antes = ctx.getEstadoActual().getClass();
            Consumer<DroneConEstado> operacion = OPERACIONES.get(i);

            if (esperados[i] == null) {
                assertThrows(TransicionInvalidaException.class, () -> operacion.accept(ctx));
                assertEquals(antes, ctx.getEstadoActual().getClass());
            } else {
                operacion.accept(ctx);
                assertEquals(esperados[i], ctx.getEstadoActual().getClass());
            }
        }
    }

    @Test
    @DisplayName("Un dron nuevo inicia APAGADO")
    void dronNuevo_iniciaApagado() {
        assertEquals(OffState.class, nuevo().getEstadoActual().getClass());
        assertEquals("APAGADO", nuevo().getEstadoActual().toString());
    }

    @Test
    @DisplayName("APAGADO solo permite encender")
    void apagado_soloEncender() {
        verificar(OffState::new, ReadyState.class, null, null, null, null);
    }

    @Test
    @DisplayName("EN ESPERA permite despegar o apagar")
    void enEspera_despegarOApagar() {
        verificar(ReadyState::new, null, OffState.class, FlyingState.class, null, null);
    }

    @Test
    @DisplayName("VOLANDO solo permite iniciar el aterrizaje")
    void volando_soloIniciarAterrizaje() {
        verificar(FlyingState::new, null, null, null, LandingState.class, null);
    }

    @Test
    @DisplayName("ATERRIZANDO solo permite finalizar el aterrizaje (no puede volver a volar)")
    void aterrizando_soloFinalizar() {
        verificar(LandingState::new, null, null, null, null, ReadyState.class);
    }

    @Test
    @DisplayName("Ciclo operativo completo")
    void cicloCompleto() {
        DroneConEstado ctx = nuevo();

        ctx.encender();
        assertEquals("EN ESPERA", ctx.getEstadoActual().toString());
        ctx.despegar();
        assertEquals("VOLANDO", ctx.getEstadoActual().toString());
        ctx.iniciarAterrizaje();
        assertEquals("ATERRIZANDO", ctx.getEstadoActual().toString());
        ctx.finalizarAterrizaje();
        assertEquals("EN ESPERA", ctx.getEstadoActual().toString());
        ctx.apagar();
        assertEquals("APAGADO", ctx.getEstadoActual().toString());
    }

    @Test
    @DisplayName("El mensaje de rechazo indica la operacion, el dron, el estado y lo permitido")
    void mensajeDeRechazo() {
        TransicionInvalidaException ex = assertThrows(TransicionInvalidaException.class,
                () -> nuevo().despegar());

        assertTrue(ex.getMessage().contains("despegar"));
        assertTrue(ex.getMessage().contains("VIG-001"));
        assertTrue(ex.getMessage().contains("APAGADO"));
        assertTrue(ex.getMessage().contains("Solo puede encenderse."));
    }

    @Test
    @DisplayName("El estado no se comparte entre drones")
    void estadoIndependientePorDron() {
        DroneConEstado a = nuevo();
        DroneConEstado b = new DroneConEstado(new Vigilancia(2, "VIG-002", "Autel", "EVO II", 1.1, false));

        a.encender();

        assertEquals("EN ESPERA", a.getEstadoActual().toString());
        assertEquals("APAGADO", b.getEstadoActual().toString());
    }

    @Test
    @DisplayName("El wrapper no modifica el dron original")
    void wrapper_noModificaElDron() {
        Drone original = drone();
        DroneConEstado ctx = new DroneConEstado(original);

        ctx.encender();

        assertSame(original, ctx.getDrone());
        assertEquals("VIG-001", original.getSerial());
        assertNull(original.getClimaActual());
    }

    @Test
    @DisplayName("No se acepta envolver un dron nulo")
    void nulo_rechazado() {
        assertThrows(NullPointerException.class, () -> new DroneConEstado(null));
    }
}
