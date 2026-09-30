package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada encargado de crear drones de vigilancia.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface CrearDroneUseCase {

    /**
     * Crea un nuevo dron de vigilancia.
     *
     * @param serial serial
     * @param fabricante fabricante
     * @param modelo modelo
     * @param pesoTexto peso capturado como texto
     * @param deteccionTermica deteccion termica
     * @return dron creado
     */
    Drone crear(String serial,
                String fabricante,
                String modelo,
                String pesoTexto,
                boolean deteccionTermica);
}