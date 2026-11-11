package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Base comun de las estrategias de consumo.
 *
 * <p>Las tres estrategias calculan igual (distancia &times; factor) y solo
 * cambian el nombre y el factor. Esta clase guarda ese codigo una vez;
 * cada estrategia concreta solo indica su nombre y su factor.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public abstract class EstrategiaConsumoBase implements ConsumoStrategy {

    private final String nombre;
    private final double factor;

    /**
     * @param nombre nombre visible del modo de vuelo
     * @param factor multiplicador de consumo por kilometro
     */
    protected EstrategiaConsumoBase(String nombre, double factor) {
        this.nombre = nombre;
        this.factor = factor;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException si la distancia es negativa o NaN
     */
    @Override
    public final double calcularConsumo(double distanciaKm) {
        if (Double.isNaN(distanciaKm) || distanciaKm < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa.");
        }
        return distanciaKm * factor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getNombre() {
        return nombre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double getFactor() {
        return factor;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
