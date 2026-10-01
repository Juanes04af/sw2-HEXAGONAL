package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prueba de integracion de la conexion real con PostgreSQL.
 * Requiere PostgreSQL encendido y un archivo .env valido.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ConexionBDIT {

    @Test
    @DisplayName("El .env aporta usuario y contrasena")
    void env_tieneCredenciales() {
        assertNotNull(CargadorEnv.obtener("DB_USER"),
                "No se encontro DB_USER en el .env");
        assertNotNull(CargadorEnv.obtener("DB_PASSWORD"),
                "No se encontro DB_PASSWORD en el .env");
    }

    @Test
    @DisplayName("Se obtiene una conexion valida con PostgreSQL")
    void obtenerConexion_esValida() throws Exception {
        try (Connection c = ConexionBD.getInstancia().obtenerConexion()) {
            assertTrue(c.isValid(2), "La conexion no es valida");
        }
    }

    @Test
    @DisplayName("Cada llamada entrega una conexion nueva e independiente")
    void obtenerConexion_entregaConexionesDistintas() throws Exception {
        try (Connection a = ConexionBD.getInstancia().obtenerConexion();
             Connection b = ConexionBD.getInstancia().obtenerConexion()) {

            assertNotSame(a, b);
            a.close();
            assertTrue(b.isValid(2), "Cerrar una conexion no debe afectar a la otra");
        }
    }
}
