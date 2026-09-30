package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ModificarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Servicio encargado de modificar drones de vigilancia.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ModificarDroneServicio implements ModificarDroneUseCase {

    private final DroneRepository droneRepository;

    /**
     * Construye el servicio.
     *
     * @param droneRepository repositorio utilizado por el servicio
     */
    public ModificarDroneServicio(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone modificar(Drone existente,
                           String serial,
                           String fabricante,
                           String modelo,
                           String pesoTexto,
                           boolean deteccionTermica) {

        if (existente == null) {
            throw new DronValidacionException(
                    "Debe seleccionar un drone.");
        }

        if (!(existente instanceof Vigilancia)) {
            throw new DronValidacionException(
                    "Actualmente solo se pueden modificar drones de vigilancia.");
        }

        ValidadorDrone.validarSerial(serial);
        ValidadorDrone.validarFabricante(fabricante);

        double peso = ValidadorDrone.parsearPeso(pesoTexto);

        existente.setSerial(serial.trim());
        existente.setFabricante(fabricante.trim());
        existente.setModelo(
                modelo == null ? null : modelo.trim());
        existente.setPeso(peso);

        Vigilancia vigilancia = (Vigilancia) existente;
        vigilancia.setDeteccionTermica(deteccionTermica);

        return droneRepository.modificar(vigilancia);
    }
}