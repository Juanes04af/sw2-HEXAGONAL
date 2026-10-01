package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * Pruebas unitarias de las reglas de negocio de {@link ValidadorDrone}.
 * No requieren base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ValidadorDroneTest {

    // ---------------- Serial ----------------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t" })
    @DisplayName("El serial vacio o nulo es rechazado")
    void serial_vacio_lanzaExcepcion(String serial) {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.validarSerial(serial));

        assertEquals("El serial es obligatorio.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "---", "@#$", "..." })
    @DisplayName("El serial sin caracteres alfanumericos es rechazado")
    void serial_sinAlfanumericos_lanzaExcepcion(String serial) {
        assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.validarSerial(serial));
    }

    @ParameterizedTest
    @ValueSource(strings = { "VIG-001", "SN123", "A" })
    @DisplayName("Un serial valido no lanza excepcion")
    void serial_valido_noLanza(String serial) {
        assertDoesNotThrow(() -> ValidadorDrone.validarSerial(serial));
    }

    // ---------------- Fabricante ----------------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("El fabricante vacio o nulo es rechazado")
    void fabricante_vacio_lanzaExcepcion(String fabricante) {
        assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.validarFabricante(fabricante));
    }

    @ParameterizedTest
    @ValueSource(strings = { "123", "12.5", " 45,7 " })
    @DisplayName("El fabricante solamente numerico es rechazado")
    void fabricante_soloNumerico_lanzaExcepcion(String fabricante) {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.validarFabricante(fabricante));

        assertEquals("El fabricante no puede ser solamente numerico.", ex.getMessage());
    }

    @Test
    @DisplayName("El fabricante sin caracteres alfanumericos es rechazado")
    void fabricante_sinAlfanumericos_lanzaExcepcion() {
        assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.validarFabricante("***"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "DJI", "Autel Robotics", "3DR" })
    @DisplayName("Un fabricante valido no lanza excepcion")
    void fabricante_valido_noLanza(String fabricante) {
        assertDoesNotThrow(() -> ValidadorDrone.validarFabricante(fabricante));
    }

    // ---------------- Peso ----------------

    @ParameterizedTest
    @CsvSource({ "0.9, 0.9", "'1,5', 1.5", "' 2 ', 2.0", "38, 38.0" })
    @DisplayName("El peso valido se convierte a double (acepta coma o punto)")
    void peso_valido_seConvierte(String texto, double esperado) {
        assertEquals(esperado, ValidadorDrone.parsearPeso(texto), 0.0001);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("El peso vacio o nulo es rechazado")
    void peso_vacio_lanzaExcepcion(String texto) {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.parsearPeso(texto));

        assertEquals("El peso es obligatorio.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-1", "-0.5" })
    @DisplayName("El peso cero o negativo es rechazado")
    void peso_noPositivo_lanzaExcepcion(String texto) {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.parsearPeso(texto));

        assertEquals("El peso debe ser mayor que cero.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "abc", "1.2.3", "dos" })
    @DisplayName("El peso no numerico es rechazado")
    void peso_noNumerico_lanzaExcepcion(String texto) {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.parsearPeso(texto));

        assertEquals("El peso debe ser un numero valido.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "NaN", "Infinity" })
    @DisplayName("Los valores especiales NaN e Infinity son rechazados")
    void peso_valoresEspeciales_lanzaExcepcion(String texto) {
        assertThrows(DronValidacionException.class,
                () -> ValidadorDrone.parsearPeso(texto));
    }

    // ---------------- Estructura ----------------

    @Test
    @DisplayName("ValidadorDrone es una clase utilitaria: final y con constructor privado")
    void clase_esUtilitaria() {
        Constructor<?>[] constructores = ValidadorDrone.class.getDeclaredConstructors();

        assertTrue(Modifier.isFinal(ValidadorDrone.class.getModifiers()));
        assertEquals(1, constructores.length);
        assertTrue(Modifier.isPrivate(constructores[0].getModifiers()));
    }
}
