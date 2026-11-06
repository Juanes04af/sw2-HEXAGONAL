package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

/**
 * Paso registrado por el mediador durante la preparacion de una mision.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class PasoMision {

    /** Posicion del paso, empezando en 1. */
    private final int orden;

    /** Componente que informo el paso. */
    private final String componente;

    /** Evento informado. */
    private final String evento;

    /** Descripcion del resultado. */
    private final String detalle;

    /** Indica si el paso fue exitoso. */
    private final boolean exitoso;

    /**
     * Crea un paso.
     *
     * @param orden      posicion del paso
     * @param componente componente que lo informo
     * @param evento     evento informado
     * @param detalle    descripcion del resultado
     * @param exitoso    {@code true} si fue exitoso
     */
    public PasoMision(int orden, String componente, String evento, String detalle,
                      boolean exitoso) {
        this.orden = orden;
        this.componente = componente;
        this.evento = evento;
        this.detalle = detalle;
        this.exitoso = exitoso;
    }

    /**
     * Obtiene la posicion del paso.
     *
     * @return orden, empezando en 1
     */
    public int getOrden() {
        return orden;
    }

    /**
     * Obtiene el componente que informo el paso.
     *
     * @return nombre del componente
     */
    public String getComponente() {
        return componente;
    }

    /**
     * Obtiene el evento informado.
     *
     * @return descripcion del evento
     */
    public String getEvento() {
        return evento;
    }

    /**
     * Obtiene la descripcion del resultado.
     *
     * @return detalle
     */
    public String getDetalle() {
        return detalle;
    }

    /**
     * Indica si el paso fue exitoso.
     *
     * @return {@code true} si fue exitoso
     */
    public boolean isExitoso() {
        return exitoso;
    }
}
