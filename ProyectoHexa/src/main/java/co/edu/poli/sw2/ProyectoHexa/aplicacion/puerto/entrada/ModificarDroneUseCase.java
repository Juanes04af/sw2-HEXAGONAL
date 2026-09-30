package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada encargado de modificar un dron.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ModificarDroneUseCase {

    /**
     * Modifica un dron de vigilancia existente.
     *
     * @param existente dron existente
     * @param serial nuevo serial
     * @param fabricante nuevo fabricante
     * @param modelo nuevo modelo
     * @param pesoTexto nuevo peso
     * @param deteccionTermica nuevo estado termico
     * @return dron modificado
     */
    Drone modificar(Drone existente,
                    String serial,
                    String fabricante,
                    String modelo,
                    String pesoTexto,
                    boolean deteccionTermica);
}