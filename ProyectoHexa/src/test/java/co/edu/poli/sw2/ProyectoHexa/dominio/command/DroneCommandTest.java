package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de los comandos concretos del patron Command.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class DroneCommandTest {

    /** Receptor de prueba que registra que operaciones se invocaron. */
    private static class ReceptorEspia implements DroneOperacion {
        final List<String> llamadas = new ArrayList<>();

        @Override
        public String tomarFotografia(Drone drone) {
            llamadas.add("foto:" + drone.getSerial());
            return "foto";
        }

        @Override
        public String escanearZona(Drone drone) {
            llamadas.add("escaneo:" + drone.getSerial());
            return "escaneo";
        }

        @Override
        public String medirTemperatura(Drone drone) {
            llamadas.add("temperatura:" + drone.getSerial());
            return "temperatura";
        }
    }

    private ReceptorEspia receptor;
    private Drone drone;

    @BeforeEach
    void preparar() {
        receptor = new ReceptorEspia();
        drone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
    }

    @Test
    @DisplayName("TomarFotografiaCommand delega en tomarFotografia del receptor")
    void tomarFotografia_delega() {
        DroneCommand c = new TomarFotografiaCommand(receptor, drone);

        assertEquals("foto", c.ejecutar());
        assertEquals(List.of("foto:VIG-001"), receptor.llamadas);
    }

    @Test
    @DisplayName("EscanearZonaCommand delega en escanearZona del receptor")
    void escanearZona_delega() {
        DroneCommand c = new EscanearZonaCommand(receptor, drone);

        assertEquals("escaneo", c.ejecutar());
        assertEquals(List.of("escaneo:VIG-001"), receptor.llamadas);
    }

    @Test
    @DisplayName("MedirTemperaturaCommand delega en medirTemperatura del receptor")
    void medirTemperatura_delega() {
        DroneCommand c = new MedirTemperaturaCommand(receptor, drone);

        assertEquals("temperatura", c.ejecutar());
        assertEquals(List.of("temperatura:VIG-001"), receptor.llamadas);
    }

    @Test
    @DisplayName("Crear un comando no ejecuta la operacion: se ejecuta despues")
    void crear_noEjecuta() {
        DroneCommand c = new TomarFotografiaCommand(receptor, drone);

        assertTrue(receptor.llamadas.isEmpty());
        c.ejecutar();
        assertEquals(1, receptor.llamadas.size());
    }

    @Test
    @DisplayName("Un comando puede ejecutarse varias veces")
    void ejecutar_variasVeces() {
        DroneCommand c = new EscanearZonaCommand(receptor, drone);

        c.ejecutar();
        c.ejecutar();

        assertEquals(2, receptor.llamadas.size());
    }

    @Test
    @DisplayName("Cada comando conoce su nombre y su dron")
    void nombreYDrone() {
        assertEquals("Tomar fotografia", new TomarFotografiaCommand(receptor, drone).getNombre());
        assertEquals("Escanear zona", new EscanearZonaCommand(receptor, drone).getNombre());
        assertEquals("Medir temperatura", new MedirTemperaturaCommand(receptor, drone).getNombre());
        assertSame(drone, new MedirTemperaturaCommand(receptor, drone).getDrone());
    }

    @Test
    @DisplayName("TipoTarea crea el comando concreto correcto")
    void tipoTarea_creaComandoCorrecto() {
        assertTrue(TipoTarea.TOMAR_FOTOGRAFIA.crear(receptor, drone) instanceof TomarFotografiaCommand);
        assertTrue(TipoTarea.ESCANEAR_ZONA.crear(receptor, drone) instanceof EscanearZonaCommand);
        assertTrue(TipoTarea.MEDIR_TEMPERATURA.crear(receptor, drone) instanceof MedirTemperaturaCommand);
    }

    @Test
    @DisplayName("Un comando sin receptor o sin dron es rechazado")
    void nulos_rechazados() {
        assertThrows(NullPointerException.class, () -> new TomarFotografiaCommand(null, drone));
        assertThrows(NullPointerException.class, () -> new TomarFotografiaCommand(receptor, null));
    }
}
