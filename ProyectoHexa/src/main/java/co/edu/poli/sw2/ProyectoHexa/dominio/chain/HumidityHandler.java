package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import java.util.Locale;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Segundo eslabon: la humedad relativa debe ser menor o igual a 85 %.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class HumidityHandler extends WeatherHandler {

    /** Humedad maxima permitida (%). */
    public static final double MAXIMA = 85;

    /** {@inheritDoc} */
    @Override
    protected String validarCondicion(Weather clima) {
        double h = clima.getHumedad();
        if (h > MAXIMA) {
            return String.format(Locale.US,
                    "Humedad superior al máximo permitido (%.0f %%): %.1f %%.", MAXIMA, h);
        }
        return null;
    }
}
