package co.edu.poli.sw2.ProyectoHexa.infraestructura.ui;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

import javafx.util.StringConverter;

/**
 * Muestra cada dron en un selector como "#id - serial".
 * Compartido por los controladores para no repetir la misma clase.
 *
 * @author Alejandra Cano y Juan Rosero
 */
final class ConvertidorDrone extends StringConverter<Drone> {

    /** {@inheritDoc} */
    @Override
    public String toString(Drone drone) {
        return drone == null ? "" : "#" + drone.getId() + " - " + drone.getSerial();
    }

    /** No se usa: el selector no es editable. */
    @Override
    public Drone fromString(String texto) {
        return null;
    }
}
