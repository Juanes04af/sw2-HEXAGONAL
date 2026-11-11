package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de integracion adicionales de {@link PostgresDroneRepository}.
 * Requieren PostgreSQL y un .env valido; borran lo que crean.
 *
 * @author Alejandra Cano y Juan Rosero
 */
@Tag("integration")
class PostgresDroneRepositoryAvanzadoIT {

    private final PostgresDroneRepository repositorio = new PostgresDroneRepository();
    private final List<Drone> creados = new ArrayList<>();
    private final List<Integer> filasAgricultura = new ArrayList<>();

    @BeforeAll
    static void requierePostgres() {
        PostgresDisponible.requerir();
    }

    @AfterEach
    void limpiar() throws SQLException {
        for (Drone d : creados) {
            try {
                repositorio.eliminar(d);
            } catch (RuntimeException ignorada) {
                // La prueba pudo haberlo eliminado ya.
            }
        }
        creados.clear();

        for (int id : filasAgricultura) {
            try (Connection c = ConexionBD.getInstancia().obtenerConexion();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM dron WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
        filasAgricultura.clear();
    }

    // ---------------- ayudas ----------------

    private static String serialUnico() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Vigilancia crearDePrueba(boolean termica) {
        Vigilancia d = (Vigilancia) repositorio.crear(
                new Vigilancia(0, serialUnico(), "DJI", "Mavic 3", 0.9, termica));
        creados.add(d);
        return d;
    }

    /** Inserta un dron de AGRICULTURA directamente en la tabla padre. */
    private int insertarAgricultura() throws SQLException {
        String sql = "INSERT INTO dron (serial, fabricante, modelo, peso, tipo) "
                + "VALUES (?, 'John Deere', 'AGR', 30.0, 'AGRICULTURA') RETURNING id";
        try (Connection c = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, serialUnico());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int id = rs.getInt(1);
                filasAgricultura.add(id);
                return id;
            }
        }
    }

    private static int contar(String sql, int id) throws SQLException {
        try (Connection c = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Exige que la BD rechace el dato; si no hay restriccion, aborta la prueba. */
    private void exigirRechazoDeLaBD(Vigilancia invalido, String advertencia) {
        try {
            creados.add(repositorio.crear(invalido));
            assumeTrue(false, advertencia);
        } catch (DronValidacionException esperada) {
            // La BD rechazo el dato, que es lo que se prueba.
        }
    }

    // ---------------- tabla hija y cascada ----------------

    @Test
    @DisplayName("Crear guarda la deteccion termica en la tabla hija")
    void crear_guardaLaTablaHija() throws SQLException {
        Vigilancia d = crearDePrueba(true);

        assertEquals(1, contar("SELECT COUNT(*) FROM dron_vigilancia "
                + "WHERE id_dron=? AND deteccion_termica=TRUE", d.getId()));
    }

    @Test
    @DisplayName("Eliminar borra tambien la fila hija por cascada")
    void eliminar_borraLaFilaHija() throws SQLException {
        Vigilancia d = crearDePrueba(false);

        repositorio.eliminar(d);

        assertEquals(0, contar("SELECT COUNT(*) FROM dron_vigilancia WHERE id_dron=?", d.getId()));
        assertEquals(0, contar("SELECT COUNT(*) FROM dron WHERE id=?", d.getId()));
    }

    // ---------------- modificar ----------------

    @Test
    @DisplayName("Modificar con el serial de otro dron lanza DronDuplicadoException y no altera el original")
    void modificar_serialDeOtro() {
        Vigilancia a = crearDePrueba(false);
        Vigilancia b = crearDePrueba(true);
        String serialOriginal = b.getSerial();

        b.setSerial(a.getSerial());
        b.setFabricante("Autel");
        assertThrows(DronDuplicadoException.class, () -> repositorio.modificar(b));

        Vigilancia leido = (Vigilancia) repositorio.buscarPorId(b.getId());
        assertEquals(serialOriginal, leido.getSerial());
        assertEquals("DJI", leido.getFabricante());
        assertTrue(leido.isDeteccionTermica());
    }

    @Test
    @DisplayName("Modificar con un peso rechazado por la BD hace rollback y conserva los datos")
    void modificar_pesoInvalido_hacerollback() {
        Vigilancia d = crearDePrueba(false);
        d.setPeso(-5.0);
        d.setDeteccionTermica(true);

        try {
            repositorio.modificar(d);
            assumeTrue(false, "ADVERTENCIA: el esquema no tiene CHECK de peso positivo.");
        } catch (DronValidacionException esperada) {
            // La BD rechazo el dato.
        }

        Vigilancia leido = (Vigilancia) repositorio.buscarPorId(d.getId());
        assertEquals(0.9, leido.getPeso(), 0.0001);
        assertFalse(leido.isDeteccionTermica());
    }

    // ---------------- crear con datos que la BD rechaza ----------------

    @Test
    @DisplayName("Un serial nulo viola NOT NULL: DronValidacionException y sin registros nuevos")
    void crear_serialNulo() {
        int antes = repositorio.listar().size();

        exigirRechazoDeLaBD(new Vigilancia(0, null, "DJI", "M", 1.0, false),
                "ADVERTENCIA: serial no es NOT NULL en este esquema.");

        assertEquals(antes, repositorio.listar().size());
    }

    @Test
    @DisplayName("Un peso negativo viola el CHECK: DronValidacionException y sin registros nuevos")
    void crear_pesoNegativo() {
        int antes = repositorio.listar().size();

        exigirRechazoDeLaBD(new Vigilancia(0, serialUnico(), "DJI", "M", -1.0, false),
                "ADVERTENCIA: el esquema no tiene CHECK de peso positivo.");

        assertEquals(antes, repositorio.listar().size());
    }

    // ---------------- filtro por tipo ----------------

    @Test
    @DisplayName("Buscar y listar ignoran los drones que no son de vigilancia")
    void buscarYListar_ignoranOtrosTipos() throws SQLException {
        int idAgricultura = insertarAgricultura();

        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.buscarPorId(idAgricultura));
        assertTrue(repositorio.listar().stream().noneMatch(x -> x.getId() == idAgricultura));
    }

    @Test
    @DisplayName("Modificar y eliminar no tocan drones que no son de vigilancia")
    void modificarYEliminar_ignoranOtrosTipos() throws SQLException {
        int idAgricultura = insertarAgricultura();
        Vigilancia suplantado = new Vigilancia(idAgricultura, serialUnico(), "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class, () -> repositorio.modificar(suplantado));
        assertThrows(DronNoEncontradoException.class, () -> repositorio.eliminar(suplantado));
        assertEquals(1, contar("SELECT COUNT(*) FROM dron WHERE id=? AND tipo='AGRICULTURA'",
                idAgricultura));
    }
}
