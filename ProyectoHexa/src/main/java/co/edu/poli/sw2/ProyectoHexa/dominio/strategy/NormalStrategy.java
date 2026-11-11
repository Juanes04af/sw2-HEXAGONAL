package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Estrategia concreta <b>Modo Normal</b>: consumo estandar.
 *
 * <p>Consumo = distancia en km &times; 1.0.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class NormalStrategy extends EstrategiaConsumoBase {

    /** Factor de consumo de este modo. */
    public static final double FACTOR = 1.0;

    /** Crea la estrategia con su nombre y su factor. */
    public NormalStrategy() {
        super("Modo Normal", FACTOR);
    }
}
