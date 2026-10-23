package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Tercer eslabon: la velocidad del viento debe ser menor o igual a 40 km/h.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class WindHandler extends WeatherHandler {

    /** Velocidad maxima del viento permitida (km/h). */
    public static final double MAXIMA = 40;

    /** {@inheritDoc} */
    @Override
    protected String validarCondicion(Weather clima) {
        double v = clima.getVelocidadViento();
        if (v > MAXIMA) {
            return String.format(Locale.US,
                    "Velocidad del viento superior al máximo permitido (%.0f km/h): %.1f km/h.",
                    MAXIMA, v);
        }
        return null;
    }
}
