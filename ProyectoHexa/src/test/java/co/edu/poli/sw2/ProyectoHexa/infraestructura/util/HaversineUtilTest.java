package co.edu.poli.sw2.ProyectoHexa.infraestructura.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.strategy.PuntoGeografico;

/**
 * Pruebas de la formula de Haversine con distancias conocidas.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class HaversineUtilTest {

    @Test
    @DisplayName("Un grado de longitud en el ecuador mide unos 111.19 km")
    void unGradoEnElEcuador() {
        assertEquals(111.19, HaversineUtil.calcular(0, 0, 0, 1), 0.01);
    }

    @Test
    @DisplayName("Ruta del enunciado (Bogota): unos 12.51 km")
    void rutaDelEnunciado() {
        double d = HaversineUtil.calcular(4.60971, -74.08175, 4.70159, -74.14690);

        assertEquals(12.51, d, 0.01);
    }

    @Test
    @DisplayName("La distancia de un punto a si mismo es cero")
    void mismoPunto() {
        assertEquals(0.0, HaversineUtil.calcular(4.6, -74.0, 4.6, -74.0), 0.000001);
    }

    @Test
    @DisplayName("La distancia es la misma en ambos sentidos")
    void simetrica() {
        double ida = HaversineUtil.calcular(4.60971, -74.08175, 45.8144, 15.978);
        double vuelta = HaversineUtil.calcular(45.8144, 15.978, 4.60971, -74.08175);

        assertEquals(ida, vuelta, 0.000001);
    }

    @Test
    @DisplayName("Puntos opuestos del planeta: media circunferencia (unos 20015 km)")
    void puntosAntipodas() {
        assertEquals(Math.PI * HaversineUtil.RADIO_TIERRA_KM,
                HaversineUtil.calcular(0, 0, 0, 180), 0.01);
    }

    @Test
    @DisplayName("Como puerto, calcula la distancia entre dos PuntoGeografico")
    void comoPuerto() {
        PuntoGeografico a = new PuntoGeografico("A", 0, 0);
        PuntoGeografico b = new PuntoGeografico("B", 0, 1);

        assertEquals(111.19, new HaversineUtil().distanciaKm(a, b), 0.01);
    }
}
