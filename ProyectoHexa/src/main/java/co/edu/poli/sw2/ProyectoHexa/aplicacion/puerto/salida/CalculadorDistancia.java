package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida;

import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;

/**
 * Puerto de salida para calcular la distancia entre dos puntos
 * geograficos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface CalculadorDistancia {

    /**
     * Calcula la distancia sobre la superficie terrestre.
     *
     * @param origen  punto de partida
     * @param destino punto de llegada
     * @return distancia en kilometros
     */
    double distanciaKm(PuntoGeografico origen, PuntoGeografico destino);
}
