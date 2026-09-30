package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.BuscarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.CrearDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.EliminarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ModificarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Adaptador de entrada JavaFX para la gestion de drones.
 *
 * <p>
 * El controlador recibe los casos de uso de la aplicacion mediante
 * puertos de entrada y no conoce directamente la implementacion
 * de persistencia ni la base de datos.
 * </p>
 *
 * <p>
 * Actualmente la interfaz permite trabajar exclusivamente con
 * drones de tipo {@link Vigilancia}.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlDrone implements Initializable {

    /**
     * Campo utilizado para ingresar el serial del dron.
     */
    @FXML
    private TextField txtSerial;

    /**
     * Campo utilizado para ingresar el fabricante.
     */
    @FXML
    private TextField txtFabricante;

    /**
     * Campo utilizado para ingresar el modelo.
     */
    @FXML
    private TextField txtModelo;

    /**
     * Campo utilizado para ingresar el peso.
     */
    @FXML
    private TextField txtPeso;

    /**
     * Casilla que indica si el dron posee deteccion termica.
     */
    @FXML
    private CheckBox chkDeteccionTermica;

    /**
     * Campo utilizado para buscar un dron por identificador.
     */
    @FXML
    private TextField txtBuscarId;

    /**
     * Etiqueta utilizada para presentar mensajes al usuario.
     */
    @FXML
    private Label lblMensaje;

    /**
     * Tabla principal de drones.
     */
    @FXML
    private TableView<Drone> tablaDrones;

    /**
     * Columna correspondiente al identificador.
     */
    @FXML
    private TableColumn<Drone, Integer> colId;

    /**
     * Columna correspondiente al tipo.
     */
    @FXML
    private TableColumn<Drone, String> colTipo;

    /**
     * Columna correspondiente al serial.
     */
    @FXML
    private TableColumn<Drone, String> colSerial;

    /**
     * Columna correspondiente al fabricante.
     */
    @FXML
    private TableColumn<Drone, String> colFabricante;

    /**
     * Columna correspondiente al modelo.
     */
    @FXML
    private TableColumn<Drone, String> colModelo;

    /**
     * Columna correspondiente al peso.
     */
    @FXML
    private TableColumn<Drone, Double> colPeso;

    /**
     * Columna correspondiente a la deteccion termica.
     */
    @FXML
    private TableColumn<Drone, String> colDeteccionTermica;

    /**
     * Caso de uso para crear drones.
     */
    private final CrearDroneUseCase crearDroneUseCase;

    /**
     * Caso de uso para modificar drones.
     */
    private final ModificarDroneUseCase modificarDroneUseCase;

    /**
     * Caso de uso para eliminar drones.
     */
    private final EliminarDroneUseCase eliminarDroneUseCase;

    /**
     * Caso de uso para buscar drones.
     */
    private final BuscarDroneUseCase buscarDroneUseCase;

    /**
     * Caso de uso para listar drones.
     */
    private final ListarDronesUseCase listarDronesUseCase;

    /**
     * Dron seleccionado actualmente en la tabla.
     */
    private Drone droneSeleccionado;

    /**
     * Construye el adaptador de entrada con todos los casos de uso
     * necesarios para gestionar drones.
     *
     * @param crearDroneUseCase caso de uso de creacion
     * @param modificarDroneUseCase caso de uso de modificacion
     * @param eliminarDroneUseCase caso de uso de eliminacion
     * @param buscarDroneUseCase caso de uso de busqueda
     * @param listarDronesUseCase caso de uso de listado
     */
    public ControlDrone(
            CrearDroneUseCase crearDroneUseCase,
            ModificarDroneUseCase modificarDroneUseCase,
            EliminarDroneUseCase eliminarDroneUseCase,
            BuscarDroneUseCase buscarDroneUseCase,
            ListarDronesUseCase listarDronesUseCase) {

        this.crearDroneUseCase = crearDroneUseCase;
        this.modificarDroneUseCase = modificarDroneUseCase;
        this.eliminarDroneUseCase = eliminarDroneUseCase;
        this.buscarDroneUseCase = buscarDroneUseCase;
        this.listarDronesUseCase = listarDronesUseCase;
    }

    /**
     * Inicializa los componentes JavaFX despues de cargar el FXML.
     *
     * @param location ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        configurarTabla();
        listarDrones();

        tablaDrones.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, nuevo) -> {

                    if (nuevo != null) {
                        droneSeleccionado = nuevo;
                        cargarFormulario(nuevo);
                    }
                });
    }

    /**
     * Configura las columnas de la tabla.
     */
    private void configurarTabla() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo"));

        colSerial.setCellValueFactory(
                new PropertyValueFactory<>("serial"));

        colFabricante.setCellValueFactory(
                new PropertyValueFactory<>("fabricante"));

        colModelo.setCellValueFactory(
                new PropertyValueFactory<>("modelo"));

        colPeso.setCellValueFactory(
                new PropertyValueFactory<>("peso"));

        colDeteccionTermica.setCellValueFactory(datos -> {

            Drone drone = datos.getValue();

            if (drone instanceof Vigilancia) {

                Vigilancia vigilancia = (Vigilancia) drone;

                return new SimpleStringProperty(
                        vigilancia.isDeteccionTermica()
                                ? "Si"
                                : "No");
            }

            return new SimpleStringProperty("");
        });
    }

    /**
     * Ejecuta el caso de uso de creacion utilizando los
     * datos capturados en el formulario.
     */
    @FXML
    private void crearDrone() {

        try {

            crearDroneUseCase.crear(
                    txtSerial.getText(),
                    txtFabricante.getText(),
                    txtModelo.getText(),
                    txtPeso.getText(),
                    chkDeteccionTermica.isSelected());

            listarDrones();
            limpiarFormulario();

            mostrarMensaje(
                    "Drone de vigilancia creado correctamente.",
                    false);

        } catch (DronException e) {

            mostrarMensaje(e.getMessage(), true);

        } catch (Exception e) {

            mostrarErrorInesperado(e);
        }
    }

    /**
     * Ejecuta el caso de uso de modificacion para el dron
     * actualmente seleccionado.
     */
    @FXML
    private void modificarDrone() {

        try {

            if (droneSeleccionado == null) {

                mostrarMensaje(
                        "Seleccione un drone para modificar.",
                        true);

                return;
            }

            modificarDroneUseCase.modificar(
                    droneSeleccionado,
                    txtSerial.getText(),
                    txtFabricante.getText(),
                    txtModelo.getText(),
                    txtPeso.getText(),
                    chkDeteccionTermica.isSelected());

            listarDrones();
            limpiarFormulario();

            mostrarMensaje(
                    "Drone modificado correctamente.",
                    false);

        } catch (DronException e) {

            mostrarMensaje(e.getMessage(), true);

        } catch (Exception e) {

            mostrarErrorInesperado(e);
        }
    }

    /**
     * Ejecuta el caso de uso de eliminacion para el dron
     * seleccionado.
     */
    @FXML
    private void eliminarDrone() {

        try {

            if (droneSeleccionado == null) {

                mostrarMensaje(
                        "Seleccione un drone para eliminar.",
                        true);

                return;
            }

            eliminarDroneUseCase.eliminar(
                    droneSeleccionado);

            listarDrones();
            limpiarFormulario();

            mostrarMensaje(
                    "Drone eliminado correctamente.",
                    false);

        } catch (DronException e) {

            mostrarMensaje(e.getMessage(), true);

        } catch (Exception e) {

            mostrarErrorInesperado(e);
        }
    }

    /**
     * Busca un dron utilizando el identificador escrito
     * por el usuario.
     */
    @FXML
    private void buscarDrone() {

        try {

            if (txtBuscarId.getText() == null
                    || txtBuscarId.getText().isBlank()) {

                mostrarMensaje(
                        "Ingrese el ID que desea buscar.",
                        true);

                return;
            }

            int id;

            try {

                id = Integer.parseInt(
                        txtBuscarId.getText().trim());

            } catch (NumberFormatException e) {

                mostrarMensaje(
                        "El ID debe ser un numero entero.",
                        true);

                return;
            }

            Drone drone =
                    buscarDroneUseCase.buscarPorId(id);

            tablaDrones.setItems(
                    FXCollections.observableArrayList(drone));

            tablaDrones.getSelectionModel()
                    .select(drone);

            droneSeleccionado = drone;

            cargarFormulario(drone);

            mostrarMensaje(
                    "Drone encontrado.",
                    false);

        } catch (DronException e) {

            mostrarMensaje(e.getMessage(), true);

        } catch (Exception e) {

            mostrarErrorInesperado(e);
        }
    }

    /**
     * Obtiene nuevamente todos los drones registrados.
     */
    @FXML
    private void listarDrones() {

        try {

            tablaDrones.setItems(
                    FXCollections.observableArrayList(
                            listarDronesUseCase.listar()));

        } catch (DronException e) {

            mostrarMensaje(e.getMessage(), true);

        } catch (Exception e) {

            mostrarErrorInesperado(e);
        }
    }

    /**
     * Limpia los campos utilizados para crear o modificar drones.
     */
    @FXML
    private void limpiarFormulario() {

        txtSerial.clear();
        txtFabricante.clear();
        txtModelo.clear();
        txtPeso.clear();

        chkDeteccionTermica.setSelected(false);

        tablaDrones.getSelectionModel()
                .clearSelection();

        droneSeleccionado = null;
    }

    /**
     * Carga en el formulario los datos de un dron seleccionado.
     *
     * @param drone dron que sera mostrado
     */
    private void cargarFormulario(Drone drone) {

        txtSerial.setText(
                drone.getSerial());

        txtFabricante.setText(
                drone.getFabricante());

        txtModelo.setText(
                drone.getModelo());

        txtPeso.setText(
                String.valueOf(drone.getPeso()));

        if (drone instanceof Vigilancia) {

            Vigilancia vigilancia =
                    (Vigilancia) drone;

            chkDeteccionTermica.setSelected(
                    vigilancia.isDeteccionTermica());
        }
    }

    /**
     * Presenta un mensaje informativo o de error dentro
     * de la interfaz.
     *
     * @param mensaje contenido que se mostrara
     * @param error indica si corresponde a un error
     */
    private void mostrarMensaje(
            String mensaje,
            boolean error) {

        lblMensaje.setText(mensaje);

        lblMensaje.getStyleClass()
                .removeAll(
                        "mensaje-ok",
                        "mensaje-error");

        lblMensaje.getStyleClass()
                .add(
                        error
                                ? "mensaje-error"
                                : "mensaje-ok");
    }

    /**
     * Presenta mediante JavaFX un error inesperado.
     *
     * @param excepcion excepcion producida
     */
    private void mostrarErrorInesperado(
            Exception excepcion) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle(
                "Error");

        alert.setHeaderText(
                "Ocurrio un error inesperado");

        alert.setContentText(
                excepcion.getMessage());

        alert.showAndWait();
    }
}