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
 * <p>
 * Implementa el patron <b>Singleton</b> mediante double-checked locking:
 * el constructor es privado y {@link #getInstancia()} es el unico punto
 * de acceso, de modo que toda la aplicacion comparte una misma
 * configuracion de conexion.
 * </p>
 *
 * <p>
 * Las credenciales se resuelven en este orden de prioridad:
 * </p>
 * <ol>
 *   <li>Variables de entorno del sistema operativo</li>
 *   <li>Archivo {@code .env} en la raiz del proyecto</li>
 *   <li>Archivo {@code db.properties} en el classpath</li>
 * </ol>
 *
 * @author Alejandra Cano y Juan Rosero
 * @see CargadorEnv
 */
public final class ConexionBD {

    /** Unica instancia. Es volatil para que el double-checked locking sea seguro. */
    private static volatile ConexionBD instancia;

    /** URL JDBC de la base de datos. */
    private final String urlBD;

    /** Usuario de la base de datos. */
    private final String usuarioBD;

    /** Contrasenia de la base de datos. */
    private final String passwordBD;

    /**
     * Constructor privado: solo se puede instanciar desde
     * {@link #getInstancia()}.
     *
     * @throws ConexionBDException si no se encuentra el driver de PostgreSQL
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
     * Abre una nueva conexion JDBC. Quien la obtiene es
     * responsable de cerrarla.
     *
     * @return conexion abierta
     * @throws ConexionBDException si faltan credenciales o el servidor no responde
     */
    public Connection obtenerConexion() {

        if (usuarioBD == null || passwordBD == null) {

            throw new ConexionBDException(
                    "Faltan las credenciales de la base de datos. "
                  + "Defina DB_USER y DB_PASSWORD en el archivo .env "
                  + "de la raiz del proyecto.");
        }

        try {

            return DriverManager.getConnection(
                    urlBD,
                    usuarioBD,
                    passwordBD);

        } catch (SQLException e) {

        	throw new ConexionBDException(
                    "No fue posible conectarse con PostgreSQL: "
                  + e.getMessage(), e);
        }
    }

    /**
     * Resuelve un valor de configuracion recorriendo las fuentes
     * por prioridad: variable de entorno, archivo .env y db.properties.
     *
     * @param propiedades     propiedades leidas de db.properties
     * @param variableEntorno nombre de la variable de entorno y clave del .env
     * @param propiedad       nombre de la clave en db.properties
     * @param defecto         valor si ninguna fuente aporta un dato
     * @return el primer valor no vacio encontrado, o {@code defecto}
     */
    private static String resolver(Properties propiedades,
                                   String variableEntorno,
                                   String propiedad,
                                   String defecto) {

        String valor = System.getenv(variableEntorno);

        if (esUtil(valor)) {
            return valor;
        }

        valor = CargadorEnv.obtener(variableEntorno);

        if (esUtil(valor)) {
            return valor;
        }

        valor = propiedades.getProperty(propiedad);

        if (esUtil(valor)) {
            return valor;
        }

        return defecto;
    }

    /**
     * Indica si un valor de configuracion es utilizable.
     *
     * @param valor cadena a evaluar
     * @return {@code true} si no es nula ni esta en blanco
     */
    private static boolean esUtil(String valor) {

        return valor != null && !valor.isBlank();
    }

    /**
     * Carga db.properties del classpath, si existe. Es una fuente opcional.
     *
     * @return propiedades encontradas, o un objeto vacio
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

    /**
     * Representacion segura: nunca expone usuario ni contrasenia.
     *
     * @return cadena fija sin credenciales
     */
    @Override
    public String toString() {

        return "ConexionBD[configurada]";
    }
}
