package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.DroneState;

/**
 * Puerto de entrada para gestionar el estado operativo de los drones.
 *
 * <p>
 * Todo dron esta APAGADO hasta que se enciende. Las operaciones que el
 * estado actual no permite se rechazan con
 * {@link co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException}. Los estados
 * viven solo en memoria mientras la aplicacion esta abierta.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ManageDroneStateUseCase {

    /**
     * Enciende un dron APAGADO (pasa a EN ESPERA).
     *
     * @param drone dron a encender
     */
    void encender(Drone drone);

    /**
     * Apaga un dron EN ESPERA (pasa a APAGADO).
     *
     * @param drone dron a apagar
     */
    void apagar(Drone drone);

    /**
     * Despega un dron EN ESPERA (pasa a VOLANDO).
     *
     * @param drone dron que despega
     */
    void despegar(Drone drone);

    /**
     * Inicia el aterrizaje de un dron VOLANDO (pasa a ATERRIZANDO).
     *
     * @param drone dron que aterriza
     */
    void iniciarAterrizaje(Drone drone);

    /**
     * Finaliza el aterrizaje de un dron ATERRIZANDO (pasa a EN ESPERA).
     *
     * @param drone dron que termina de aterrizar
     */
    void finalizarAterrizaje(Drone drone);

    /**
     * Obtiene el estado operativo actual de un dron.
     *
     * @param drone dron a consultar
     * @return su estado; APAGADO si nunca se ha operado
     */
    DroneState estadoDe(Drone drone);
}
