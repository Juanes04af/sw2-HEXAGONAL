package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

/**
 * Estrategia del patron <b>Strategy</b> para estimar la bateria que
 * un dron necesita para recorrer una distancia.
 *
 * <p>
 * Cada modo de vuelo implementa esta interfaz con su propio factor de
 * consumo. Quien calcula el consumo (el contexto) solo conoce esta
 * interfaz, por lo que la estrategia puede cambiarse en tiempo de
 * ejecucion sin modificar el calculo.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ConsumoStrategy {

    /**
     * Calcula la bateria requerida para recorrer una distancia.
     *
     * @param distanciaKm distancia en kilometros; no puede ser negativa
     * @return bateria requerida, en unidades de bateria
     * @throws IllegalArgumentException si la distancia es negativa o no es un numero
     */
    double calcularConsumo(double distanciaKm);

    /**
     * Obtiene el nombre del modo, para mostrarlo al usuario.
     *
     * @return nombre de la estrategia
     */
    String getNombre();

    /**
     * Obtiene el factor por el que se multiplica la distancia.
     *
     * @return factor de consumo
     */
    double getFactor();
}
