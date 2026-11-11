package co.edu.poli.sw2.ProyectoHexa.infraestructura.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas directas del lector minimo de JSON.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class LectorJsonTest {

    /** Barra invertida, escrita sin secuencias de escape. */
    private static final String B = Character.toString((char) 92);

    /** Permite escribir JSON con comillas simples y lo pasa a comillas dobles. */
    private static String j(String texto) {
        return texto.replace((char) 39, (char) 34);
    }

    // ---------------- bloque ----------------

    @Test
    @DisplayName("bloque devuelve el contenido del objeto asociado a la clave")
    void bloque_devuelveContenido() {
        String json = j("{'a':1,'current':{'x':1,'y':2},'z':3}");

        assertEquals(j("'x':1,'y':2"), LectorJson.bloque(json, "current"));
    }

    @Test
    @DisplayName("bloque tolera espacios alrededor de los dos puntos")
    void bloque_conEspacios() {
        String json = j("{'current' : { 'x':1 }}");

        assertEquals(j(" 'x':1 "), LectorJson.bloque(json, "current"));
    }

    @Test
    @DisplayName("bloque devuelve null si la clave no existe o el objeto no cierra")
    void bloque_inexistenteOSinCierre() {
        assertNull(LectorJson.bloque(j("{'a':1}"), "current"));
        assertNull(LectorJson.bloque(j("{'current':{'x':1"), "current"));
    }

    // ---------------- primerElemento ----------------

    @Test
    @DisplayName("primerElemento devuelve el primer objeto del arreglo")
    void primerElemento_devuelvePrimero() {
        String json = j("{'results':[{'name':'Zagreb','latitude':45.8},{'name':'Otro'}]}");

        assertEquals(j("'name':'Zagreb','latitude':45.8"),
                LectorJson.primerElemento(json, "results"));
    }

    @Test
    @DisplayName("primerElemento devuelve null con arreglo vacio o ausente")
    void primerElemento_vacioOAusente() {
        assertNull(LectorJson.primerElemento(j("{'results':[]}"), "results"));
        assertNull(LectorJson.primerElemento(j("{'a':1}"), "results"));
    }

    // ---------------- numero ----------------

    @Test
    @DisplayName("numero lee enteros, negativos, decimales y exponentes")
    void numero_formatos() {
        assertEquals(72.0, LectorJson.numero(j("'v':72"), "v"), 0.0001);
        assertEquals(-2.5, LectorJson.numero(j("'v':-2.5"), "v"), 0.0001);
        assertEquals(1500.0, LectorJson.numero(j("'v':1.5e3"), "v"), 0.0001);
        assertEquals(0.02, LectorJson.numero(j("'v':2E-2"), "v"), 0.0001);
    }

    @Test
    @DisplayName("numero devuelve null si el valor es null o la clave no existe")
    void numero_nuloOAusente() {
        assertNull(LectorJson.numero(j("'visibility':null"), "visibility"));
        assertNull(LectorJson.numero(j("'a':1"), "v"));
    }

    @Test
    @DisplayName("numero no confunde una clave con otra que solo empieza igual")
    void numero_claveParcial() {
        assertNull(LectorJson.numero(j("'temperature_2m':14.6"), "temp"));
    }

    // ---------------- texto ----------------

    @Test
    @DisplayName("texto lee una cadena simple, incluso con espacios alrededor de los dos puntos")
    void texto_simple() {
        assertEquals("Centro de Bogota", LectorJson.texto(j("'nombre' : 'Centro de Bogota'"), "nombre"));
    }

    @Test
    @DisplayName("texto desescapa comillas, saltos de linea, unicode y barras")
    void texto_escapes() {
        assertEquals(j("a'b"), LectorJson.texto(j("{'n':'a" + B + "'b'}"), "n"));
        assertEquals("l1" + (char) 10 + "l2", LectorJson.texto(j("{'n':'l1" + B + "nl2'}"), "n"));
        assertEquals("Bogot" + (char) 225, LectorJson.texto(j("{'n':'Bogot" + B + "u00e1'}"), "n"));
        assertEquals("C:" + B + "temp", LectorJson.texto(j("{'p':'C:" + B + B + "temp'}"), "p"));
    }

    @Test
    @DisplayName("texto devuelve null si el valor no es texto o la clave no existe")
    void texto_noEsTextoOAusente() {
        assertNull(LectorJson.texto(j("'n':5"), "n"));
        assertNull(LectorJson.texto(j("'a':'x'"), "n"));
    }
}
