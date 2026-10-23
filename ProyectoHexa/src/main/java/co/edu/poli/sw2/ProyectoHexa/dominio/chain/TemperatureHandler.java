package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Primer eslabon: la temperatura debe estar entre 0 y 40 grados Celsius.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class TemperatureHandler extends WeatherHandler {

    /** Temperatura minima permitida (C). */
    public static final double MINIMA = 0;

    /** Temperatura maxima permitida (C). */
    public static final double MAXIMA = 40;

    /** {@inheritDoc} */
    @Override
    protected String validarCondicion(Weather clima) {
        double t = clima.getTemperatura();
        if (t < MINIMA || t > MAXIMA) {
            return String.format(Locale.US,
                    "Temperatura fuera del rango permitido (%.0f a %.0f °C): %.1f °C.",
                    MINIMA, MAXIMA, t);
        }
        return null;
    }
}
