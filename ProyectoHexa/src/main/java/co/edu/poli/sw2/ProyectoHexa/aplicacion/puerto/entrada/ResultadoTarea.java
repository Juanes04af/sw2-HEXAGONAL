package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Resultado de la ejecucion de una tarea de la mision.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ResultadoTarea {

    /** Posicion de la tarea en la mision, empezando en 1. */
    private final int orden;

    /** Nombre de la tarea. */
    private final String tarea;

    /** Dron que la ejecuto. */
    private final Drone drone;

    /** Resultado o mensaje de error. */
    private final String mensaje;

    /** Indica si la tarea termino sin errores. */
    private final boolean exitosa;

    /**
     * Crea el resultado.
     *
     * @param orden   posicion en la mision
     * @param tarea   nombre de la tarea
     * @param drone   dron que la ejecuto
     * @param mensaje resultado o error
     * @param exitosa {@code true} si termino sin errores
     */
    public ResultadoTarea(int orden, String tarea, Drone drone, String mensaje, boolean exitosa) {
        this.orden = orden;
        this.tarea = tarea;
        this.drone = drone;
        this.mensaje = mensaje;
        this.exitosa = exitosa;
    }

    /**
     * Obtiene la posicion en la mision.
     *
     * @return orden, empezando en 1
     */
    public int getOrden() {
        return orden;
    }

    /**
     * Obtiene el nombre de la tarea.
     *
     * @return nombre
     */
    public String getTarea() {
        return tarea;
    }

    /**
     * Obtiene el dron que la ejecuto.
     *
     * @return dron
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Obtiene el resultado o el mensaje de error.
     *
     * @return mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Indica si la tarea termino sin errores.
     *
     * @return {@code true} si fue exitosa
     */
    public boolean isExitosa() {
        return exitosa;
    }
}
