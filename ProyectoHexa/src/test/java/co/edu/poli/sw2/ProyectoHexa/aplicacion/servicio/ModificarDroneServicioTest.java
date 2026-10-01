package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del caso de uso de modificacion ({@link ModificarDroneServicio}).
 * Usa {@link RepositorioEnMemoria}, por lo que no requiere base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ModificarDroneServicioTest {

    private RepositorioEnMemoria repositorio;
    private ModificarDroneServicio servicio;
    private Drone existente;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        servicio = new ModificarDroneServicio(repositorio);
        existente = repositorio.crear(
                new Vigilancia(0, "VIG-001", "DJI", "Mavic 3", 0.9, false));
    }

    @Test
    @DisplayName("Modificar actualiza los datos comunes y la deteccion termica")
    void modificar_actualizaDatos() {
        Drone modificado = servicio.modificar(
                existente, "VIG-001B", "Autel", "EVO II", "1.2", true);

        assertEquals("VIG-001B", modificado.getSerial());
        assertEquals("Autel", modificado.getFabricante());
        assertEquals("EVO II", modificado.getModelo());
        assertEquals(1.2, modificado.getPeso(), 0.0001);
        assertTrue(((Vigilancia) modificado).isDeteccionTermica());
        assertEquals(existente.getId(), modificado.getId());
        assertEquals(1, repositorio.llamadasModificar);
    }

    @Test
    @DisplayName("Modificar sin seleccion lanza excepcion y no llama al repositorio")
    void modificar_sinSeleccion_lanzaExcepcion() {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> servicio.modificar(null, "VIG-X", "DJI", "M", "1", false));

        assertEquals("Debe seleccionar un drone.", ex.getMessage());
        assertEquals(0, repositorio.llamadasModificar);
    }

    @Test
    @DisplayName("Modificar un dron que no es de vigilancia es rechazado")
    void modificar_tipoNoVigilancia_lanzaExcepcion() {
        Drone otroTipo = new Drone(5, "AGR-001", "DJI", "Agras", 30.0) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getTipo() {
                return "AGRICULTURA";
            }
        };

        assertThrows(DronValidacionException.class,
                () -> servicio.modificar(otroTipo, "AGR-001", "DJI", "Agras", "30", false));
        assertEquals(0, repositorio.llamadasModificar);
    }

    @Test
    @DisplayName("Si un dato es invalido, el dron original no se altera")
    void modificar_datoInvalido_noAlteraOriginal() {
        assertThrows(DronValidacionException.class,
                () -> servicio.modificar(existente, "VIG-NUEVO", "DJI", "M", "abc", true));

        assertEquals("VIG-001", existente.getSerial());
        assertEquals(0.9, existente.getPeso(), 0.0001);
        assertFalse(((Vigilancia) existente).isDeteccionTermica());
        assertEquals(0, repositorio.llamadasModificar);
    }

    @Test
    @DisplayName("Modificar un dron inexistente lanza DronNoEncontradoException")
    void modificar_inexistente_lanzaExcepcion() {
        Vigilancia fantasma = new Vigilancia(999, "VIG-999", "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class,
                () -> servicio.modificar(fantasma, "VIG-999", "DJI", "M", "1.0", false));
    }

    @Test
    @DisplayName("Modificar usando el serial de otro dron lanza DronDuplicadoException")
    void modificar_serialDeOtro_lanzaExcepcion() {
        repositorio.crear(new Vigilancia(0, "VIG-002", "DJI", "Mini", 0.25, false));

        assertThrows(DronDuplicadoException.class,
                () -> servicio.modificar(existente, "VIG-002", "DJI", "Mavic 3", "0.9", false));
    }
}
