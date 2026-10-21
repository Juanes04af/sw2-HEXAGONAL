package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

/**
 * Observador del patron <b>Observer</b>.
 *
 * <p>
 * Cualquier objeto que quiera recibir automaticamente las
 * actualizaciones del clima implementa esta interfaz y se registra en
 * un {@link WeatherSubject}. El sujeto no conoce la clase concreta del
 * observador: solo invoca {@link #actualizarClima(Weather)}.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 * @see WeatherSubject
 */
public interface WeatherObserver {

    /**
     * Recibe una nueva lectura del clima.
     *
     * @param clima condiciones climaticas con los cinco aspectos
     */
    void actualizarClima(Weather clima);
}
