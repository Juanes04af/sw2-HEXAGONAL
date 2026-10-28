package co.edu.poli.sw2.ProyectoHexa.dominio.strategy;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.RutaException;

/**
 * Punto de la superficie terrestre identificado por su nombre y sus
 * coordenadas geograficas.
 *
 * <p>
 * Es inmutable y valida sus datos al construirse: el nombre es
 * obligatorio, la latitud debe estar entre -90 y 90 grados y la
 * longitud entre -180 y 180 grados.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class PuntoGeografico {

    /** Nombre descriptivo del punto. */
    private final String nombre;

    /** Latitud en grados decimales. */
    private final double latitud;

    /** Longitud en grados decimales. */
    private final double longitud;

    /**
     * Crea un punto geografico.
     *
     * @param nombre   nombre del punto
     * @param latitud  latitud en grados, entre -90 y 90
     * @param longitud longitud en grados, entre -180 y 180
     * @throws RutaException si el nombre esta vacio o alguna coordenada
     *         esta fuera de rango
     */
    public PuntoGeografico(String nombre, double latitud, double longitud) {
        if (nombre == null || nombre.isBlank()) {
            throw new RutaException("El nombre del punto geografico es obligatorio.");
        }
        if (Double.isNaN(latitud) || latitud < -90 || latitud > 90) {
            throw new RutaException("La latitud de '" + nombre
                    + "' debe estar entre -90 y 90 grados.");
        }
        if (Double.isNaN(longitud) || longitud < -180 || longitud > 180) {
            throw new RutaException("La longitud de '" + nombre
                    + "' debe estar entre -180 y 180 grados.");
        }
        this.nombre = nombre.trim();
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /**
     * Obtiene el nombre del punto.
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
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

    /**
     * Devuelve el nombre y las coordenadas del punto.
     *
     * @return por ejemplo "Centro (4.60971, -74.08175)"
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%.5f, %.5f)", nombre, latitud, longitud);
    }
}
