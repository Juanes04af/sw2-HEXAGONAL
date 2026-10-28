package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;

/**
 * Recorrido de un dron entre un punto de origen y un punto de destino.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class Ruta {

    /** Punto de partida. */
    private final PuntoGeografico origen;

    /** Punto de llegada. */
    private final PuntoGeografico destino;

    /**
     * Crea una ruta.
     *
     * @param origen  punto de partida
     * @param destino punto de llegada
     * @throws RutaException si falta alguno de los dos puntos
     */
    public Ruta(PuntoGeografico origen, PuntoGeografico destino) {
        if (origen == null || destino == null) {
            throw new RutaException("La ruta necesita un punto de origen y uno de destino.");
        }
        this.origen = origen;
        this.destino = destino;
    }

    /**
     * Obtiene el punto de origen.
     *
     * @return origen
     */
    public PuntoGeografico getOrigen() {
        return origen;
    }

    /**
     * Obtiene el punto de destino.
     *
     * @return destino
     */
    public PuntoGeografico getDestino() {
        return destino;
    }
}
