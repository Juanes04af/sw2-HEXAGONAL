package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Excepcion lanzada cuando el dron solicitado
 * no existe en persistencia.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DronNoEncontradoException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion.
     *
     * @param id identificador buscado
     */
    public DronNoEncontradoException(int id) {
        super("No se encontro un drone con id " + id + ".");
    }
}