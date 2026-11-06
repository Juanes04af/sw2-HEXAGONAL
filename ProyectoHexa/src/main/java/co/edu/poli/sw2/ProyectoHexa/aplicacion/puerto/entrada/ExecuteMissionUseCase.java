package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada para iniciar una operacion de vigilancia
 * coordinada por el patron Mediator.
 *
 * <p>
 * Recibe un dron ya existente (leido de la base de datos y convertido
 * al modelo {@link Drone}) y lo entrega al mediador. Este caso de uso
 * no crea, modifica ni elimina drones.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ExecuteMissionUseCase {

    /**
     * Inicia una operacion de vigilancia.
     *
     * @param drone        dron registrado que hara la operacion
     * @param nivelBateria bateria actual del dron, de 0 a 100 %
     * @return resultado de la coordinacion
     */
    ResultadoMision ejecutarMision(Drone drone, int nivelBateria);
}
