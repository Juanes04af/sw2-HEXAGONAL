package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.List;
import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CalcularConsumoUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoConsumo;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.CalculadorDistancia;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.RutaLoader;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AhorroStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.AltoRendimientoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.ConsumoStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.NormalStrategy;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.Ruta;

/**
 * <b>Contexto</b> del patron Strategy.
 *
 * <p>
 * Guarda una referencia a la {@link ConsumoStrategy} elegida y delega
 * en ella el calculo de la bateria. El servicio no sabe que modo de
 * vuelo esta usando: solo invoca {@code calcularConsumo(distancia)},
 * de modo que cambiar de estrategia no exige modificar esta clase.
 * </p>
 *
 * <p>
 * La ruta se obtiene del puerto {@link RutaLoader} y la distancia del
 * puerto {@link CalculadorDistancia}; ambas se calculan una sola vez,
 * la primera vez que se necesitan.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class CalcularConsumoServicio implements CalcularConsumoUseCase {

    /** Estrategias que ofrece el sistema. */
    private static final List<ConsumoStrategy> ESTRATEGIAS = List.of(
            new AhorroStrategy(),
            new NormalStrategy(),
            new AltoRendimientoStrategy());

    /** Fuente de la ruta. */
    private final RutaLoader rutaLoader;

    /** Calculo de la distancia entre los puntos. */
    private final CalculadorDistancia calculadorDistancia;

    /** Estrategia seleccionada; nula hasta que se elija una. */
    private ConsumoStrategy estrategia;

    /** Ruta ya cargada. */
    private Ruta ruta;

    /** Distancia de la ruta ya calculada. */
    private double distanciaKm;

    /**
     * Construye el servicio.
     *
     * @param rutaLoader          puerto para cargar la ruta
     * @param calculadorDistancia puerto para calcular la distancia
     */
    public CalcularConsumoServicio(RutaLoader rutaLoader,
                                   CalculadorDistancia calculadorDistancia) {
        this.rutaLoader = Objects.requireNonNull(rutaLoader);
        this.calculadorDistancia = Objects.requireNonNull(calculadorDistancia);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Ruta obtenerRuta() {
        if (ruta == null) {
            Ruta cargada = rutaLoader.cargarRuta();
            distanciaKm = calculadorDistancia.distanciaKm(
                    cargada.getOrigen(), cargada.getDestino());
            ruta = cargada;
        }
        return ruta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double obtenerDistanciaKm() {
        obtenerRuta();
        return distanciaKm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConsumoStrategy> estrategiasDisponibles() {
        return ESTRATEGIAS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setEstrategia(ConsumoStrategy estrategia) {
        this.estrategia = estrategia;
    }

    /**
     * Obtiene la estrategia seleccionada.
     *
     * @return estrategia actual, o {@code null} si no se ha elegido
     */
    public synchronized ConsumoStrategy getEstrategia() {
        return estrategia;
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si falta el dron o la estrategia
     */
    @Override
    public synchronized ResultadoConsumo calcular(Drone drone) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone.");
        }
        if (estrategia == null) {
            throw new DronValidacionException("Seleccione una estrategia de consumo.");
        }

        Ruta actual = obtenerRuta();

        // Delegacion del patron Strategy: el contexto no sabe que modo usa.
        double consumo = estrategia.calcularConsumo(distanciaKm);

        return new ResultadoConsumo(drone, actual, distanciaKm, estrategia, consumo);
    }
}
