package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas estructurales del patron <b>Singleton</b> en {@link ConexionBD}.
 * No abren conexiones, por lo que no requieren PostgreSQL.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ConexionBDTest {

    /**
     * Reinicia la instancia por reflexion para que cada prueba
     * parta de "aun no existe instancia".
     */
    @BeforeEach
    void reiniciarSingleton() throws Exception {
        Field campo = ConexionBD.class.getDeclaredField("instancia");
        campo.setAccessible(true);
        campo.set(null, null);
    }

    @Test
    @DisplayName("getInstancia devuelve siempre el mismo objeto")
    void getInstancia_devuelveLaMismaInstancia() {
        ConexionBD a = ConexionBD.getInstancia();
        ConexionBD b = ConexionBD.getInstancia();

        assertNotNull(a);
        assertSame(a, b);
    }

    @Test
    @DisplayName("Con hilos simultaneos solo se crea una instancia")
    void getInstancia_esSeguraConHilos() throws Exception {
        int hilos = 20;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        Set<ConexionBD> vistas = ConcurrentHashMap.newKeySet();
        List<Future<Void>> futuros = new ArrayList<>();

        for (int i = 0; i < hilos; i++) {
            Callable<Void> tarea = () -> {
                salida.await();
                vistas.add(ConexionBD.getInstancia());
                return null;
            };
            futuros.add(pool.submit(tarea));
        }

        salida.countDown();
        for (Future<Void> f : futuros) {
            f.get(5, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertEquals(1, vistas.size(), "Se crearon instancias distintas bajo concurrencia");
    }

    @Test
    @DisplayName("El constructor es privado")
    void constructor_esPrivado() {
        Constructor<?>[] constructores = ConexionBD.class.getDeclaredConstructors();

        assertEquals(1, constructores.length);
        assertTrue(Modifier.isPrivate(constructores[0].getModifiers()));
    }

    @Test
    @DisplayName("La clase es final y no se puede extender")
    void clase_esFinal() {
        assertTrue(Modifier.isFinal(ConexionBD.class.getModifiers()));
    }

    @Test
    @DisplayName("El campo de instancia es static y volatile")
    void campoInstancia_esStaticVolatile() throws Exception {
        Field campo = ConexionBD.class.getDeclaredField("instancia");

        assertTrue(Modifier.isStatic(campo.getModifiers()));
        assertTrue(Modifier.isVolatile(campo.getModifiers()));
    }

    @Test
    @DisplayName("toString no expone credenciales")
    void toString_noExponeCredenciales() {
        String texto = ConexionBD.getInstancia().toString();

        assertEquals("ConexionBD[configurada]", texto);

        String password = CargadorEnv.obtener("DB_PASSWORD");
        if (password != null && !password.isBlank()) {
            assertFalse(texto.contains(password));
        }
    }
}
