package co.edu.poli.sw2.ProyectoHexa.infraestructura.weather;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lector minimo de JSON para las respuestas de Open-Meteo, escrito
 * con Java estandar para no depender de librerias externas.
 *
 * <p>
 * No es un lector JSON general: solo resuelve lo que necesitan los
 * adaptadores, que es ubicar un bloque por nombre y leer valores
 * numericos dentro de el.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class LectorJson {

    /** Numero JSON: signo opcional, decimales y exponente. */
    private static final String NUMERO = "-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

    /** Evita instancias de la utilidad. */
    private LectorJson() {
    }

    /**
     * Devuelve el contenido del objeto asociado a una clave, por
     * ejemplo el bloque {@code "current": {...}}. Supone que el bloque
     * no contiene objetos anidados, como ocurre en Open-Meteo.
     *
     * @param json  texto JSON completo
     * @param clave nombre del bloque
     * @return contenido entre llaves, o {@code null} si no existe
     */
    static String bloque(String json, String clave) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(clave) + "\"\\s*:\\s*\\{").matcher(json);
        if (!m.find()) {
            return null;
        }
        int fin = json.indexOf('}', m.end());
        return fin < 0 ? null : json.substring(m.end(), fin);
    }

    /**
     * Devuelve el primer elemento de un arreglo de objetos, por ejemplo
     * el primer resultado de {@code "results": [{...}, ...]}.
     *
     * @param json  texto JSON completo
     * @param clave nombre del arreglo
     * @return contenido del primer objeto, o {@code null} si el arreglo
     *         no existe o esta vacio
     */
    static String primerElemento(String json, String clave) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(clave) + "\"\\s*:\\s*\\[\\s*\\{")
                .matcher(json);
        if (!m.find()) {
            return null;
        }
        int fin = json.indexOf('}', m.end());
        return fin < 0 ? null : json.substring(m.end(), fin);
    }

    /**
     * Lee un valor numerico de un fragmento JSON.
     *
     * @param fragmento texto donde buscar
     * @param clave     nombre del campo
     * @return valor del campo, o {@code null} si no existe o es nulo
     */
    static Double numero(String fragmento, String clave) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(clave) + "\"\\s*:\\s*(" + NUMERO + ")")
                .matcher(fragmento);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }
}
