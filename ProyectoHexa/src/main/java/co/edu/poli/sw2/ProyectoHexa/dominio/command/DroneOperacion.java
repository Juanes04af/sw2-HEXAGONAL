package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Contrato del <b>receptor</b> del patron Command: las operaciones
 * reales que sabe hacer un dron.
 *
 * <p>
 * Los comandos dependen de esta interfaz del dominio y no de la clase
 * concreta de la capa de aplicacion ({@code DroneOperacionService}).
 * Asi el dominio no conoce capas externas, como exige la arquitectura
 * hexagonal.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface DroneOperacion {

    /**
     * Toma una fotografia con el dron.
     *
     * @param drone dron que toma la fotografia
     * @return resultado de la operacion
     */
    String tomarFotografia(Drone drone);

    /**
     * Escanea la zona en la que se encuentra el dron.
     *
     * @param drone dron que escanea
     * @return resultado del escaneo
     */
    String escanearZona(Drone drone);

    /**
     * Mide la temperatura del ambiente.
     *
     * @param drone dron que mide
     * @return resultado de la medicion
     */
    String medirTemperatura(Drone drone);
}
