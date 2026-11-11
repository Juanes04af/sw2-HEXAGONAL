package co.edu.poli.sw2.ProyectoHexa.dominio.clima;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Prueba de concurrencia del sujeto {@link WeatherNotifier}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class WeatherNotifierConcurrenciaTest {

    @Test
    @DisplayName("Notificar mientras otros hilos suscriben y retiran no lanza excepciones")
    void notificarConcurrentemente() throws Exception {
        WeatherNotifier notifier = new WeatherNotifier(Ciudad.BOGOTA);
        Weather clima = new Weather(20, 50, 10, 1010, 20);
        int hilos = 6;

        ExecutorService pool = Executors.newFixedThreadPool(hilos + 1);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        for (int h = 0; h < hilos; h++) {
            int base = h * 1000;
            futuros.add(pool.submit(() -> {
                salida.await();
                for (int i = 1; i <= 300; i++) {
                    Drone d = new Vigilancia(base + i, "S" + i, "F", "M", 1.0, false);
                    notifier.suscribir(d);
                    notifier.desuscribir(d);
                }
                return null;
            }));
        }

        futuros.add(pool.submit(() -> {
            salida.await();
            for (int i = 0; i < 300; i++) {
                notifier.notificar(clima);
            }
            return null;
        }));

        salida.countDown();
        for (Future<?> f : futuros) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertFalse(notifier.tieneObservadores());
    }
}
