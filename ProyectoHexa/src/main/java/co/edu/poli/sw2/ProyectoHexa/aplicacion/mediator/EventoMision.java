package co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator;

/**
 * Eventos que los componentes informan al mediador durante la
 * preparacion de una mision.
 *
 * <p>
 * Los componentes no saben que pasa despues de cada evento: solo lo
 * comunican, y el mediador decide cual es el siguiente paso.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public enum EventoMision {

    /** El sistema de navegacion preparo la ruta. */
    RUTA_PREPARADA("Ruta preparada"),

    /** El sistema de bateria confirmo energia suficiente. */
    BATERIA_SUFICIENTE("Bateria suficiente"),

    /** El sistema de bateria detecto energia insuficiente. */
    BATERIA_INSUFICIENTE("Bateria insuficiente"),

    /** El sistema de sensores activo los sensores. */
    SENSORES_ACTIVADOS("Sensores activados"),

    /** El sistema de comunicacion establecio el enlace. */
    ENLACE_ESTABLECIDO("Enlace establecido");

    /** Descripcion visible del evento. */
    private final String descripcion;

    /**
     * Crea el evento.
     *
     * @param descripcion descripcion visible
     */
    EventoMision(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la descripcion visible.
     *
     * @return descripcion del evento
     */
    public String getDescripcion() {
        return descripcion;
    }
}
