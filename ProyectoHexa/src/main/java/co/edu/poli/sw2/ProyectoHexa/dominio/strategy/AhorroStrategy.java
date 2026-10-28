package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Estrategia concreta <b>Modo Ahorro</b>: minimiza el consumo de bateria.
 *
 * <p>Consumo = distancia en km &times; 0.5.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class AhorroStrategy implements ConsumoStrategy {

    /** Factor de consumo de este modo. */
    public static final double FACTOR = 0.5;

    /**
     * {@inheritDoc}
     */
    @Override
    public double calcularConsumo(double distanciaKm) {
        if (Double.isNaN(distanciaKm) || distanciaKm < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa.");
        }
        return distanciaKm * FACTOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNombre() {
        return "Modo Ahorro";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFactor() {
        return FACTOR;
    }

    /**
     * Devuelve el nombre del modo, usado por los selectores de JavaFX.
     *
     * @return nombre de la estrategia
     */
    @Override
    public String toString() {
        return getNombre();
    }
}
