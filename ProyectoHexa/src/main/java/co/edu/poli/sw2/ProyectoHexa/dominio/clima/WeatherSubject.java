package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

/**
 * Sujeto del patron <b>Observer</b>.
 *
 * <p>
 * Mantiene una lista de {@link WeatherObserver} que pueden registrarse
 * y retirarse dinamicamente. Cada vez que hay una nueva lectura del
 * clima, el sujeto la envia a todos los observadores registrados.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 * @see WeatherObserver
 */
public interface WeatherSubject {

    /**
     * Registra un observador para que reciba las notificaciones.
     *
     * @param observer observador a registrar
     */
    void suscribir(WeatherObserver observer);

    /**
     * Retira un observador; deja de recibir notificaciones.
     *
     * @param observer observador a retirar
     */
    void desuscribir(WeatherObserver observer);

    /**
     * Envia el clima a todos los observadores registrados.
     *
     * @param clima condiciones climaticas a notificar
     */
    void notificar(Weather clima);
}
