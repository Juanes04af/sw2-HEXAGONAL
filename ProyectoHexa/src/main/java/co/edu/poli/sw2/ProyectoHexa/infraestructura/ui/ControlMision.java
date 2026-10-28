package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ProgramarMisionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneCommand;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.TipoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

/**
 * Adaptador de entrada JavaFX de la pestana Mision.
 *
 * <p>
 * Permite programar una secuencia de tareas (comandos) para uno o
 * varios drones, quitarlas o limpiarlas, y ejecutar la mision. Los
 * resultados se muestran en el orden en que se programaron.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ControlMision implements Initializable {

    /** Selector del dron. */
    @FXML
    private ComboBox<Drone> cmbDrone;

    /** Selector de la tarea. */
    @FXML
    private ComboBox<TipoTarea> cmbTarea;

    /** Lista de tareas programadas. */
    @FXML
    private ListView<String> listaTareas;

    /** Etiqueta de mensajes al usuario. */
    @FXML
    private Label lblMensaje;

    /** Tabla de resultados de la ultima mision. */
    @FXML
    private TableView<ResultadoTarea> tablaResultados;

    /** Columna del orden. */
    @FXML
    private TableColumn<ResultadoTarea, String> colOrden;

    /** Columna de la tarea. */
    @FXML
    private TableColumn<ResultadoTarea, String> colTarea;

    /** Columna del dron. */
    @FXML
    private TableColumn<ResultadoTarea, String> colDrone;

    /** Columna del estado. */
    @FXML
    private TableColumn<ResultadoTarea, String> colEstado;

    /** Columna del resultado. */
    @FXML
    private TableColumn<ResultadoTarea, String> colResultado;

    /** Caso de uso de programacion de la mision. */
    private final ProgramarMisionUseCase programarMision;

    /** Caso de uso de listado de drones. */
    private final ListarDronesUseCase listarDrones;

    /**
     * Construye el controlador con los casos de uso que necesita.
     *
     * @param programarMision caso de uso de programacion de la mision
     * @param listarDrones    caso de uso de listado de drones
     */
    public ControlMision(ProgramarMisionUseCase programarMision,
                         ListarDronesUseCase listarDrones) {
        this.programarMision = programarMision;
        this.listarDrones = listarDrones;
    }

    /**
     * Configura los selectores, la lista y la tabla despues de cargar
     * el FXML.
     *
     * @param location  ubicacion del recurso
     * @param resources recursos de internacionalizacion
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbDrone.setConverter(new ConvertidorDrone());
        cmbDrone.setOnShowing(evento -> cargarDrones());
        cmbTarea.setItems(FXCollections.observableArrayList(TipoTarea.values()));

        colOrden.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getOrden())));
        colTarea.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTarea()));
        colDrone.setCellValueFactory(d ->
                new SimpleStringProperty(nombreDrone(d.getValue().getDrone())));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isExitosa() ? "OK" : "Error"));
        colResultado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMensaje()));

        cargarDrones();
        refrescarTareas();
    }

    /**
     * Agrega la tarea elegida al final de la mision.
     */
    @FXML
    private void agregarTarea() {
        try {
            programarMision.programar(cmbTarea.getValue(), cmbDrone.getValue());
            refrescarTareas();
            mostrarMensaje("Tarea '" + cmbTarea.getValue() + "' agregada.", false);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Quita la tarea seleccionada en la lista.
     */
    @FXML
    private void quitarTarea() {
        try {
            programarMision.quitar(listaTareas.getSelectionModel().getSelectedIndex());
            refrescarTareas();
            mostrarMensaje("Tarea quitada.", false);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Borra todas las tareas programadas.
     */
    @FXML
    private void limpiarTareas() {
        programarMision.limpiar();
        refrescarTareas();
        mostrarMensaje("Mision vaciada.", false);
    }

    /**
     * Ejecuta las tareas en orden y muestra los resultados.
     */
    @FXML
    private void ejecutarMision() {
        try {
            List<ResultadoTarea> resultados = programarMision.ejecutarMision();
            tablaResultados.setItems(FXCollections.observableArrayList(resultados));
            refrescarTareas();

            long errores = resultados.stream().filter(r -> !r.isExitosa()).count();
            mostrarMensaje("Mision ejecutada: " + resultados.size() + " tarea(s)"
                    + (errores > 0 ? ", " + errores + " con error." : " sin errores."),
                    errores > 0);

        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Muestra las tareas programadas numeradas en orden.
     */
    private void refrescarTareas() {
        List<DroneCommand> tareas = programarMision.tareasProgramadas();
        listaTareas.getItems().clear();
        for (int i = 0; i < tareas.size(); i++) {
            DroneCommand tarea = tareas.get(i);
            listaTareas.getItems().add((i + 1) + ". " + tarea.getNombre()
                    + "  -  " + nombreDrone(tarea.getDrone()));
        }
    }

    /**
     * Recarga la lista de drones registrados, conservando la seleccion.
     */
    private void cargarDrones() {
        try {
            Drone seleccionado = cmbDrone.getValue();
            List<Drone> drones = listarDrones.listar();
            cmbDrone.setItems(FXCollections.observableArrayList(drones));

            if (seleccionado != null) {
                drones.stream()
                      .filter(seleccionado::equals)
                      .findFirst()
                      .ifPresent(cmbDrone::setValue);
            }
        } catch (DronException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    /**
     * Describe un dron como "#id - serial".
     *
     * @param drone dron
     * @return texto visible
     */
    private static String nombreDrone(Drone drone) {
        return drone == null ? "" : "#" + drone.getId() + " - " + drone.getSerial();
    }

    /**
     * Muestra un mensaje informativo o de error.
     *
     * @param mensaje texto a mostrar
     * @param error   {@code true} si es un error
     */
    private void mostrarMensaje(String mensaje, boolean error) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
        lblMensaje.getStyleClass().add(error ? "mensaje-error" : "mensaje-ok");
    }

    /**
     * Muestra cada dron en el selector como "#id - serial".
     */
    private static final class ConvertidorDrone extends StringConverter<Drone> {

        /** {@inheritDoc} */
        @Override
        public String toString(Drone drone) {
            return nombreDrone(drone);
        }

        /** No se usa: el selector no es editable. */
        @Override
        public Drone fromString(String texto) {
            return null;
        }
    }
}
