package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.ConexionBDException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Adaptador de salida que implementa la persistencia
 * de drones utilizando PostgreSQL y JDBC.
 *
 * Actualmente solamente maneja drones de tipo Vigilancia.
 * Crear y modificar comparten el manejo de la transaccion
 * ({@link #enTransaccion}); buscar y listar comparten la consulta base.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class PostgresDroneRepository implements DroneRepository {

    private static final String SQLSTATE_UNIQUE = "23505";
    private static final String SQLSTATE_CHECK = "23514";
    private static final String SQLSTATE_NOT_NULL = "23502";

    private static final String TIPO_VIGILANCIA = "VIGILANCIA";

    private static final String SQL_INSERTAR_DRON =
            "INSERT INTO dron (serial, fabricante, modelo, peso, tipo) "
          + "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String SQL_INSERTAR_VIGILANCIA =
            "INSERT INTO dron_vigilancia (id_dron, deteccion_termica) VALUES (?, ?)";

    private static final String SQL_MODIFICAR_DRON =
            "UPDATE dron SET serial=?, fabricante=?, modelo=?, peso=? "
          + "WHERE id=? AND tipo='VIGILANCIA'";

    private static final String SQL_MODIFICAR_VIGILANCIA =
            "UPDATE dron_vigilancia SET deteccion_termica=? WHERE id_dron=?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM dron WHERE id=? AND tipo='VIGILANCIA'";

    /** Consulta base compartida por buscarPorId y listar. */
    private static final String SQL_SELECT_VIGILANCIA =
            "SELECT d.id, d.serial, d.fabricante, d.modelo, d.peso, v.deteccion_termica "
          + "FROM dron d INNER JOIN dron_vigilancia v ON v.id_dron = d.id "
          + "WHERE d.tipo='VIGILANCIA' ";

    /**
     * Operacion JDBC que se ejecuta dentro de una transaccion.
     */
    @FunctionalInterface
    private interface OperacionTransaccional {
        void ejecutar(Connection conexion, Vigilancia vigilancia) throws SQLException;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone crear(Drone drone) {
        return enTransaccion(drone, (conexion, vigilancia) -> {
            try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_DRON)) {
                asignarDatosComunes(ps, vigilancia);
                ps.setString(5, TIPO_VIGILANCIA);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ConexionBDException("No se pudo obtener el id creado.");
                    }
                    vigilancia.setId(rs.getInt("id"));
                }
            }
            try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR_VIGILANCIA)) {
                ps.setInt(1, vigilancia.getId());
                ps.setBoolean(2, vigilancia.isDeteccionTermica());
                ps.executeUpdate();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone modificar(Drone drone) {
        return enTransaccion(drone, (conexion, vigilancia) -> {
            try (PreparedStatement ps = conexion.prepareStatement(SQL_MODIFICAR_DRON)) {
                asignarDatosComunes(ps, vigilancia);
                ps.setInt(5, vigilancia.getId());
                if (ps.executeUpdate() == 0) {
                    throw new DronNoEncontradoException(vigilancia.getId());
                }
            }
            try (PreparedStatement ps = conexion.prepareStatement(SQL_MODIFICAR_VIGILANCIA)) {
                ps.setBoolean(1, vigilancia.isDeteccionTermica());
                ps.setInt(2, vigilancia.getId());
                ps.executeUpdate();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminar(Drone drone) {
        try (Connection conexion = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, drone.getId());
            if (ps.executeUpdate() == 0) {
                throw new DronNoEncontradoException(drone.getId());
            }
        } catch (SQLException e) {
            throw new ConexionBDException("No fue posible eliminar el drone.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone buscarPorId(int id) {
        try (Connection conexion = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_SELECT_VIGILANCIA + "AND d.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DronNoEncontradoException(id);
                }
                return mapearVigilancia(rs);
            }
        } catch (SQLException e) {
            throw new ConexionBDException("No fue posible buscar el drone.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Drone> listar() {
        List<Drone> drones = new ArrayList<>();
        try (Connection conexion = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(SQL_SELECT_VIGILANCIA + "ORDER BY d.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                drones.add(mapearVigilancia(rs));
            }
            return drones;
        } catch (SQLException e) {
            throw new ConexionBDException("No fue posible listar los drones.", e);
        }
    }

    /**
     * Abre una conexion, ejecuta la operacion en una transaccion y hace
     * commit; si algo falla, hace rollback y traduce el error.
     * Lo comparten crear y modificar, que escriben en dos tablas.
     */
    private Drone enTransaccion(Drone drone, OperacionTransaccional operacion) {
        validarTipo(drone);
        Vigilancia vigilancia = (Vigilancia) drone;
        Connection conexion = null;
        try {
            conexion = ConexionBD.getInstancia().obtenerConexion();
            conexion.setAutoCommit(false);
            operacion.ejecutar(conexion, vigilancia);
            conexion.commit();
            return drone;
        } catch (SQLException e) {
            rollback(conexion);
            throw traducirExcepcion(e, drone.getSerial());
        } catch (RuntimeException e) {
            rollback(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    /** Asigna serial, fabricante, modelo y peso (parametros 1 a 4). */
    private void asignarDatosComunes(PreparedStatement ps, Drone drone) throws SQLException {
        ps.setString(1, drone.getSerial());
        ps.setString(2, drone.getFabricante());
        ps.setString(3, drone.getModelo());
        ps.setDouble(4, drone.getPeso());
    }

    private Drone mapearVigilancia(ResultSet rs) throws SQLException {
        return new Vigilancia(
                rs.getInt("id"),
                rs.getString("serial"),
                rs.getString("fabricante"),
                rs.getString("modelo"),
                rs.getDouble("peso"),
                rs.getBoolean("deteccion_termica"));
    }

    private void validarTipo(Drone drone) {
        if (!(drone instanceof Vigilancia)) {
            throw new DronValidacionException(
                    "Actualmente solo se admiten drones de vigilancia.");
        }
    }

    private RuntimeException traducirExcepcion(SQLException e, String serial) {
        String estado = e.getSQLState();
        if (SQLSTATE_UNIQUE.equals(estado)) {
            return new DronDuplicadoException(serial);
        }
        if (SQLSTATE_CHECK.equals(estado) || SQLSTATE_NOT_NULL.equals(estado)) {
            return new DronValidacionException(
                    "Los datos no cumplen las restricciones de la base de datos.");
        }
        return new ConexionBDException("No fue posible guardar el drone.", e);
    }

    private void rollback(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.rollback();
            } catch (SQLException ignorada) {
                // Se conserva la excepcion original.
            }
        }
    }

    private void cerrar(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.setAutoCommit(true);
                conexion.close();
            } catch (SQLException ignorada) {
                // La conexion ya no se usa.
            }
        }
    }
}
