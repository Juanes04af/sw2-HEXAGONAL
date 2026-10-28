package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Tareas que se pueden programar en una mision.
 *
 * <p>
 * Cada tipo sabe crear su comando concreto, de modo que agregar una
 * tarea nueva solo requiere un comando y un valor en este enum.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public enum TipoTarea {

    /** Tomar una fotografia. */
    TOMAR_FOTOGRAFIA("Tomar fotografia") {
        @Override
        public DroneCommand crear(DroneOperacion operacion, Drone drone) {
            return new TomarFotografiaCommand(operacion, drone);
        }
    },

    /** Escanear la zona. */
    ESCANEAR_ZONA("Escanear zona") {
        @Override
        public DroneCommand crear(DroneOperacion operacion, Drone drone) {
            return new EscanearZonaCommand(operacion, drone);
        }
    },

    /** Medir la temperatura. */
    MEDIR_TEMPERATURA("Medir temperatura") {
        @Override
        public DroneCommand crear(DroneOperacion operacion, Drone drone) {
            return new MedirTemperaturaCommand(operacion, drone);
        }
    };

    /** Nombre mostrado al usuario. */
    private final String nombre;

    /**
     * Crea el tipo de tarea.
     *
     * @param nombre nombre visible
     */
    TipoTarea(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Crea el comando concreto de este tipo.
     *
     * @param operacion receptor de la operacion
     * @param drone     dron que ejecutara la tarea
     * @return comando listo para programar
     */
    public abstract DroneCommand crear(DroneOperacion operacion, Drone drone);

    /**
     * Obtiene el nombre visible.
     *
     * @return nombre de la tarea
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el nombre visible, usado por los selectores de JavaFX.
     *
     * @return nombre de la tarea
     */
    @Override
    public String toString() {
        return nombre;
    }
}
