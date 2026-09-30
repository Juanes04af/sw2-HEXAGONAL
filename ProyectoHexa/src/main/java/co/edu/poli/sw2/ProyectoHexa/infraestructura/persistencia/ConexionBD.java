package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ConexionBDException;

/**
 * Gestiona las conexiones JDBC con PostgreSQL.
 *
 * Implementa el patron Singleton para mantener una unica
 * instancia encargada de la configuracion de conexion.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ConexionBD {

    private static volatile ConexionBD instancia;

    private final String urlBD;
    private final String usuarioBD;
    private final String passwordBD;

    /**
     * Construye internamente el gestor de conexion.
     */
    private ConexionBD() {

        Properties propiedades = cargarPropiedades();

        urlBD = resolver(
                propiedades,
                "DB_URL",
                "db.url",
                "jdbc:postgresql://localhost:5432/dronesdb");

        usuarioBD = resolver(
                propiedades,
                "DB_USER",
                "db.user",
                null);

        passwordBD = resolver(
                propiedades,
                "DB_PASSWORD",
                "db.password",
                null);

        try {

            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {

            throw new ConexionBDException(
                    "No se encontro el driver PostgreSQL.", e);
        }
    }

    /**
     * Devuelve la unica instancia disponible.
     *
     * @return instancia Singleton
     */
    public static ConexionBD getInstancia() {

        ConexionBD resultado = instancia;

        if (resultado == null) {

            synchronized (ConexionBD.class) {

                resultado = instancia;

                if (resultado == null) {
                    instancia = resultado = new ConexionBD();
                }
            }
        }

        return resultado;
    }

    /**
     * Abre una conexion JDBC.
     *
     * @return conexion abierta
     */
    public Connection obtenerConexion() {

        if (usuarioBD == null || passwordBD == null) {

            throw new ConexionBDException(
                    "Faltan las credenciales de la base de datos.");
        }

        try {

            return DriverManager.getConnection(
                    urlBD,
                    usuarioBD,
                    passwordBD);

        } catch (SQLException e) {

            throw new ConexionBDException(
                    "No fue posible conectarse con PostgreSQL.", e);
        }
    }

    /**
     * Obtiene una propiedad usando variables de entorno
     * o db.properties.
     *
     * @param propiedades propiedades
     * @param variableEntorno variable de entorno
     * @param propiedad nombre de propiedad
     * @param defecto valor por defecto
     * @return valor encontrado
     */
    private static String resolver(Properties propiedades,
                                   String variableEntorno,
                                   String propiedad,
                                   String defecto) {

        String valor = System.getenv(variableEntorno);

        if (valor != null && !valor.isBlank()) {
            return valor;
        }

        valor = propiedades.getProperty(propiedad);

        if (valor != null && !valor.isBlank()) {
            return valor;
        }

        return defecto;
    }

    /**
     * Carga db.properties.
     *
     * @return propiedades encontradas
     */
    private static Properties cargarPropiedades() {

        Properties propiedades = new Properties();

        try (InputStream entrada =
                     ConexionBD.class.getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (entrada != null) {
                propiedades.load(entrada);
            }

        } catch (IOException e) {
            // El archivo es opcional.
        }

        return propiedades;
    }
}