package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sujeto concreto del patron <b>Observer</b>.
 *
 * <p>
 * Existe un notificador por cada {@link Ciudad}: los drones que se
 * suscriben a una ciudad quedan registrados en su notificador, y al
 * llegar un clima nuevo de esa ciudad todos ellos son avisados
 * automaticamente.
 * </p>
 *
 * <p>
 * La lista de observadores es una {@link CopyOnWriteArrayList}, de modo
 * que la notificacion puede ejecutarse en un hilo de fondo sin fallar
 * si la interfaz suscribe o retira un dron al mismo tiempo.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class WeatherNotifier implements WeatherSubject {

    /** Ciudad cuyo clima notifica este sujeto. */
    private final Ciudad ciudad;

    /** Observadores registrados, sin duplicados. */
    private final List<WeatherObserver> observadores = new CopyOnWriteArrayList<>();

    /**
     * Crea el notificador de una ciudad.
     *
     * @param ciudad ciudad asociada
     * @throws NullPointerException si la ciudad es nula
     */
    public WeatherNotifier(Ciudad ciudad) {
        this.ciudad = Objects.requireNonNull(ciudad, "La ciudad es obligatoria.");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si el observador ya estaba registrado, no se agrega de nuevo.</p>
     *
     * @throws NullPointerException si el observador es nulo
     */
    @Override
    public void suscribir(WeatherObserver observer) {
        Objects.requireNonNull(observer, "El observador es obligatorio.");
        if (!observadores.contains(observer)) {
            observadores.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si el observador no estaba registrado, no ocurre nada.</p>
     */
    @Override
    public void desuscribir(WeatherObserver observer) {
        observadores.remove(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException si el clima es nulo
     */
    @Override
    public void notificar(Weather clima) {
        Objects.requireNonNull(clima, "El clima es obligatorio.");
        for (WeatherObserver observer : observadores) {
            observer.actualizarClima(clima);
        }
    }

    /**
     * Indica si un observador esta registrado.
     *
     * @param observer observador a buscar
     * @return {@code true} si esta suscrito
     */
    public boolean estaSuscrito(WeatherObserver observer) {
        return observadores.contains(observer);
    }

    /**
     * Indica si hay al menos un observador registrado.
     *
     * @return {@code true} si tiene suscriptores
     */
    public boolean tieneObservadores() {
        return !observadores.isEmpty();
    }

    /**
     * Devuelve los observadores registrados en orden de suscripcion.
     *
     * @return vista de solo lectura de los observadores
     */
    public List<WeatherObserver> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }

    /**
     * Obtiene la ciudad asociada.
     *
     * @return ciudad de este notificador
     */
    public Ciudad getCiudad() {
        return ciudad;
    }
}
