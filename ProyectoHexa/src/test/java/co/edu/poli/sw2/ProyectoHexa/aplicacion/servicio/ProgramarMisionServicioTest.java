package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.TipoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Prueba de punta a punta del patron Command: cliente, invocador y
 * receptor reales.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ProgramarMisionServicioTest {

    private ProgramarMisionServicio mision;
    private Drone vig1;
    private Drone vig2;

    @BeforeEach
    void preparar() {
        WeatherNotificationService clima = new WeatherNotificationService(
                new GetWeatherByCityServicio(c -> new Weather(20, 50, 10, 1010, 20)));
        mision = new ProgramarMisionServicio(
                new DroneOperacionService(clima, new Random(7)), new ProgramadorTareas());
        vig1 = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
        vig2 = new Vigilancia(2, "VIG-002", "Autel", "EVO II", 1.1, false);
    }

    @Test
    @DisplayName("Programa y ejecuta una mision completa en el orden registrado")
    void misionCompleta() {
        mision.programar(TipoTarea.TOMAR_FOTOGRAFIA, vig1);
        mision.programar(TipoTarea.ESCANEAR_ZONA, vig2);
        mision.programar(TipoTarea.MEDIR_TEMPERATURA, vig1);

        List<ResultadoTarea> r = mision.ejecutarMision();

        assertEquals(3, r.size());
        assertEquals("Tomar fotografia", r.get(0).getTarea());
        assertEquals("Escanear zona", r.get(1).getTarea());
        assertEquals("Medir temperatura", r.get(2).getTarea());
        assertSame(vig2, r.get(1).getDrone());
        assertTrue(r.get(0).getMensaje().startsWith("Fotografia #1 tomada por VIG-001"));
        assertTrue(r.stream().allMatch(ResultadoTarea::isExitosa));
        assertTrue(mision.tareasProgramadas().isEmpty());
    }

    @Test
    @DisplayName("Programar sin dron o sin tarea es rechazado")
    void programar_faltanDatos() {
        assertThrows(DronValidacionException.class,
                () -> mision.programar(TipoTarea.ESCANEAR_ZONA, null));
        assertThrows(DronValidacionException.class, () -> mision.programar(null, vig1));
        assertTrue(mision.tareasProgramadas().isEmpty());
    }

    @Test
    @DisplayName("Las tareas programadas se listan en orden antes de ejecutar")
    void listaTareasEnOrden() {
        mision.programar(TipoTarea.MEDIR_TEMPERATURA, vig1);
        mision.programar(TipoTarea.TOMAR_FOTOGRAFIA, vig2);

        assertEquals("Medir temperatura", mision.tareasProgramadas().get(0).getNombre());
        assertEquals("Tomar fotografia", mision.tareasProgramadas().get(1).getNombre());
    }

    @Test
    @DisplayName("Quitar y limpiar modifican la mision")
    void quitarYLimpiar() {
        mision.programar(TipoTarea.MEDIR_TEMPERATURA, vig1);
        mision.programar(TipoTarea.TOMAR_FOTOGRAFIA, vig1);

        mision.quitar(0);
        assertEquals(1, mision.tareasProgramadas().size());

        mision.limpiar();
        assertTrue(mision.tareasProgramadas().isEmpty());
    }
}
