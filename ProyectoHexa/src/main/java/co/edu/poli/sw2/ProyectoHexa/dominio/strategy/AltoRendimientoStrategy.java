package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Estrategia concreta <b>Modo Alto Rendimiento</b>: mas velocidad, mas bateria.
 *
 * <p>Consumo = distancia en km &times; 1.5.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class AltoRendimientoStrategy extends EstrategiaConsumoBase {

    /** Factor de consumo de este modo. */
    public static final double FACTOR = 1.5;

    /** Crea la estrategia con su nombre y su factor. */
    public AltoRendimientoStrategy() {
        super("Modo Alto Rendimiento", FACTOR);
    }
}
