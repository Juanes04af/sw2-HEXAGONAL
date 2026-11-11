package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

/**
 * Utilidad compartida por los controladores para recargar el selector de
 * drones conservando el dron que estaba elegido.
 *
 * Antes cada controlador repetia este mismo bloque; ahora todos lo llaman.
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class SelectorDrones {

    private SelectorDrones() {
    }

    /**
     * Consulta los drones con el caso de uso, los pone en el ComboBox y
     * vuelve a seleccionar el dron que estaba elegido (comparando por id).
     *
     * @param combo  selector de drones del controlador
     * @param listar caso de uso para listar drones
     * @return la lista consultada, por si el controlador la necesita
     */
    static List<Drone> recargar(ComboBox<Drone> combo, ListarDronesUseCase listar) {
        Drone seleccionado = combo.getValue();
        List<Drone> drones = listar.listar();
        combo.setItems(FXCollections.observableArrayList(drones));
        if (seleccionado != null) {
            drones.stream()
                  .filter(seleccionado::equals)
                  .findFirst()
                  .ifPresent(combo::setValue);
        }
        return drones;
    }
}
