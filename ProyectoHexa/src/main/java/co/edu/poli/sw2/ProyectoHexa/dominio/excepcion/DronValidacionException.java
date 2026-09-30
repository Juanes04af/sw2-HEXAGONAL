package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;


/**
 * Excepcion lanzada cuando un dato del dron
 * no cumple las reglas de validacion.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DronValidacionException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion.
     *
     * @param mensaje regla que no fue cumplida
     */
    public DronValidacionException(String mensaje) {
        super(mensaje);
    }
}