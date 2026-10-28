package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.List;
import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ProgramarMisionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneCommand;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneOperacion;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.TipoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * <b>Cliente</b> del patron Command.
 *
 * <p>
 * Crea cada comando con su receptor ({@link DroneOperacion}) y su dron,
 * y lo entrega al invocador ({@link ProgramadorTareas}), que lo guarda
 * hasta ejecutar la mision.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ProgramarMisionServicio implements ProgramarMisionUseCase {

    /** Receptor de las operaciones. */
    private final DroneOperacion receptor;

    /** Invocador que guarda y ejecuta las tareas. */
    private final ProgramadorTareas programador;

    /**
     * Construye el servicio.
     *
     * @param receptor    receptor de las operaciones del dron
     * @param programador invocador de las tareas
     */
    public ProgramarMisionServicio(DroneOperacion receptor, ProgramadorTareas programador) {
        this.receptor = Objects.requireNonNull(receptor);
        this.programador = Objects.requireNonNull(programador);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si falta la tarea o el dron
     */
    @Override
    public void programar(TipoTarea tipo, Drone drone) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone.");
        }
        if (tipo == null) {
            throw new DronValidacionException("Seleccione una tarea.");
        }
        programador.agregar(tipo.crear(receptor, drone));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quitar(int indice) {
        programador.quitar(indice);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void limpiar() {
        programador.limpiar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DroneCommand> tareasProgramadas() {
        return programador.getTareas();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResultadoTarea> ejecutarMision() {
        return programador.ejecutarTodas();
    }
}
