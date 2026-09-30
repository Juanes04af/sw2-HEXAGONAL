package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Excepcion lanzada cuando ya existe un dron
 * con el mismo serial.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DronDuplicadoException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion.
     *
     * @param serial serial duplicado
     */
    public DronDuplicadoException(String serial) {
        super("Ya existe un drone registrado con el serial '" + serial + "'.");
    }
}