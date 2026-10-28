package co.edu.poli.sw2.ProyectoHexa.infraestructura.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lector minimo de JSON para las respuestas de Open-Meteo y el archivo de ruta, escrito
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
public final class LectorJson {

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
    public static String bloque(String json, String clave) {
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
    public static String primerElemento(String json, String clave) {
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
    public static Double numero(String fragmento, String clave) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(clave) + "\"\\s*:\\s*(" + NUMERO + ")")
                .matcher(fragmento);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    /**
     * Lee un valor de texto de un fragmento JSON. Admite las secuencias
     * de escape basicas: comillas, barra invertida y unicode.
     *
     * @param fragmento texto donde buscar
     * @param clave     nombre del campo
     * @return valor del campo, o {@code null} si no existe o no es texto
     */
    public static String texto(String fragmento, String clave) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(clave)
                + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(fragmento);
        if (!m.find()) {
            return null;
        }
        return desescapar(m.group(1));
    }

    /**
     * Convierte las secuencias de escape de JSON en sus caracteres.
     *
     * @param valor texto con escapes
     * @return texto sin escapes
     */
    private static String desescapar(String valor) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c != '\\' || i + 1 >= valor.length()) {
                sb.append(c);
                continue;
            }
            char siguiente = valor.charAt(++i);
            switch (siguiente) {
                case 'n':
                    sb.append('\n');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'u':
                    if (i + 4 < valor.length()) {
                        sb.append((char) Integer.parseInt(valor.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    break;
                default:
                    sb.append(siguiente);
            }
        }
        return sb.toString();
    }
}
