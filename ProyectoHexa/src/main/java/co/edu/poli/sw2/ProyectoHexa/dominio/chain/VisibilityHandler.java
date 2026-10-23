package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Quinto y ultimo eslabon: la visibilidad debe ser mayor o igual a 5 km.
 * El {@code Weather} del dominio ya entrega la visibilidad en kilometros.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class VisibilityHandler extends WeatherHandler {

    /** Visibilidad minima permitida (km). */
    public static final double MINIMA = 5;

    /** {@inheritDoc} */
    @Override
    protected String validarCondicion(Weather clima) {
        double v = clima.getVisibilidad();
        if (v < MINIMA) {
            return String.format(Locale.US,
                    "Visibilidad inferior al mínimo permitido (%.0f km): %.1f km.", MINIMA, v);
        }
        return null;
    }
}
