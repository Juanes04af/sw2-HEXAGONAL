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
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class PostgresDroneRepository implements DroneRepository {

    private static final String SQLSTATE_UNIQUE = "23505";
    private static final String SQLSTATE_CHECK = "23514";
    private static final String SQLSTATE_NOT_NULL = "23502";

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone crear(Drone drone) {

        validarTipo(drone);

        String sqlDrone =
                "INSERT INTO dron "
              + "(serial, fabricante, modelo, peso, tipo) "
              + "VALUES (?, ?, ?, ?, ?) RETURNING id";

        String sqlVigilancia =
                "INSERT INTO dron_vigilancia "
              + "(id_dron, deteccion_termica) VALUES (?, ?)";

        Connection conexion = null;

        try {

            conexion = ConexionBD.getInstancia()
                    .obtenerConexion();

            conexion.setAutoCommit(false);

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlDrone)) {

                ps.setString(1, drone.getSerial());
                ps.setString(2, drone.getFabricante());
                ps.setString(3, drone.getModelo());
                ps.setDouble(4, drone.getPeso());
                ps.setString(5, "VIGILANCIA");

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new ConexionBDException(
                                "No se pudo obtener el id creado.");
                    }

                    drone.setId(rs.getInt("id"));
                }
            }

            Vigilancia vigilancia = (Vigilancia) drone;

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlVigilancia)) {

                ps.setInt(1, vigilancia.getId());
                ps.setBoolean(
                        2,
                        vigilancia.isDeteccionTermica());

                ps.executeUpdate();
            }

            conexion.commit();

            return drone;

        } catch (SQLException e) {

            rollback(conexion);

            throw traducirExcepcion(
                    e,
                    drone.getSerial());

        } catch (RuntimeException e) {

            rollback(conexion);

            throw e;

        } finally {

            cerrar(conexion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone modificar(Drone drone) {

        validarTipo(drone);

        String sqlDrone =
                "UPDATE dron "
              + "SET serial=?, fabricante=?, modelo=?, peso=? "
              + "WHERE id=? AND tipo='VIGILANCIA'";

        String sqlVigilancia =
                "UPDATE dron_vigilancia "
              + "SET deteccion_termica=? "
              + "WHERE id_dron=?";

        Connection conexion = null;

        try {

            conexion = ConexionBD.getInstancia()
                    .obtenerConexion();

            conexion.setAutoCommit(false);

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlDrone)) {

                ps.setString(1, drone.getSerial());
                ps.setString(2, drone.getFabricante());
                ps.setString(3, drone.getModelo());
                ps.setDouble(4, drone.getPeso());
                ps.setInt(5, drone.getId());

                if (ps.executeUpdate() == 0) {
                    throw new DronNoEncontradoException(
                            drone.getId());
                }
            }

            Vigilancia vigilancia = (Vigilancia) drone;

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlVigilancia)) {

                ps.setBoolean(
                        1,
                        vigilancia.isDeteccionTermica());

                ps.setInt(
                        2,
                        vigilancia.getId());

                ps.executeUpdate();
            }

            conexion.commit();

            return drone;

        } catch (SQLException e) {

            rollback(conexion);

            throw traducirExcepcion(
                    e,
                    drone.getSerial());

        } catch (RuntimeException e) {

            rollback(conexion);

            throw e;

        } finally {

            cerrar(conexion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminar(Drone drone) {

        String sql =
                "DELETE FROM dron "
              + "WHERE id=? AND tipo='VIGILANCIA'";

        try (Connection conexion =
                     ConexionBD.getInstancia()
                             .obtenerConexion();

             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, drone.getId());

            if (ps.executeUpdate() == 0) {
                throw new DronNoEncontradoException(
                        drone.getId());
            }

        } catch (SQLException e) {

            throw new ConexionBDException(
                    "No fue posible eliminar el drone.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone buscarPorId(int id) {

        String sql =
                "SELECT d.id, d.serial, d.fabricante, "
              + "d.modelo, d.peso, v.deteccion_termica "
              + "FROM dron d "
              + "INNER JOIN dron_vigilancia v "
              + "ON v.id_dron = d.id "
              + "WHERE d.id=? AND d.tipo='VIGILANCIA'";

        try (Connection conexion =
                     ConexionBD.getInstancia()
                             .obtenerConexion();

             PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new DronNoEncontradoException(id);
                }

                return mapearVigilancia(rs);
            }

        } catch (SQLException e) {

            throw new ConexionBDException(
                    "No fue posible buscar el drone.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Drone> listar() {

        String sql =
                "SELECT d.id, d.serial, d.fabricante, "
              + "d.modelo, d.peso, v.deteccion_termica "
              + "FROM dron d "
              + "INNER JOIN dron_vigilancia v "
              + "ON v.id_dron = d.id "
              + "WHERE d.tipo='VIGILANCIA' "
              + "ORDER BY d.id";

        List<Drone> drones = new ArrayList<>();

        try (Connection conexion =
                     ConexionBD.getInstancia()
                             .obtenerConexion();

             PreparedStatement ps =
                     conexion.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {
                drones.add(mapearVigilancia(rs));
            }

            return drones;

        } catch (SQLException e) {

            throw new ConexionBDException(
                    "No fue posible listar los drones.", e);
        }
    }

    /**
     * Convierte una fila de PostgreSQL en un objeto Vigilancia.
     *
     * @param rs resultado SQL
     * @return dron construido
     * @throws SQLException si ocurre un error de lectura
     */
    private Drone mapearVigilancia(ResultSet rs)
            throws SQLException {

        return new Vigilancia(
                rs.getInt("id"),
                rs.getString("serial"),
                rs.getString("fabricante"),
                rs.getString("modelo"),
                rs.getDouble("peso"),
                rs.getBoolean("deteccion_termica")
        );
    }

    /**
     * Verifica que el dron sea de vigilancia.
     *
     * @param drone dron recibido
     */
    private void validarTipo(Drone drone) {

        if (!(drone instanceof Vigilancia)) {

            throw new DronValidacionException(
                    "Actualmente solo se admiten drones de vigilancia.");
        }
    }

    /**
     * Traduce errores PostgreSQL.
     *
     * @param e excepcion SQL
     * @param serial serial involucrado
     * @return excepcion correspondiente
     */
    private RuntimeException traducirExcepcion(
            SQLException e,
            String serial) {

        String estado = e.getSQLState();

        if (SQLSTATE_UNIQUE.equals(estado)) {
            return new DronDuplicadoException(serial);
        }

        if (SQLSTATE_CHECK.equals(estado)
                || SQLSTATE_NOT_NULL.equals(estado)) {

            return new DronValidacionException(
                    "Los datos no cumplen "
                  + "las restricciones de la base de datos.");
        }

        return new ConexionBDException(
                "No fue posible guardar el drone.", e);
    }

    /**
     * Revierte una transaccion.
     *
     * @param conexion conexion activa
     */
    private void rollback(Connection conexion) {

        if (conexion != null) {

            try {
                conexion.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Cierra una conexion.
     *
     * @param conexion conexion activa
     */
    private void cerrar(Connection conexion) {

        if (conexion != null) {

            try {
                conexion.setAutoCommit(true);
                conexion.close();
            } catch (SQLException ignored) {
            }
        }
    }
}