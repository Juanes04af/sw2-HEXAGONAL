package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Cuarto eslabon: la presion atmosferica debe estar entre 950 y 1050 hPa.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class PressureHandler extends WeatherHandler {

    /** Presion minima permitida (hPa). */
    public static final double MINIMA = 950;

    /** Presion maxima permitida (hPa). */
    public static final double MAXIMA = 1050;

    /** {@inheritDoc} */
    @Override
    protected String validarCondicion(Weather clima) {
        double p = clima.getPresion();
        if (p < MINIMA || p > MAXIMA) {
            return String.format(Locale.US,
                    "Presión atmosférica fuera del rango permitido (%.0f a %.0f hPa): %.1f hPa.",
                    MINIMA, MAXIMA, p);
        }
        return null;
    }
}
