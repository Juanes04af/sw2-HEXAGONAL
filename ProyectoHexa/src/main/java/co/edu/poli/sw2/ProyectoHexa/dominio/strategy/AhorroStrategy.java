package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Estrategia concreta <b>Modo Ahorro</b>: minimiza el consumo de bateria.
 *
 * <p>Consumo = distancia en km &times; 0.5.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class AhorroStrategy extends EstrategiaConsumoBase {

    /** Factor de consumo de este modo. */
    public static final double FACTOR = 0.5;

    /** Crea la estrategia con su nombre y su factor. */
    public AhorroStrategy() {
        super("Modo Ahorro", FACTOR);
    }
}
