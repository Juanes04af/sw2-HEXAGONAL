package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.DroneState;

/**
 * Excepcion lanzada cuando se pide una operacion que el estado actual
 * del dron no permite (transicion fuera del ciclo operativo).
 *
 * <p>
 * Hereda de {@link DronException} para que la interfaz la maneje con
 * el mismo bloque {@code catch} que el resto de errores del dominio.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class TransicionInvalidaException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion.
     *
     * @param drone      dron sobre el que se pidio la operacion
     * @param estado     estado en el que se encuentra
     * @param operacion  operacion rechazada, por ejemplo "despegar"
     * @param permitidas texto con lo que el estado si permite
     */
    public TransicionInvalidaException(Drone drone, DroneState estado,
                                       String operacion, String permitidas) {
        super("Operacion no permitida (" + operacion + "): el drone "
                + drone.getSerial() + " esta " + estado + ". " + permitidas);
    }
}
