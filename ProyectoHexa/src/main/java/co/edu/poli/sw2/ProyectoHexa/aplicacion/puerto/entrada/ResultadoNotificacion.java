package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Resultado de una ronda de notificaciones.
 *
 * <p>
 * Registra que ciudades se consultaron con exito (y su clima) y
 * cuales fallaron (y el motivo). Un fallo en una ciudad no impide
 * notificar a las demas.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ResultadoNotificacion {

    /** Clima notificado por ciudad. */
    private final Map<Ciudad, Weather> notificadas = new EnumMap<>(Ciudad.class);

    /** Mensaje de error por ciudad. */
    private final Map<Ciudad, String> errores = new EnumMap<>(Ciudad.class);

    /**
     * Registra una ciudad notificada con exito.
     *
     * @param ciudad ciudad consultada
     * @param clima  clima notificado
     */
    public void registrarExito(Ciudad ciudad, Weather clima) {
        notificadas.put(ciudad, clima);
    }

    /**
     * Registra una ciudad cuya consulta fallo.
     *
     * @param ciudad  ciudad consultada
     * @param mensaje motivo del error
     */
    public void registrarError(Ciudad ciudad, String mensaje) {
        errores.put(ciudad, mensaje);
    }

    /**
     * Obtiene las ciudades notificadas con su clima.
     *
     * @return mapa de solo lectura
     */
    public Map<Ciudad, Weather> getNotificadas() {
        return Collections.unmodifiableMap(notificadas);
    }

    /**
     * Obtiene las ciudades que fallaron con su motivo.
     *
     * @return mapa de solo lectura
     */
    public Map<Ciudad, String> getErrores() {
        return Collections.unmodifiableMap(errores);
    }

    /**
     * Indica si no se consulto ninguna ciudad (no habia suscriptores).
     *
     * @return {@code true} si no hubo exitos ni errores
     */
    public boolean estaVacio() {
        return notificadas.isEmpty() && errores.isEmpty();
    }
}
