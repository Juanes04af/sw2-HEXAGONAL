package co.edu.poli.sw2.ProyectoHexa.dominio.chain;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Weather;

/**
 * Eslabon abstracto de la cadena de responsabilidad que valida si el
 * clima permite operar drones.
 *
 * <p>
 * Cada handler valida <b>una sola condicion</b> mediante
 * {@link #validarCondicion(Weather)}. Si se cumple, pasa la solicitud al
 * siguiente eslabon; si falla, la cadena se detiene y se devuelve el
 * motivo del rechazo.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public abstract class WeatherHandler {

    /** Siguiente eslabon de la cadena; {@code null} si es el ultimo. */
    private WeatherHandler siguiente;

    /**
     * Define el siguiente eslabon.
     *
     * @param siguiente handler que seguira en la cadena
     * @return el handler recibido, para poder encadenar de forma fluida
     */
    public WeatherHandler setSiguiente(WeatherHandler siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Recorre la cadena desde este eslabon. Se detiene en el primer rechazo.
     *
     * @param clima condiciones climaticas a validar
     * @return motivo del primer rechazo, o {@code null} si todos aprueban
     */
    public String validar(Weather clima) {
        String motivo = validarCondicion(clima);
        if (motivo != null) {
            return motivo;                 // falla: la cadena se detiene aqui
        }
        return siguiente == null ? null : siguiente.validar(clima);
    }

    /**
     * Valida unicamente la condicion propia de este eslabon.
     *
     * @param clima condiciones climaticas
     * @return motivo del rechazo, o {@code null} si la condicion se cumple
     */
    protected abstract String validarCondicion(Weather clima);
}
