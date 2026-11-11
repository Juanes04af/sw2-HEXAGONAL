package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ManageDroneStateUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.DroneState;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.FlyingState;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.LandingState;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.OffState;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.ReadyState;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Adaptador de entrada JavaFX de la pestana Estado.
 *
 * <p>
 * Muestra el automata del patron State resaltando el estado actual del
 * dron elegido, permite pedir cada operacion (las no permitidas se
 * rechazan y se muestra el motivo) y conserva un historial de
 * transiciones. No contiene reglas del ciclo operativo: solo delega en
 * el caso de uso.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlEstado implements Initializable {

    private static final String SIN_DATO = "-";
    private static final String CLASE_ACTIVO = "nodo-activo";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Selector del dron. */
    @FXML private ComboBox<Drone> cmbDrone;
    /** Estado actual del dron elegido. */
    @FXML private Label lblEstadoActual;
    /** Mensajes al usuario. */
    @FXML private Label lblMensaje;
    /** Nodos del automata. */
    @FXML private Label lblNodoApagado;
    @FXML private Label lblNodoEspera;
    @FXML private Label lblNodoVolando;
    @FXML private Label lblNodoAterrizando;

    /** Tabla de drones con su estado. */
    @FXML private TableView<Drone> tablaDrones;
    @FXML private TableColumn<Drone, String> colId;
    @FXML private TableColumn<Drone, String> colSerial;
    @FXML private TableColumn<Drone, String> colEstado;

    /** Historial de transiciones y rechazos, el mas reciente primero. */
    @FXML private ListView<String> listaHistorial;

    private final ManageDroneStateUseCase estados;
    private final ListarDronesUseCase listarDrones;

    /** Nodo grafico de cada estado, para resaltar el actual sin condicionales. */
    private final Map<Class<? extends DroneState>, Label> nodos = new LinkedHashMap<>();

    /**
     * Construye el controlador.
     *
     * @param estados      caso de uso de gestion de estados
     * @param listarDrones caso de uso de listado de drones
     */
    public ControlEstado(ManageDroneStateUseCase estados, ListarDronesUseCase listarDrones) {
        this.estados = estados;
        this.listarDrones = listarDrones;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nodos.put(OffState.class, lblNodoApagado);
        nodos.put(ReadyState.class, lblNodoEspera);
        nodos.put(FlyingState.class, lblNodoVolando);
        nodos.put(LandingState.class, lblNodoAterrizando);

        cmbDrone.setConverter(new ConvertidorDrone());
        cmbDrone.setOnShowing(evento -> cargarDrones());
        cmbDrone.valueProperty().addListener((obs, anterior, nuevo) -> mostrarEstado(nuevo));

        tablaDrones.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                cmbDrone.setValue(nuevo);
            }
        });

        configurarTabla();
        cargarDrones();
        mostrarEstado(null);
    }

    @FXML
    private void encender() {
        ejecutar(estados::encender, "encender");
    }

    @FXML
    private void apagar() {
        ejecutar(estados::apagar, "apagar");
    }

    @FXML
    private void despegar() {
        ejecutar(estados::despegar, "despegar");
    }

    @FXML
    private void iniciarAterrizaje() {
        ejecutar(estados::iniciarAterrizaje, "iniciar aterrizaje");
    }

    @FXML
    private void finalizarAterrizaje() {
        ejecutar(estados::finalizarAterrizaje, "finalizar aterrizaje");
    }

    @FXML
    private void actualizarLista() {
        cargarDrones();
    }

    /**
     * Pide la operacion al caso de uso. Si el estado actual la rechaza,
     * se muestra el motivo y el estado no cambia.
     */
    private void ejecutar(Consumer<Drone> operacion, String nombre) {
        Drone drone = cmbDrone.getValue();
        try {
            DroneState antes = estados.estadoDe(drone);
            operacion.accept(drone);
            DroneState despues = estados.estadoDe(drone);

            registrar(drone.getSerial() + ": " + antes + " -> " + despues);
            actualizarVista(drone);
            mostrarMensaje("Drone " + drone.getSerial() + " ahora esta " + despues + ".", false);

        } catch (DronException e) {
            if (drone != null) {
                registrar(drone.getSerial() + ": RECHAZADO (" + nombre + ")");
            }
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colSerial.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSerial()));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(estados.estadoDe(d.getValue()).toString()));
    }

    /** Recarga drones (combo y tabla) conservando la seleccion. */
    private void cargarDrones() {
        try {
            List<Drone> drones = SelectorDrones.recargar(cmbDrone, listarDrones);
            tablaDrones.setItems(FXCollections.observableArrayList(drones));
        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void actualizarVista(Drone drone) {
        tablaDrones.refresh();
        mostrarEstado(drone);
    }

    /** Refleja el estado del dron en la etiqueta y resalta su nodo del automata. */
    private void mostrarEstado(Drone drone) {
        DroneState estado = drone == null ? null : estados.estadoDe(drone);

        lblEstadoActual.setText(estado == null ? SIN_DATO : estado.toString());

        nodos.forEach((tipo, nodo) -> {
            nodo.getStyleClass().remove(CLASE_ACTIVO);
            if (estado != null && tipo == estado.getClass()) {
                nodo.getStyleClass().add(CLASE_ACTIVO);
            }
        });
    }

    private void registrar(String texto) {
        listaHistorial.getItems().add(0, LocalTime.now().format(HORA) + "  " + texto);
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        MensajeVista.mostrar(lblMensaje, mensaje, error);
    }

}
