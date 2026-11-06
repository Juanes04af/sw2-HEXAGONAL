package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado del patron <b>State</b>: define los comportamientos del ciclo
 * operativo de un dron. Cada estado concreto decide cuales permite
 * (haciendo la transicion) y cuales rechaza con
 * {@link co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException}.
 *
 * <p>Ciclo: APAGADO &harr; EN ESPERA &rarr; VOLANDO &rarr; ATERRIZANDO &rarr; EN ESPERA.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface DroneState {

    /**
     * Enciende el dron.
     *
     * @param drone dron sobre el que se opera
     */
    void encender(Drone drone);

    /**
     * Apaga el dron.
     *
     * @param drone dron sobre el que se opera
     */
    void apagar(Drone drone);

    /**
     * Despega el dron.
     *
     * @param drone dron sobre el que se opera
     */
    void despegar(Drone drone);

    /**
     * Inicia el aterrizaje del dron.
     *
     * @param drone dron sobre el que se opera
     */
    void iniciarAterrizaje(Drone drone);

    /**
     * Finaliza el aterrizaje del dron.
     *
     * @param drone dron sobre el que se opera
     */
    void finalizarAterrizaje(Drone drone);
}
