package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Implementacion en memoria del puerto de salida {@link DroneRepository},
 * usada solo en pruebas.
 *
 * <p>
 * Permite probar los casos de uso sin PostgreSQL. Imita las reglas
 * de la base de datos real: asigna ids, rechaza seriales duplicados
 * y lanza {@link DronNoEncontradoException} si el id no existe.
 * Tambien cuenta cuantas veces se llamo cada operacion, para verificar
 * que un dato invalido nunca llegue a la persistencia.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
class RepositorioEnMemoria implements DroneRepository {

    private final Map<Integer, Drone> datos = new LinkedHashMap<>();
    private int siguienteId = 1;

    int llamadasCrear;
    int llamadasModificar;
    int llamadasEliminar;

    @Override
    public Drone crear(Drone drone) {
        llamadasCrear++;
        verificarSerialUnico(drone.getSerial(), -1);
        drone.setId(siguienteId++);
        datos.put(drone.getId(), drone);
        return drone;
    }

    @Override
    public Drone modificar(Drone drone) {
        llamadasModificar++;
        if (!datos.containsKey(drone.getId())) {
            throw new DronNoEncontradoException(drone.getId());
        }
        verificarSerialUnico(drone.getSerial(), drone.getId());
        datos.put(drone.getId(), drone);
        return drone;
    }

    @Override
    public void eliminar(Drone drone) {
        llamadasEliminar++;
        if (datos.remove(drone.getId()) == null) {
            throw new DronNoEncontradoException(drone.getId());
        }
    }

    @Override
    public Drone buscarPorId(int id) {
        Drone drone = datos.get(id);
        if (drone == null) {
            throw new DronNoEncontradoException(id);
        }
        return drone;
    }

    @Override
    public List<Drone> listar() {
        return new ArrayList<>(datos.values());
    }

    private void verificarSerialUnico(String serial, int idPropio) {
        for (Drone d : datos.values()) {
            if (d.getId() != idPropio && d.getSerial().equals(serial)) {
                throw new DronDuplicadoException(serial);
            }
        }
    }
}
