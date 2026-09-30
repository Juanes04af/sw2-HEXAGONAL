package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Excepcion base utilizada por el dominio de drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DronException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye una excepcion con mensaje.
     *
     * @param mensaje descripcion del error
     */
    public DronException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye una excepcion con mensaje y causa.
     *
     * @param mensaje descripcion del error
     * @param causa causa original
     */
    public DronException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}