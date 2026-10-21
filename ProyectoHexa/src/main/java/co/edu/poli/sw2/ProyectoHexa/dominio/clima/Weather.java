package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import java.io.Serializable;
import java.util.Locale;

/**
 * Entidad de dominio con las condiciones climaticas actuales de
 * una ciudad.
 *
 * <p>
 * Es el mensaje que el {@link WeatherSubject} envia a sus
 * {@link WeatherObserver} en cada notificacion, e incluye los cinco
 * aspectos meteorologicos que exige el sistema. Es inmutable: una vez
 * creada no cambia, de modo que todos los observadores reciben
 * exactamente la misma informacion.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class Weather implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Temperatura en grados Celsius. */
    private final double temperatura;

    /** Humedad relativa en porcentaje (0 a 100). */
    private final double humedad;

    /** Velocidad del viento en km/h. */
    private final double velocidadViento;

    /** Presion atmosferica en hPa. */
    private final double presion;

    /** Visibilidad en kilometros. */
    private final double visibilidad;

    /**
     * Crea un registro climatico con los cinco aspectos meteorologicos.
     *
     * @param temperatura     temperatura en grados Celsius
     * @param humedad         humedad relativa en porcentaje
     * @param velocidadViento velocidad del viento en km/h
     * @param presion         presion atmosferica en hPa
     * @param visibilidad     visibilidad en kilometros
     */
    public Weather(double temperatura, double humedad, double velocidadViento,
                   double presion, double visibilidad) {
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.velocidadViento = velocidadViento;
        this.presion = presion;
        this.visibilidad = visibilidad;
    }

    /**
     * Obtiene la temperatura.
     *
     * @return temperatura en grados Celsius
     */
    public double getTemperatura() {
        return temperatura;
    }

    /**
     * Obtiene la humedad relativa.
     *
     * @return humedad en porcentaje
     */
    public double getHumedad() {
        return humedad;
    }

    /**
     * Obtiene la velocidad del viento.
     *
     * @return velocidad del viento en km/h
     */
    public double getVelocidadViento() {
        return velocidadViento;
    }

    /**
     * Obtiene la presion atmosferica.
     *
     * @return presion en hPa
     */
    public double getPresion() {
        return presion;
    }

    /**
     * Obtiene la visibilidad.
     *
     * @return visibilidad en kilometros
     */
    public double getVisibilidad() {
        return visibilidad;
    }

    /**
     * Resume los cinco aspectos en una sola linea legible.
     *
     * @return texto con temperatura, humedad, viento, presion y visibilidad
     */
    @Override
    public String toString() {
        return String.format(Locale.US,
                "%.1f C | %.0f %% | %.1f km/h | %.1f hPa | %.1f km",
                temperatura, humedad, velocidadViento, presion, visibilidad);
    }
}
