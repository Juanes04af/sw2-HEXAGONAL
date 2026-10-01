package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del caso de uso de creacion ({@link CrearDroneServicio}).
 * Usa {@link RepositorioEnMemoria}, por lo que no requiere base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class CrearDroneServicioTest {

    private RepositorioEnMemoria repositorio;
    private CrearDroneServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        servicio = new CrearDroneServicio(repositorio);
    }

    @Test
    @DisplayName("Crear devuelve un dron de vigilancia con id asignado")
    void crear_devuelveVigilanciaConId() {
        Drone creado = servicio.crear("VIG-001", "DJI", "Mavic 3", "0.9", true);

        assertTrue(creado instanceof Vigilancia);
        assertTrue(creado.getId() > 0);
        assertEquals("VIGILANCIA", creado.getTipo());
        assertTrue(((Vigilancia) creado).isDeteccionTermica());
        assertEquals(1, repositorio.llamadasCrear);
    }

    @Test
    @DisplayName("Crear elimina los espacios sobrantes de los textos")
    void crear_recortaEspacios() {
        Drone creado = servicio.crear("  VIG-002  ", "  DJI ", " Mini 4 ", "0.25", false);

        assertEquals("VIG-002", creado.getSerial());
        assertEquals("DJI", creado.getFabricante());
        assertEquals("Mini 4", creado.getModelo());
    }

    @Test
    @DisplayName("Crear acepta el peso con coma decimal")
    void crear_aceptaPesoConComa() {
        Drone creado = servicio.crear("VIG-003", "DJI", "Mavic", "1,5", false);

        assertEquals(1.5, creado.getPeso(), 0.0001);
    }

    @Test
    @DisplayName("Crear admite modelo nulo")
    void crear_admiteModeloNulo() {
        Drone creado = servicio.crear("VIG-004", "DJI", null, "1.0", false);

        assertNull(creado.getModelo());
    }

    @Test
    @DisplayName("Un serial invalido no llega al repositorio")
    void crear_serialInvalido_noLlamaRepositorio() {
        assertThrows(DronValidacionException.class,
                () -> servicio.crear("", "DJI", "Mavic", "0.9", false));

        assertEquals(0, repositorio.llamadasCrear);
    }

    @Test
    @DisplayName("Un fabricante invalido no llega al repositorio")
    void crear_fabricanteInvalido_noLlamaRepositorio() {
        assertThrows(DronValidacionException.class,
                () -> servicio.crear("VIG-005", "12345", "Mavic", "0.9", false));

        assertEquals(0, repositorio.llamadasCrear);
    }

    @Test
    @DisplayName("Un peso invalido no llega al repositorio")
    void crear_pesoInvalido_noLlamaRepositorio() {
        assertThrows(DronValidacionException.class,
                () -> servicio.crear("VIG-006", "DJI", "Mavic", "-3", false));

        assertEquals(0, repositorio.llamadasCrear);
    }

    @Test
    @DisplayName("Crear con un serial repetido lanza DronDuplicadoException")
    void crear_serialDuplicado_lanzaExcepcion() {
        servicio.crear("VIG-007", "DJI", "Mavic", "0.9", false);

        assertThrows(DronDuplicadoException.class,
                () -> servicio.crear("VIG-007", "Autel", "EVO", "1.1", true));
    }
}
