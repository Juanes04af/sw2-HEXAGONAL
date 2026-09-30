package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CrearDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Servicio de aplicacion encargado de crear drones.
 *
 * Actualmente crea exclusivamente drones de tipo Vigilancia.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class CrearDroneServicio implements CrearDroneUseCase {

    private final DroneRepository droneRepository;

    /**
     * Construye el servicio.
     *
     * @param droneRepository puerto de salida para persistencia
     */
    public CrearDroneServicio(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone crear(String serial,
                       String fabricante,
                       String modelo,
                       String pesoTexto,
                       boolean deteccionTermica) {

        ValidadorDrone.validarSerial(serial);
        ValidadorDrone.validarFabricante(fabricante);

        double peso = ValidadorDrone.parsearPeso(pesoTexto);

        Vigilancia vigilancia = new Vigilancia(
                0,
                serial.trim(),
                fabricante.trim(),
                modelo == null ? null : modelo.trim(),
                peso,
                deteccionTermica
        );

        return droneRepository.crear(vigilancia);
    }
}