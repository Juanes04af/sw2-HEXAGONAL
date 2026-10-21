package co.edu.poli.sw2.ProyectoHexa.dominio.excepcion;

/**
 * Excepcion lanzada cuando no es posible obtener el clima de una
 * ciudad, por ejemplo si la API externa no responde o la ciudad no
 * se encuentra.
 *
 * <p>
 * Hereda de {@link DronException} para que la interfaz la maneje con
 * el mismo bloque {@code catch} que el resto de errores del dominio.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ClimaException extends DronException {

    private static final long serialVersionUID = 1L;

    /**
     * Construye la excepcion con mensaje.
     *
     * @param mensaje descripcion del error
     */
    public ClimaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Construye la excepcion con mensaje y causa.
     *
     * @param mensaje descripcion del error
     * @param causa   causa original
     */
    public ClimaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
