package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Representa errores relacionados con la conexion
 * o comunicacion con la base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ConexionBDException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion con mensaje.
     *
     * @param mensaje descripcion del error
     */
    public ConexionBDException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye la excepcion con mensaje y causa.
     *
     * @param mensaje descripcion del error
     * @param causa causa original
     */
    public ConexionBDException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}