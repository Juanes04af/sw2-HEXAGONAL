package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.MissionCoordinator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.BatterySystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.CommunicationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.NavigationSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.components.SensorSystem;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AhorroStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AltoRendimientoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.NormalStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.loader.JsonRutaLoader;
import co.edu.poli.sw2.ProyectoHexa.infraestructura.util.HaversineUtil;

/**
 * Integracion de Mediator y Strategy con la ruta (ruta.json) y la
 * formula de Haversine reales. No necesita base de datos ni internet.
 *
 * @author Alejandra Cano y Juan Rosero
 */
@Tag("integration")
class RutaRealIT {

    private final Drone drone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);

    private static MissionCoordinator coordinadorReal() {
        return new MissionCoordinator(
                new NavigationSystem(new JsonRutaLoader(), new HaversineUtil()),
                new SensorSystem(),
                new BatterySystem(),
                new CommunicationSystem());
    }

    @Test
    @DisplayName("Carga el ruta.json incluido en el programa")
    void cargaLaRutaDelPrograma() {
        Ruta ruta = new JsonRutaLoader().cargarRuta();

        assertEquals("Centro de Bogota", ruta.getOrigen().getNombre());
        assertEquals("Aeropuerto El Dorado", ruta.getDestino().getNombre());
    }

    @Test
    @DisplayName("Strategy con la ruta real: 12.51 km y los tres consumos")
    void strategyConLaRutaReal() {
        CalcularConsumoServicio servicio =
                new CalcularConsumoServicio(new JsonRutaLoader(), new HaversineUtil());

        assertEquals(12.51, servicio.obtenerDistanciaKm(), 0.02);

        servicio.setEstrategia(new AhorroStrategy());
        assertEquals(6.26, servicio.calcular(drone).getConsumo(), 0.02);

        servicio.setEstrategia(new NormalStrategy());
        assertEquals(12.51, servicio.calcular(drone).getConsumo(), 0.02);

        servicio.setEstrategia(new AltoRendimientoStrategy());
        assertEquals(18.77, servicio.calcular(drone).getConsumo(), 0.02);
    }

    @Test
    @DisplayName("Mediator con la ruta real: la mision inicia con los 5 pasos")
    void misionIniciadaConRutaReal() {
        ResultadoMision r = coordinadorReal().iniciarMision(drone, 80);

        assertTrue(r.isIniciada());
        assertEquals(5, r.getPasos().size());
        assertTrue(r.getPasos().get(0).getDetalle()
                .contains("Centro de Bogota -> Aeropuerto El Dorado"));
    }

    @Test
    @DisplayName("Limite de bateria con la distancia real: 33 % alcanza y 32 % no")
    void limiteDeBateria() {
        assertTrue(coordinadorReal().iniciarMision(drone, 33).isIniciada());

        ResultadoMision cancelada = coordinadorReal().iniciarMision(drone, 32);
        assertFalse(cancelada.isIniciada());
        assertTrue(cancelada.getMensaje().startsWith(ResultadoMision.MENSAJE_CANCELADA));
    }
}
