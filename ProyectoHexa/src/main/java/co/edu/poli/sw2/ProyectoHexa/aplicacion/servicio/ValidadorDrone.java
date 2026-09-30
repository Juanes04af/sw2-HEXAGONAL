package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.regex.Pattern;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Contiene las reglas de validacion utilizadas por
 * los casos de uso relacionados con drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ValidadorDrone {

    private static final Pattern TIENE_ALFANUMERICO =
            Pattern.compile("[A-Za-z0-9]");

    private static final Pattern SOLO_NUMERICO =
            Pattern.compile("^\\s*[0-9]+([.,][0-9]+)?\\s*$");

    /**
     * Evita crear instancias del validador.
     */
    private ValidadorDrone() {
    }

    /**
     * Valida el serial.
     *
     * @param serial serial capturado
     */
    public static void validarSerial(String serial) {

        if (serial == null || serial.isBlank()) {
            throw new DronValidacionException(
                    "El serial es obligatorio.");
        }

        if (!TIENE_ALFANUMERICO.matcher(serial).find()) {
            throw new DronValidacionException(
                    "El serial debe contener caracteres alfanumericos.");
        }
    }

    /**
     * Valida el fabricante.
     *
     * @param fabricante fabricante capturado
     */
    public static void validarFabricante(String fabricante) {

        if (fabricante == null || fabricante.isBlank()) {
            throw new DronValidacionException(
                    "El fabricante es obligatorio.");
        }

        if (!TIENE_ALFANUMERICO.matcher(fabricante).find()) {
            throw new DronValidacionException(
                    "El fabricante debe contener caracteres alfanumericos.");
        }

        if (SOLO_NUMERICO.matcher(fabricante).matches()) {
            throw new DronValidacionException(
                    "El fabricante no puede ser solamente numerico.");
        }
    }

    /**
     * Convierte y valida el peso.
     *
     * @param pesoTexto peso capturado
     * @return peso convertido a double
     */
    public static double parsearPeso(String pesoTexto) {

        if (pesoTexto == null || pesoTexto.isBlank()) {
            throw new DronValidacionException(
                    "El peso es obligatorio.");
        }

        try {

            double peso = Double.parseDouble(
                    pesoTexto.trim().replace(",", "."));

            if (peso <= 0) {
                throw new DronValidacionException(
                        "El peso debe ser mayor que cero.");
            }

            return peso;

        } catch (NumberFormatException e) {

            throw new DronValidacionException(
                    "El peso debe ser un numero valido.");
        }
    }
}