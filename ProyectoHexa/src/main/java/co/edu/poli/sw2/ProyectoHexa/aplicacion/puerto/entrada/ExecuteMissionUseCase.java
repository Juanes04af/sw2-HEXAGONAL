package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

/**
 * Puerto de entrada para iniciar una operacion de vigilancia
 * coordinada por el patron Mediator.
 *
 * <p>
 * No usa las operaciones CRUD de drones: el dron de la operacion se
 * crea solo en memoria con los datos que ingresa el usuario.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ExecuteMissionUseCase {

    /**
     * Inicia una operacion de vigilancia.
     *
     * @param serial           serial del dron
     * @param modelo           modelo del dron; opcional
     * @param deteccionTermica {@code true} si el dron tiene camara termica
     * @param nivelBateria     bateria actual, de 0 a 100 %
     * @return resultado de la coordinacion
     */
    ResultadoMision ejecutarMision(String serial, String modelo, boolean deteccionTermica,
                                   int nivelBateria);
}
