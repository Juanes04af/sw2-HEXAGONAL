package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;

/**
 * Ayuda de las pruebas de integracion: si PostgreSQL no esta disponible,
 * la prueba se omite en lugar de fallar.
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class PostgresDisponible {

    private PostgresDisponible() { }

    static boolean estaDisponible() {
        try (Connection c = ConexionBD.getInstancia().obtenerConexion()) {
            return c.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    static void requerir() {
        assumeTrue(estaDisponible(),
                "PostgreSQL no esta disponible: se omite la prueba de integracion.");
    }
}
