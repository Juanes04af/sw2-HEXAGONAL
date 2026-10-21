package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

/**
 * Latitud y longitud de una ciudad, obtenidas de la geocodificacion.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class Coordenadas {

    /** Latitud en grados. */
    private final double latitud;

    /** Longitud en grados. */
    private final double longitud;

    /**
     * Crea un par de coordenadas.
     *
     * @param latitud  latitud en grados
     * @param longitud longitud en grados
     */
    public Coordenadas(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /**
     * Obtiene la latitud.
     *
     * @return latitud en grados
     */
    public double getLatitud() {
        return latitud;
    }

    /**
     * Obtiene la longitud.
     *
     * @return longitud en grados
     */
    public double getLongitud() {
        return longitud;
    }
}
