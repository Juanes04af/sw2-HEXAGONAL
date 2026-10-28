package co.edu.poli.sw2.ProyectoHexa.infraestructura.util;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.CalculadorDistancia;
import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;

/**
 * Calcula la distancia entre dos coordenadas con la formula de
 * <b>Haversine</b>, que mide el arco mas corto sobre una esfera del
 * tamano de la Tierra.
 *
 * <pre>
 * a = sin^2(dLat / 2) + cos(lat1) * cos(lat2) * sin^2(dLon / 2)
 * d = 2 * R * asin(sqrt(a))
 * </pre>
 *
 * <p>
 * Implementa el puerto {@link CalculadorDistancia} y ofrece ademas un
 * metodo estatico para usarla directamente con latitudes y longitudes.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class HaversineUtil implements CalculadorDistancia {

    /** Radio medio de la Tierra en kilometros. */
    public static final double RADIO_TIERRA_KM = 6371.0;

    /**
     * {@inheritDoc}
     */
    @Override
    public double distanciaKm(PuntoGeografico origen, PuntoGeografico destino) {
        return calcular(origen.getLatitud(), origen.getLongitud(),
                        destino.getLatitud(), destino.getLongitud());
    }

    /**
     * Calcula la distancia entre dos coordenadas.
     *
     * @param lat1 latitud del origen en grados
     * @param lon1 longitud del origen en grados
     * @param lat2 latitud del destino en grados
     * @param lon2 longitud del destino en grados
     * @return distancia en kilometros
     */
    public static double calcular(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(deltaPhi / 2), 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2);

        // min() evita errores de redondeo que dejen 'a' apenas por encima de 1.
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(Math.min(1.0, a)));
    }
}
