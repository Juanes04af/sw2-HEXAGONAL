package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoConsumo;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AhorroStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AltoRendimientoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.NormalStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * Pruebas del contexto del patron Strategy ({@link CalcularConsumoServicio}).
 * Usa una ruta y una distancia falsas (20 km), sin archivos ni formulas.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class CalcularConsumoServicioTest {

    private int cargas;
    private CalcularConsumoServicio servicio;
    private Drone drone;

    @BeforeEach
    void preparar() {
        cargas = 0;
        Ruta ruta = new Ruta(new PuntoGeografico("A", 0, 0), new PuntoGeografico("B", 1, 1));
        servicio = new CalcularConsumoServicio(
                () -> {
                    cargas++;
                    return ruta;
                },
                (origen, destino) -> 20.0);
        drone = new Vigilancia(1, "VIG-001", "DJI", "Mavic 3", 0.9, true);
    }

    @Test
    @DisplayName("El contexto usa la estrategia seleccionada")
    void calcula_conEstrategiaSeleccionada() {
        servicio.setEstrategia(new AhorroStrategy());

        ResultadoConsumo r = servicio.calcular(drone);

        assertEquals(20.0, r.getDistanciaKm(), 0.0001);
        assertEquals(10.0, r.getConsumo(), 0.0001);
        assertEquals("Modo Ahorro", r.getEstrategia().getNombre());
        assertSame(drone, r.getDrone());
    }

    @Test
    @DisplayName("Cambiar la estrategia cambia el resultado sin cambiar el contexto")
    void intercambiaEstrategias() {
        servicio.setEstrategia(new AhorroStrategy());
        double ahorro = servicio.calcular(drone).getConsumo();

        servicio.setEstrategia(new NormalStrategy());
        double normal = servicio.calcular(drone).getConsumo();

        servicio.setEstrategia(new AltoRendimientoStrategy());
        double alto = servicio.calcular(drone).getConsumo();

        assertEquals(10.0, ahorro, 0.0001);
        assertEquals(20.0, normal, 0.0001);
        assertEquals(30.0, alto, 0.0001);
    }

    @Test
    @DisplayName("Acepta cualquier estrategia nueva sin modificar el servicio")
    void aceptaEstrategiaNueva() {
        ConsumoStrategy doble = new ConsumoStrategy() {
            @Override
            public double calcularConsumo(double distanciaKm) {
                return distanciaKm * 2;
            }

            @Override
            public String getNombre() {
                return "Modo Prueba";
            }

            @Override
            public double getFactor() {
                return 2;
            }
        };
        servicio.setEstrategia(doble);

        assertEquals(40.0, servicio.calcular(drone).getConsumo(), 0.0001);
    }

    @Test
    @DisplayName("Sin estrategia seleccionada, el calculo es rechazado")
    void sinEstrategia_rechazado() {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> servicio.calcular(drone));
        assertEquals("Seleccione una estrategia de consumo.", ex.getMessage());
    }

    @Test
    @DisplayName("Sin dron seleccionado, el calculo es rechazado")
    void sinDrone_rechazado() {
        servicio.setEstrategia(new NormalStrategy());

        assertThrows(DronValidacionException.class, () -> servicio.calcular(null));
    }

    @Test
    @DisplayName("La ruta se carga una sola vez aunque se calcule varias veces")
    void rutaSeCargaUnaVez() {
        servicio.setEstrategia(new NormalStrategy());
        servicio.calcular(drone);
        servicio.calcular(drone);
        servicio.obtenerRuta();

        assertEquals(1, cargas);
    }

    @Test
    @DisplayName("Ofrece las tres estrategias en orden")
    void estrategiasDisponibles() {
        assertEquals(3, servicio.estrategiasDisponibles().size());
        assertTrue(servicio.estrategiasDisponibles().get(0) instanceof AhorroStrategy);
        assertTrue(servicio.estrategiasDisponibles().get(1) instanceof NormalStrategy);
        assertTrue(servicio.estrategiasDisponibles().get(2) instanceof AltoRendimientoStrategy);
    }

    @Test
    @DisplayName("Describe el calculo como 'distancia x factor = consumo'")
    void describeCalculo() {
        servicio.setEstrategia(new AltoRendimientoStrategy());

        assertEquals("20.00 km x 1.5 = 30.00", servicio.calcular(drone).describirCalculo());
    }
}
