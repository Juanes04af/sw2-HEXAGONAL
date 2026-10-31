# Gestión de Drones — Arquitectura Hexagonal

Aplicación de escritorio en **JavaFX** para gestionar drones de **vigilancia** sobre **PostgreSQL**, consultar el **clima** de varias ciudades mediante las APIs de **Open-Meteo**, **evaluar si ese clima permite volar**, **estimar el consumo de batería** de una ruta según el modo de vuelo y **programar misiones** como una secuencia de tareas, construida con **arquitectura hexagonal (puertos y adaptadores)**.

Es la evolución del primer proyecto (`Proyecto-1` / `sw2`). Conserva la misma base de datos, pero reorganiza el código para que la lógica de negocio no dependa de la interfaz gráfica, de la tecnología de persistencia ni de los servicios externos.

**Autores:** Alejandra Cano y Juan Rosero
**Asignatura:** Ingeniería de Software 2 — Politécnico Grancolombiano

---

## 1. Alcance

El proyecto se limita deliberadamente a lo esencial:

- **5 casos de uso del CRUD:** crear, modificar, eliminar, buscar por ID y listar todos.
- **4 casos de uso del clima:** suscribir/desuscribir drones a una ciudad, consultar el clima de una ciudad, notificar a los drones suscritos y **validar si el clima permite operar drones**.
- **1 caso de uso de consumo:** calcular la batería que un dron necesita para recorrer la ruta cargada, según la **estrategia de consumo** elegida.
- **1 caso de uso de misión:** programar una secuencia de tareas (tomar fotografía, escanear zona, medir temperatura), quitarlas o limpiarlas, y **ejecutarlas en orden**.
- **Un único tipo de dron:** `Vigilancia` (con su atributo `deteccionTermica`). No es posible cambiar a otro tipo; los drones de agricultura que existan en la base de datos simplemente no se muestran.
- **Cinco patrones de diseño:**
  - **Singleton**, aplicado a la conexión con la base de datos (`ConexionBD`).
  - **Observer**, aplicado a las notificaciones del clima (`WeatherSubject`, `WeatherNotifier`, `WeatherObserver` y `Drone`).
  - **Chain of Responsibility**, aplicado a la validación de la viabilidad de vuelo (`WeatherHandler` y sus cinco eslabones).
  - **Strategy**, aplicado al cálculo del consumo de batería (`ConsumoStrategy` y sus tres modos de vuelo).
  - **Command**, aplicado a la programación de tareas de una misión (`DroneCommand`, `ProgramadorTareas` y sus tres comandos).

> La explicación de la **arquitectura hexagonal** (capas, puertos, adaptadores y casos de uso) está en la **Wiki** del repositorio.

La interfaz tiene **cuatro pestañas**:

| Pestaña | Qué permite |
|---|---|
| **Drones** | CRUD de drones de vigilancia: formulario, búsqueda por ID, listado y tabla de resultados |
| **Clima** | Suscribir drones a una ciudad, consultar el clima, ver qué clima recibió cada dron y si **puede volar** con ese clima (columna **Puede volar** y motivo del rechazo) |
| **Consumo** | Ver la ruta del archivo JSON y su distancia, elegir un dron y una estrategia, y calcular la **batería requerida**; cada cálculo queda en un historial |
| **Mision** | Programar tareas para uno o varios drones, quitarlas o limpiarlas, ejecutar la misión y ver el resultado de cada tarea en orden |

---

## 2. Patrón Singleton: `ConexionBD`

`ConexionBD` garantiza una única instancia encargada de la configuración de conexión, usando *double-checked locking*:

- La clase es `final`, para que no se pueda extender.
- El constructor es `private`, así que nadie puede hacer `new ConexionBD()`.
- El campo `instancia` es `static volatile`, lo que lo hace seguro entre hilos.
- `getInstancia()` es el único punto de acceso.

Cada llamada a `obtenerConexion()` abre una conexión JDBC nueva, que quien la usa debe cerrar (los repositorios lo hacen con `try-with-resources` o en un `finally`).

### Orden de lectura de credenciales

1. Variables de entorno del sistema operativo (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
2. Archivo `.env` en la raíz del proyecto, leído por `CargadorEnv`.
3. Archivo `db.properties` en el classpath (opcional).

Si ninguna fuente aporta la URL, se usa `jdbc:postgresql://localhost:5432/dronesdb`.

> Si en Windows existe una variable de entorno `DB_USER` o `DB_PASSWORD`, esta tiene prioridad sobre el `.env`.

---

## 3. Patrón Observer: clima por ciudad

Cada dron puede **suscribirse al clima de una ciudad**. Cuando se consulta el clima de esa ciudad, todos los drones suscritos reciben automáticamente la misma lectura y la guardan como su clima actual.

### Participantes

| Rol en el patrón | Clase / interfaz | Capa |
|---|---|---|
| Sujeto (interfaz) | `WeatherSubject` — `suscribir`, `desuscribir`, `notificar` | `dominio/clima` |
| Sujeto concreto | `WeatherNotifier` — uno por cada `Ciudad` | `dominio/clima` |
| Observador (interfaz) | `WeatherObserver` — `actualizarClima(Weather)` | `dominio/clima` |
| Observador concreto | `Drone` (y por herencia `Vigilancia`) | `dominio/modelo` |
| Mensaje | `Weather` — inmutable, con los cinco aspectos del clima | `dominio/clima` |

`Weather` incluye los cinco aspectos meteorológicos que exige el sistema: **temperatura** (°C), **humedad** relativa (%), **velocidad del viento** (km/h), **presión** atmosférica (hPa) y **visibilidad** (km).

### Ciudades disponibles

Las ciudades están definidas en el enum `Ciudad`: **Bogotá (Colombia), Zagreb (Croacia), Madrid (España), Tokio (Japón) y Buenos Aires (Argentina)**. Cada una guarda el nombre con el que se busca en la API de geocodificación y el código ISO de su país, para no confundirla con otra ciudad del mismo nombre.

### Reglas de negocio

- Un dron pertenece **como máximo a una ciudad**; una ciudad puede tener **muchos drones**.
- Suscribir un dron a otra ciudad lo **traslada** (se retira de la anterior). Suscribirlo dos veces a la misma ciudad es rechazado.
- Las suscripciones **viven solo en memoria** mientras la aplicación está abierta. No se guardan en la base de datos, y el último clima recibido por el dron tampoco (`climaActual` es `transient`).
- Los drones se comparan **por id y tipo** (`Drone.equals`/`hashCode`), porque cada consulta a la base de datos crea objetos nuevos. Un dron sin guardar (id 0) solo es igual a sí mismo.
- Al **modificar** un dron suscrito, `sincronizar` reemplaza la copia suscrita conservando su ciudad y su último clima.
- Al **eliminar** un dron, `retirar` lo quita de cualquier suscripción.

### Flujo de una notificación

1. El usuario pulsa **Consultar clima** en la pestaña Clima.
2. `WeatherNotificationService.notificarSuscritos()` recorre los notificadores y **solo consulta las ciudades que tienen al menos un dron suscrito**.
3. Por cada ciudad se obtiene el `Weather` a través del caso de uso `GetWeatherByCityUseCase` y el puerto de salida `WeatherProvider`.
4. El `WeatherNotifier` de la ciudad llama a `actualizarClima` en cada dron suscrito.
5. Si una ciudad falla (sin internet, API caída, ciudad no encontrada), el error se registra en el `ResultadoNotificacion` y **se continúa con las demás ciudades**.
6. La tabla se refresca y muestra el clima que recibió cada dron.
7. Cada clima notificado se **valida con la cadena de responsabilidad** (sección 4): la tabla agrega *Puede volar* y el motivo, y el mensaje de la izquierda indica por ciudad si el clima es apto.

### Detalles de implementación

- `WeatherNotifier` usa una `CopyOnWriteArrayList` sin duplicados, de modo que la notificación puede correr en un hilo de fondo sin fallar si la interfaz suscribe o retira un dron al mismo tiempo.
- Los métodos de `WeatherNotificationService` que tocan las suscripciones son `synchronized`.
- La consulta se ejecuta en un `Task` de JavaFX con un hilo *daemon*, para **no congelar la ventana** mientras se espera la respuesta de internet; mientras tanto se muestra un indicador de carga y el botón queda deshabilitado.

---

## 4. Patrón Chain of Responsibility: viabilidad de vuelo

Cuando un dron recibe el clima de su ciudad, ese clima se **valida con una cadena de responsabilidad** para saber si permite operar drones. Cada eslabón (*handler*) valida **una sola condición**: si se cumple, pasa la solicitud al siguiente; si falla, **la cadena se detiene** y se devuelve el motivo del rechazo.

```
TemperatureHandler → HumidityHandler → WindHandler → PressureHandler → VisibilityHandler
```

### Reglas de validación

| Orden | Eslabón | Condición para aprobar |
|---|---|---|
| 1 | `TemperatureHandler` | Temperatura entre **0 °C y 40 °C** |
| 2 | `HumidityHandler` | Humedad relativa **≤ 85 %** |
| 3 | `WindHandler` | Velocidad del viento **≤ 40 km/h** |
| 4 | `PressureHandler` | Presión atmosférica entre **950 y 1050 hPa** |
| 5 | `VisibilityHandler` | Visibilidad **≥ 5 km** |

Los límites son **inclusivos**: por ejemplo, 40 °C, 85 %, 40 km/h, 950 hPa y 5 km aprueban.

### Resultado

- Si los cinco eslabones aprueban: **`CLIMA APTO PARA OPERACIÓN DE DRONES`**.
- Si alguno falla: **`CLIMA NO APTO PARA OPERACIÓN DE DRONES — <regla incumplida>`**, por ejemplo: *Velocidad del viento superior al máximo permitido (40 km/h): 55.0 km/h.*
- El motivo del rechazo **se registra en el log** (`java.util.logging`, nivel `WARNING`) desde `ValidarClimaServicio`.
- Nada se guarda en la base de datos.

### Participantes

| Rol en el patrón | Clase / interfaz | Capa |
|---|---|---|
| Manejador base (abstracto) | `WeatherHandler` — `setSiguiente`, `validar`, `validarCondicion` | `dominio/chain` |
| Manejadores concretos | `TemperatureHandler`, `HumidityHandler`, `WindHandler`, `PressureHandler`, `VisibilityHandler` | `dominio/chain` |
| Armado de la cadena | `CadenaValidacionClima` — el **orden** es una regla de negocio, por eso vive en el dominio | `dominio/chain` |
| Resultado | `EvaluacionClima` — inmutable: apto o no apto con su motivo | `dominio/chain` |
| Solicitud | `Weather` — el mismo mensaje que usa el Observer | `dominio/clima` |
| Cliente / caso de uso | `ValidarClimaUseCase` (puerto de entrada) y `ValidarClimaServicio` | `aplicacion` |
| Adaptador de entrada | `ControlClima` — muestra el resultado en la tabla | `infraestructura/ui` |

Las reglas y la cadena están en el **dominio**; el caso de uso en `aplicacion`; la interfaz y la API externa quedan **fuera del dominio**, como exige la arquitectura hexagonal.

### Detalles de implementación

- Si fallan varias reglas a la vez, **solo se informa la primera** de la cadena (por ejemplo, con 50 °C y 99 % de humedad se informa la temperatura).
- `WeatherHandler.validar` devuelve el motivo del primer rechazo o `null` si todos aprueban; `EvaluacionClima` envuelve ese resultado con los mensajes finales.
- Los handlers no validan datos ausentes: `OpenWeatherAdapter` ya garantiza que los cinco datos existen (si no, lanza `ClimaException`).
- `ControlClima` guarda la evaluación **por instancia de `Weather`**, para no validar ni escribir en el log cada vez que la tabla se repinta. Cuando llega un clima nuevo, se evalúa de nuevo.
- Un `Weather` nulo es rechazado con `DronValidacionException`.

### Relación con el Observer

El Observer **entrega** el `Weather` a cada dron suscrito; la cadena **evalúa** ese mismo `Weather`. Son independientes: el notificador no conoce la cadena, y la validación se hace al mostrar el resultado en la pestaña Clima.

---

## 5. Patrón Strategy: consumo de batería

Cada **modo de vuelo** consume batería de forma distinta. En lugar de poner un `if/else` por modo dentro del cálculo, cada modo es una **estrategia** intercambiable: quien calcula el consumo solo conoce la interfaz `ConsumoStrategy`, de modo que se puede **cambiar de estrategia en tiempo de ejecución** y **agregar una nueva** sin modificar el servicio.

### Estrategias disponibles

| Estrategia | Modo | Fórmula |
|---|---|---|
| `AhorroStrategy` | Modo Ahorro: minimiza el consumo | distancia (km) × **0.5** |
| `NormalStrategy` | Modo Normal: consumo estándar | distancia (km) × **1.0** |
| `AltoRendimientoStrategy` | Modo Alto Rendimiento: más velocidad, más batería | distancia (km) × **1.5** |

Para la misma distancia siempre se cumple: **Ahorro < Normal < Alto Rendimiento**.

### Ruta y distancia

La ruta (origen y destino) se lee de un **archivo JSON incluido en el programa**, `src/main/resources/co/edu/poli/sw2/datos/ruta.json`:

```json
{
  "origen":  { "nombre": "Centro de Bogota",     "latitud": 4.60971, "longitud": -74.08175 },
  "destino": { "nombre": "Aeropuerto El Dorado", "latitud": 4.70159, "longitud": -74.14690 }
}
```

La distancia entre ambos puntos se calcula con la fórmula de **Haversine**, que mide el arco más corto sobre una esfera del tamaño de la Tierra (radio medio de 6371 km):

```
a = sin²(Δlat / 2) + cos(lat1) · cos(lat2) · sin²(Δlon / 2)
d = 2 · R · asin(√a)
```

Para la ruta del archivo la distancia es de unos **12.51 km**, así que las tres estrategias dan:

| Estrategia | Cálculo | Batería requerida |
|---|---|---|
| Modo Ahorro | 12.51 km × 0.5 | ≈ 6.26 unidades |
| Modo Normal | 12.51 km × 1.0 | ≈ 12.51 unidades |
| Modo Alto Rendimiento | 12.51 km × 1.5 | ≈ 18.77 unidades |

### Participantes

| Rol en el patrón | Clase / interfaz | Capa |
|---|---|---|
| Estrategia (interfaz) | `ConsumoStrategy` — `calcularConsumo`, `getNombre`, `getFactor` | `dominio/strategy` |
| Estrategias concretas | `AhorroStrategy`, `NormalStrategy`, `AltoRendimientoStrategy` | `dominio/strategy` |
| Contexto | `CalcularConsumoServicio` — guarda la estrategia elegida y delega en ella | `aplicacion/servicio` |
| Caso de uso (puerto de entrada) | `CalcularConsumoUseCase` — `obtenerRuta`, `obtenerDistanciaKm`, `estrategiasDisponibles`, `setEstrategia`, `calcular` | `aplicacion/puerto/entrada` |
| Datos de la ruta | `Ruta` y `PuntoGeografico` — inmutables y con validación | `dominio/strategy` |
| Resultado | `ResultadoConsumo` — dron, ruta, distancia, estrategia y consumo | `aplicacion/puerto/entrada` |
| Puerto de salida (ruta) | `RutaLoader` | `aplicacion/puerto/salida` |
| Puerto de salida (distancia) | `CalculadorDistancia` | `aplicacion/puerto/salida` |
| Adaptador de salida (ruta) | `JsonRutaLoader` — lee `ruta.json` | `infraestructura/loader` |
| Adaptador de salida (distancia) | `HaversineUtil` — implementa `CalculadorDistancia` | `infraestructura/util` |
| Adaptador de entrada | `ControlConsumo` y `consumo.fxml` | `infraestructura/ui` |

### Reglas de negocio

- Para calcular hay que **seleccionar un dron y una estrategia**; si falta alguno, `CalcularConsumoServicio` lanza `DronValidacionException` ("Seleccione un drone." / "Seleccione una estrategia de consumo.").
- Las tres estrategias **rechazan una distancia negativa o `NaN`** con `IllegalArgumentException`.
- `PuntoGeografico` exige **nombre**, **latitud entre -90 y 90** y **longitud entre -180 y 180**; si no, lanza `RutaException`. `Ruta` exige origen y destino.
- Las estrategias disponibles son tres y se ofrecen **en orden**: ahorro, normal y alto rendimiento.

### Detalles de implementación

- El contexto **no sabe qué modo está usando**: solo invoca `estrategia.calcularConsumo(distanciaKm)`. Es la **delegación** que define al patrón.
- La ruta se carga y la distancia se calcula **una sola vez**, la primera vez que se necesitan; después se reutilizan (los métodos que tocan ese estado son `synchronized`).
- `ControlConsumo` entrega la estrategia elegida con `setEstrategia` justo antes de calcular y agrega cada resultado **al inicio del historial** (el más reciente primero) para poder comparar los tres modos.
- `JsonRutaLoader` usa `LectorJson` (ahora en `infraestructura/util`, compartido con los adaptadores de Open-Meteo), que se amplió con `texto(...)` para leer el nombre de cada punto con las secuencias de escape básicas.
- Si el archivo falta, está vacío, mal formado o trae coordenadas fuera de rango, se lanza `RutaException` (hereda de `DronException`), el mensaje se muestra en pantalla y el botón **Calcular** queda deshabilitado.
- Nada se guarda en la base de datos; el historial vive solo mientras la aplicación está abierta.

---

## 6. Patrón Command: misión de tareas

Una misión es una **secuencia de tareas** que se programan primero y se ejecutan después, todas juntas y en el orden en que se registraron. Para lograrlo, cada tarea se **encapsula como un objeto** (`DroneCommand`): así puede guardarse en una lista, quitarse, ordenarse y ejecutarse más tarde, sin que quien la ejecuta sepa qué hace cada una.

### Tareas disponibles

| Tarea (`TipoTarea`) | Comando | Qué hace (simulado) |
|---|---|---|
| Tomar fotografía | `TomarFotografiaCommand` | Toma una foto; las fotos se **numeran por dron** |
| Escanear zona | `EscanearZonaCommand` | Escanea un radio de **200 a 1000 m** y reporta de 0 a 3 anomalías |
| Medir temperatura | `MedirTemperaturaCommand` | Mide la temperatura del ambiente |

Las operaciones son **simuladas** (no hay un dron físico), pero se apoyan en los datos reales del sistema:

- La **fotografía** y el **escaneo** mencionan la *cámara térmica* solo si el dron de vigilancia tiene `deteccionTermica`.
- La **temperatura** usa el **último clima notificado** al dron (patrón Observer) si está suscrito a una ciudad y ya se consultó; si no, entrega una **lectura simulada entre 10 y 30 °C**.

### Participantes

| Rol en el patrón | Clase / interfaz | Capa |
|---|---|---|
| Comando (interfaz) | `DroneCommand` — `ejecutar`, `getNombre`, `getDrone` | `dominio/command` |
| Comandos concretos | `TomarFotografiaCommand`, `EscanearZonaCommand`, `MedirTemperaturaCommand` | `dominio/command` |
| Receptor (contrato) | `DroneOperacion` — `tomarFotografia`, `escanearZona`, `medirTemperatura` | `dominio/command` |
| Receptor (implementación) | `DroneOperacionService` — realiza las operaciones | `aplicacion/servicio` |
| Invocador | `ProgramadorTareas` — guarda la secuencia y ejecuta cada comando | `aplicacion/servicio` |
| Cliente | `ProgramarMisionServicio` — crea cada comando y se lo entrega al invocador | `aplicacion/servicio` |
| Fábrica de comandos | `TipoTarea` — cada valor sabe crear su comando | `dominio/command` |
| Caso de uso (puerto de entrada) | `ProgramarMisionUseCase` — `programar`, `quitar`, `limpiar`, `tareasProgramadas`, `ejecutarMision` | `aplicacion/puerto/entrada` |
| Resultado | `ResultadoTarea` — orden, tarea, dron, mensaje y si fue exitosa | `aplicacion/puerto/entrada` |
| Adaptador de entrada | `ControlMision` y `mision.fxml` | `infraestructura/ui` |

> `DroneOperacion` es una **interfaz del dominio**: los comandos dependen de ella y no de la clase concreta de la capa de aplicación (`DroneOperacionService`). Así el dominio no conoce capas externas, como exige la arquitectura hexagonal.

### Reglas de negocio

- **Programar** exige un dron y una tarea; si falta alguno, se lanza `DronValidacionException`.
- **Agregar no ejecuta**: crear un comando solo lo guarda; la operación ocurre al **ejecutar la misión**.
- **Ejecutar sin tareas** es rechazado ("No hay tareas programadas en la mision.").
- **Quitar** una posición inexistente (o sin seleccionar) es rechazado.
- **Si una tarea falla**, su error queda en el resultado (`Error: ...`, estado *Error*) y la misión **continúa con las siguientes**.
- Al terminar la ejecución, la lista **queda vacía** para programar la próxima misión.
- Se pueden mezclar **varios drones** en una misma misión.

### Detalles de implementación

- Cada comando guarda el receptor y el dron **al crearse** y solo delega en el receptor cuando se invoca `ejecutar()`. Un comando puede ejecutarse varias veces.
- Los comandos rechazan receptor o dron nulos con `NullPointerException`.
- `ProgramadorTareas.getTareas()` devuelve una **copia de solo lectura**, y sus métodos son `synchronized`.
- Para agregar una tarea nueva basta con **un comando y un valor en `TipoTarea`**; el invocador y el cliente no cambian.
- `DroneOperacionService` acepta un `Random` propio para que las pruebas tengan **resultados repetibles**, y busca el clima del dron comparando por id (el dron de la base de datos es otra instancia que el suscrito).
- Nada se guarda en la base de datos. La entidad `Mision` del modelo **no se usa** aquí: la "misión" del Command es solo la lista de tareas en memoria.

### Relación con los demás patrones

- **Observer:** la tarea *Medir temperatura* lee el clima que el Observer entregó al dron.
- **Strategy:** son independientes; la pestaña Consumo no usa comandos ni la pestaña Mision usa estrategias.

---

## 7. Clima: APIs de Open-Meteo

El puerto de salida `WeatherProvider` se implementa en `infraestructura/weather` con las APIs públicas de **Open-Meteo**, que **no requieren API key**.

| Clase | Responsabilidad |
|---|---|
| `OpenWeatherAdapter` | Implementa `WeatherProvider`: obtiene coordenadas, consulta el clima y arma el `Weather` |
| `GeoApiAdapter` | Convierte una `Ciudad` en `Coordenadas` y **guarda el resultado en caché** (las coordenadas no cambian) |
| `GeoApiClient` | Cliente HTTP de la geocodificación (`geocoding-api.open-meteo.com`) |
| `OpenWeatherClient` | Cliente HTTP del clima actual (`api.open-meteo.com`); pide viento en km/h |
| `ClienteHttp` | Utilidad común de peticiones GET con `java.net.http.HttpClient` (tiempo de espera de 10 s) |
| `Coordenadas` | Latitud y longitud de una ciudad |

`LectorJson` (lector mínimo de JSON con Java estándar, sin librerías externas) vive en `infraestructura/util` y lo comparten estos adaptadores y `JsonRutaLoader`.

Notas:

- El flujo es: **geocodificación → coordenadas → clima actual → `Weather`**.
- La API entrega la **visibilidad en metros**; el adaptador la convierte a **kilómetros**.
- Si falta alguno de los cinco datos, o es nulo, se lanza `ClimaException`.
- Los errores de red o de la API se traducen a `ClimaException`, que hereda de `DronException`, así la interfaz los maneja con el mismo `catch` que el resto de errores del dominio.
- **La pestaña Clima necesita conexión a internet.** Las pestañas Consumo y Mision **no** la necesitan (la ruta es un archivo local y las operaciones son simuladas).

---

## 8. Validaciones de los datos del dron

Las reglas están en `ValidadorDrone` y las usan los casos de uso de crear y modificar:

- **Serial:** obligatorio y con al menos un carácter alfanumérico.
- **Fabricante:** obligatorio, con al menos un carácter alfanumérico y **no puede ser solamente numérico**.
- **Peso:** obligatorio, número válido (acepta coma o punto decimal), **mayor que cero**, y **no puede ser `NaN` ni `Infinity`**.

> `Double.parseDouble("NaN")` e `Double.parseDouble("Infinity")` no lanzan `NumberFormatException`, y como `NaN <= 0` e `Infinity <= 0` son `false`, pasaban la validación del peso. Ahora se rechazan de forma explícita con el mensaje "El peso debe ser un numero valido." (corregido a raíz de `ValidadorDroneTest`).

---

## 9. Tecnologías

| Componente | Versión |
|---|---|
| Java | 11 |
| JavaFX (controls + fxml) | 17.0.9 |
| Maven / javafx-maven-plugin | 0.0.8 |
| PostgreSQL JDBC | 42.7.3 |
| JUnit | 5.10.2 |
| Cliente HTTP | `java.net.http` (incluido en el JDK, sin dependencias extra) |
| API del clima | Open-Meteo (clima + geocodificación) |
| Distancia de la ruta | Fórmula de Haversine (Java estándar) |
| IDE | Eclipse (con EGit para Git) |

El `module-info.java` declara `requires java.net.http` para poder llamar a las APIs, abre `infraestructura.ui` y `dominio.modelo` a JavaFX para que `FXML` y `TableView` funcionen por reflexión, y exporta `dominio.clima` porque `Drone` usa `Weather` y `WeatherObserver` en su API pública.

---

## 10. Base de datos

Se usa **la misma base de datos del primer proyecto** (`dronesdb`), sin cambios. El esquema aplica herencia por tablas:

- **`dron`** (tabla padre): `id`, `serial` (único), `fabricante`, `modelo`, `peso`, `tipo`.
- **`dron_vigilancia`** (tabla hija): `id_dron` (referencia a `dron.id` con `ON DELETE CASCADE`), `deteccion_termica`.

El repositorio solo lee y modifica registros con `tipo = 'VIGILANCIA'`. Crear y modificar se hacen en una transacción sobre ambas tablas, de modo que un fallo parcial no deja registros huérfanos. Los errores de PostgreSQL se traducen a excepciones de dominio según su SQLState (`23505` duplicado, `23514` y `23502` dato inválido).

> Al compartir la base de datos con el primer proyecto, cualquier cambio sobre un dron de vigilancia se refleja en ambas aplicaciones.
> El clima, las suscripciones, el consumo y la misión **no usan la base de datos**.

---

## 11. Estructura del proyecto

```
ProyectoHexa/
├── .env                 ← tus credenciales (NO se sube)
├── .env.example         ← plantilla pública
├── .gitignore
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java
    │   │   └── co/edu/poli/sw2/ProyectoHexa/
    │   │       ├── dominio/
    │   │       │   ├── modelo/          Drone, Vigilancia, ...
    │   │       │   ├── clima/           Ciudad, Weather, WeatherSubject,
    │   │       │   │                    WeatherObserver, WeatherNotifier
    │   │       │   ├── chain/           WeatherHandler, Temperature/Humidity/Wind/
    │   │       │   │                    Pressure/VisibilityHandler,
    │   │       │   │                    CadenaValidacionClima, EvaluacionClima
    │   │       │   ├── strategy/        ConsumoStrategy, Ahorro/Normal/
    │   │       │   │                    AltoRendimientoStrategy,
    │   │       │   │                    Ruta, PuntoGeografico
    │   │       │   ├── command/         DroneCommand, DroneOperacion, TipoTarea,
    │   │       │   │                    TomarFotografia/EscanearZona/
    │   │       │   │                    MedirTemperaturaCommand
    │   │       │   └── excepcion/       DronException y derivadas, ClimaException,
    │   │       │                        RutaException
    │   │       ├── aplicacion/
    │   │       │   ├── puerto/entrada/  casos de uso (drones, clima, consumo y
    │   │       │   │                    mision), Suscripcion, ResultadoNotificacion,
    │   │       │   │                    ResultadoConsumo, ResultadoTarea
    │   │       │   ├── puerto/salida/   DroneRepository, WeatherProvider,
    │   │       │   │                    RutaLoader, CalculadorDistancia
    │   │       │   └── servicio/        servicios de drones, ValidadorDrone,
    │   │       │                        GetWeatherByCityServicio,
    │   │       │                        WeatherNotificationService,
    │   │       │                        ValidarClimaServicio,
    │   │       │                        CalcularConsumoServicio,
    │   │       │                        ProgramarMisionServicio,
    │   │       │                        ProgramadorTareas,
    │   │       │                        DroneOperacionService
    │   │       └── infraestructura/
    │   │           ├── persistencia/    ConexionBD, CargadorEnv,
    │   │           │                    PostgresDroneRepository
    │   │           ├── weather/         adaptadores y clientes de Open-Meteo
    │   │           ├── loader/          JsonRutaLoader
    │   │           ├── util/            LectorJson, HaversineUtil
    │   │           ├── ui/              ControlDrone, ControlClima,
    │   │           │                    ControlConsumo, ControlMision
    │   │           └── web/             App (punto de entrada y wiring)
    │   └── resources/
    │       └── co/edu/poli/sw2/
    │           ├── vista/
    │           │   ├── drone.fxml
    │           │   ├── clima.fxml
    │           │   ├── consumo.fxml
    │           │   ├── mision.fxml
    │           │   └── styles.css
    │           └── datos/
    │               └── ruta.json
    └── test/java/
```

### Puertos de la aplicación

| Tipo | Puerto | Implementado por |
|---|---|---|
| Entrada | `CrearDroneUseCase`, `ModificarDroneUseCase`, `EliminarDroneUseCase`, `BuscarDroneUseCase`, `ListarDronesUseCase` | `*Servicio` de cada caso de uso |
| Entrada | `GetWeatherByCityUseCase` | `GetWeatherByCityServicio` |
| Entrada | `SuscripcionClimaUseCase`, `NotificarClimaUseCase` | `WeatherNotificationService` |
| Entrada | `ValidarClimaUseCase` | `ValidarClimaServicio` (usa la cadena de `dominio/chain`) |
| Entrada | `CalcularConsumoUseCase` | `CalcularConsumoServicio` (contexto del Strategy) |
| Entrada | `ProgramarMisionUseCase` | `ProgramarMisionServicio` (cliente del Command) |
| Salida | `DroneRepository` | `PostgresDroneRepository` |
| Salida | `WeatherProvider` | `OpenWeatherAdapter` |
| Salida | `RutaLoader` | `JsonRutaLoader` |
| Salida | `CalculadorDistancia` | `HaversineUtil` |

`App.java` hace el *wiring*: construye los adaptadores de salida, los servicios y, mediante un `controllerFactory`, inyecta los casos de uso en los controladores:

| Controlador | Recibe |
|---|---|
| `ControlDrone` | 6 casos de uso (el sexto es `SuscripcionClimaUseCase`) |
| `ControlClima` | 4 casos de uso (el cuarto es `ValidarClimaUseCase`) |
| `ControlConsumo` | `CalcularConsumoUseCase` y `ListarDronesUseCase` |
| `ControlMision` | `ProgramarMisionUseCase` y `ListarDronesUseCase` |

> Los recursos (`drone.fxml`, `clima.fxml`, `consumo.fxml`, `mision.fxml`, `styles.css` y `ruta.json`) **deben** estar en `src/main/resources`. Si quedan en otra carpeta, Maven no los copia y la aplicación falla al iniciar con `Location is not set` (los FXML) o con `RutaException` (la ruta). `drone.fxml` incluye a `clima.fxml`, `consumo.fxml` y `mision.fxml` con `fx:include`, así que los cuatro deben estar en la misma carpeta.

> El modelo conserva clases de la primera versión (`Agricultura`, `Sensor`, `Piloto`, `Mision`). **No hacen parte del alcance actual**: el CRUD y la interfaz solo trabajan con `Vigilancia`.

---

## 12. Cómo ejecutar el proyecto

### Requisitos

- JDK 11.
- Eclipse con soporte Maven (m2e) y EGit.
- PostgreSQL corriendo, con la base de datos `dronesdb` y las tablas `dron` y `dron_vigilancia`.
- Conexión a internet (solo para la pestaña Clima).

### Pasos

1. **Clonar el repositorio** desde Eclipse: *File → Import → Git → Projects from Git → Clone URI*.
2. **Importar el proyecto Maven** `ProyectoHexa` (*File → Import → Maven → Existing Maven Projects*).
3. **Crear tu archivo `.env`** en la carpeta `ProyectoHexa`, copiando `.env.example` y poniendo tus datos:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/dronesdb
   DB_USER=tu_usuario
   DB_PASSWORD=tu_password
   ```
   Escribe una variable por línea, sin comillas.
4. **Actualizar el proyecto:** clic derecho → *Maven → Update Project* (Alt+F5) y luego *Project → Clean*.
5. **Ejecutar** `infraestructura/web/App.java` con *Run As → Java Application*.

Desde consola también puede ejecutarse con:

```bash
mvn clean javafx:run
```

### Uso de la pestaña Clima

1. Crea al menos un dron en la pestaña **Drones**.
2. En **Clima**, abre el selector de drones (la lista se recarga cada vez que se abre) y elige uno.
3. Elige una ciudad y pulsa **Suscribir**. Para quitarlo, selecciónalo y pulsa **Desuscribir**.
4. Pulsa **Consultar clima**. Cuando termine, la tabla muestra temperatura, humedad, viento, presión y visibilidad de cada dron suscrito (`-` si aún no ha recibido clima), además de:
   - **Puede volar:** `Si` o `No`, según la cadena de validación.
   - **Motivo del rechazo:** la regla incumplida, o `-` si puede volar.
5. A la izquierda se muestra, por cada ciudad, `CLIMA APTO PARA OPERACIÓN DE DRONES` o `CLIMA NO APTO PARA OPERACIÓN DE DRONES` con la regla incumplida.

### Uso de la pestaña Consumo

1. Al abrir la pestaña se muestran el **origen**, el **destino** y la **distancia (Haversine)** de la ruta del archivo JSON.
2. Elige un **dron** y una **estrategia** (cada una aparece con su factor, por ejemplo *Modo Ahorro (x0.5)*).
3. Pulsa **Calcular**. La tarjeta *Resultado* muestra el dron, la distancia, la estrategia, la operación (`12.51 km x 0.5 = 6.26`) y la **batería requerida**.
4. Cada cálculo se agrega al **historial**; calcula con las tres estrategias para compararlas.

### Uso de la pestaña Mision

1. Elige un **dron** y una **tarea** (*Tomar fotografia*, *Escanear zona* o *Medir temperatura*) y pulsa **Agregar a la mision**. Repite con las tareas y los drones que quieras.
2. La lista *Tareas programadas* las muestra **numeradas en orden**. Para quitar una, selecciónala y pulsa **Quitar**; **Limpiar** las borra todas.
3. Pulsa **Ejecutar mision**. La tabla muestra, por cada tarea, su orden, el dron, el estado (`OK` o `Error`) y el resultado.
4. Para que *Medir temperatura* use un clima real, suscribe antes el dron a una ciudad y pulsa **Consultar clima** en la pestaña Clima; si no, la lectura será simulada.

---

## 13. Pruebas

Las pruebas usan **JUnit 5** y están en `src/test/java`, en los mismos paquetes que el código que prueban. Se dividen en dos grupos.

### Pruebas unitarias (no necesitan base de datos ni internet)

| Clase de prueba | Qué verifica |
|---|---|
| `VigilanciaTest` | Constructor, *getters*/*setters*, tipo `VIGILANCIA` y que sea un `Drone` serializable |
| `ValidadorDroneTest` | Reglas de serial, fabricante y peso (vacíos, solo números, sin alfanuméricos, cero o negativos, coma decimal, `NaN`/`Infinity`) |
| `CrearDroneServicioTest` | Creación, recorte de espacios, modelo opcional, peso con coma, serial duplicado y que un dato inválido **nunca llegue** al repositorio |
| `ModificarDroneServicioTest` | Actualización de datos, dron sin seleccionar, tipo distinto a vigilancia, que un dato inválido no altere el original, inexistente y serial de otro dron |
| `EliminarDroneServicioTest` | Eliminación, que solo se borre el indicado, dron sin seleccionar e inexistente |
| `BuscarYListarDroneServicioTest` | Búsqueda por ID, ID inexistente, lista vacía y orden del listado |
| `ConexionBDTest` | Estructura del Singleton: misma instancia, seguridad con 20 hilos, constructor privado, clase `final`, campo `static volatile` y `toString` sin credenciales |
| `DroneObserverTest` | Que `Drone` sea un `WeatherObserver`, guarde el clima recibido y se compare por id |
| `WeatherNotifierTest` | Sujeto del Observer: notifica a todos, incluye los cinco aspectos, desuscribir, sin duplicados, sin observadores, nulos rechazados y lista de solo lectura |
| `WeatherNotificationServiceTest` | Suscribir, trasladar de ciudad, rechazo de duplicados, desuscribir, solo se consultan ciudades con suscriptores, cada dron recibe el clima de su ciudad, un fallo no detiene a las demás, `retirar` y `sincronizar` |
| `CadenaValidacionClimaTest` | Chain of Responsibility: clima apto (incluidos los límites exactos), cada regla incumplida rechaza con su motivo, si fallan varias se informa la primera, la cadena se detiene en el primer rechazo y los mensajes finales |
| `OpenMeteoAdaptersTest` | Interpretación de las respuestas JSON de la geocodificación y del clima (visibilidad en km, bloque `current_units` ignorado, datos nulos o faltantes), orden de las consultas y caché de coordenadas |
| `ConsumoStrategyTest` | Strategy: cada modo aplica su factor (0.5, 1.0, 1.5), el orden Ahorro < Normal < Alto Rendimiento, distancia cero y rechazo de distancias negativas o `NaN` |
| `CalcularConsumoServicioTest` | Contexto del Strategy: usa la estrategia seleccionada, cambiarla cambia el resultado sin tocar el contexto, acepta una estrategia nueva, rechaza sin dron o sin estrategia, carga la ruta una sola vez y describe el cálculo |
| `PuntoGeograficoTest` | Punto válido, límites de latitud y longitud, coordenadas fuera de rango, nombre obligatorio y que la ruta exija origen y destino |
| `HaversineUtilTest` | Distancias conocidas: un grado en el ecuador (~111.19 km), la ruta de Bogotá (~12.51 km), mismo punto, simetría, antípodas y uso como puerto |
| `JsonRutaLoaderTest` | Lectura de un JSON válido, orden de campos indiferente, falta de destino o de campos, coordenada fuera de rango, archivo vacío, carga de `ruta.json` y archivo inexistente |
| `DroneCommandTest` | Command: cada comando delega en su operación del receptor, crear no ejecuta, puede ejecutarse varias veces, nombre y dron, `TipoTarea` crea el comando correcto y rechaza nulos |
| `ProgramadorTareasTest` | Invocador: ejecuta en orden, agregar no ejecuta, la lista queda vacía, ejecutar sin tareas, una tarea que falla no detiene la misión, quitar, limpiar y lista de solo lectura |
| `ProgramarMisionServicioTest` | Prueba de punta a punta del Command con cliente, invocador y receptor reales: misión completa, datos faltantes, orden de las tareas, quitar y limpiar |
| `DroneOperacionServiceTest` | Receptor: fotos numeradas por dron, cámara térmica solo si el dron la tiene, radio del escaneo, temperatura tomada del clima notificado o simulada entre 10 y 30 °C |

Los casos de uso del CRUD se prueban con `RepositorioEnMemoria`, una implementación falsa del puerto `DroneRepository` que imita las reglas de la base de datos (ids, seriales únicos, no encontrado). El servicio de notificaciones y el receptor del Command se prueban con un `WeatherProvider` falso, los adaptadores de Open-Meteo con clientes falsos y JSON de ejemplo, el contexto del Strategy con una ruta y una distancia falsas (20 km), y la cadena de validación con objetos `Weather` construidos a mano. Así toda la lógica de aplicación se prueba de forma aislada, sin PostgreSQL ni internet.

### Pruebas de integración (necesitan PostgreSQL y el `.env`)

| Clase de prueba | Qué verifica |
|---|---|
| `ConexionBDIT` | Que el `.env` aporte credenciales y que la conexión real sea válida e independiente en cada llamada |
| `PostgresDroneRepositoryIT` | CRUD completo contra la BD, cascada al eliminar, serial duplicado con *rollback*, inexistentes y rechazo de tipos distintos a vigilancia |

Estas pruebas crean drones con serial `TEST-xxxxxxxx` y los **borran al terminar**, para no dejar datos basura en la base de datos compartida.

### Cómo ejecutarlas

- **Eclipse:** clic derecho sobre `src/test/java` → *Run As → JUnit Test* (corre todas), o sobre una clase concreta.
- **Maven:** `mvn test` corre solo las unitarias (las clases `*Test`). Las de integración (`*IT`) se corren a propósito con:
  ```bash
  mvn test -Dtest=*IT
  ```

---

## 14. Seguridad de credenciales

- El archivo `.env` está en `.gitignore` y **nunca se sube** al repositorio. Cada integrante crea el suyo a partir de `.env.example`.
- `db.properties` también está ignorado.
- `ConexionBD.toString()` devuelve un texto fijo (`ConexionBD[configurada]`), así que las credenciales no se filtran ni en un `println` accidental.
- Las APIs de Open-Meteo no requieren clave, por lo que no hay ninguna credencial del clima que proteger.

---

## 15. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Location is not set` al iniciar | Algún FXML (`drone`, `clima`, `consumo` o `mision`) no está en `src/main/resources` | Mover la carpeta `co/...` a `src/main/resources` y hacer *Maven → Update Project* |
| `Faltan las credenciales de la base de datos` | No existe `.env` o le faltan claves | Crear el `.env` a partir de `.env.example` |
| `password authentication failed for user "..."` | Usuario o contraseña incorrectos | Revisar el `.env` (sin texto extra) |
| `Connection ... refused` | PostgreSQL no está corriendo | Iniciar el servicio `postgresql-x64-XX` en `services.msc` |
| `database "dronesdb" does not exist` | La base de datos tiene otro nombre | Corregir `DB_URL` en el `.env` |
| La ventana se ve sin estilos | `styles.css` no está junto al FXML o tiene contenido inválido | Verificar que esté en `src/main/resources/co/edu/poli/sw2/vista/` y que contenga CSS |
| No se ven los archivos `.env` / `.gitignore` en Eclipse | Eclipse oculta los archivos que empiezan con punto | *Package Explorer → ⋮ → Filters* → desmarcar `.* resources` |
| "No fue posible conectarse con la API de ..." en la pestaña Clima | Sin internet, o firewall/proxy bloquea Open-Meteo | Revisar la conexión; las demás ciudades se siguen consultando |
| "La API de ... respondio con el codigo 4xx/5xx" | Open-Meteo no disponible o límite de peticiones | Reintentar más tarde |
| "No hay drones suscritos. Suscriba al menos uno." | Se pulsó *Consultar clima* sin suscripciones | Suscribir un dron a una ciudad antes de consultar |
| La columna *Puede volar* muestra `-` | El dron aún no ha recibido clima | Pulsar **Consultar clima** |
| `The type Weather is not exported from this module` en `Drone.java` | `module-info.java` no exporta `dominio.clima` | Agregar `exports co.edu.poli.sw2.ProyectoHexa.dominio.clima;` |
| `The constructor ControlClima(...) is undefined` / `ControlDrone(...) is undefined` / `ControlConsumo(...)` / `ControlMision(...)` | `App.java` no coincide con los constructores actuales | `ControlDrone` recibe 6 casos de uso, `ControlClima` recibe 4, y `ControlConsumo` y `ControlMision` reciben 2 (ver sección 11) |
| Un dron desaparece de la tabla de Clima al reabrir la app | Las suscripciones solo viven en memoria | Es el comportamiento esperado: volver a suscribirlo |
| `ValidadorDroneTest` falla en los casos `NaN` / `Infinity` | `parsearPeso` no rechaza esos valores | Verificar que `ValidadorDrone` tenga la validación `Double.isNaN` / `Double.isInfinite` (ver sección 8) |
| "No se encontro el archivo de ruta ..." y el botón *Calcular* deshabilitado | `ruta.json` no está en `src/main/resources/co/edu/poli/sw2/datos/` | Colocar el archivo en esa carpeta y hacer *Maven → Update Project* |
| "El punto 'origen' debe tener nombre, latitud y longitud." o "La latitud de ... debe estar entre -90 y 90 grados." | `ruta.json` incompleto o con coordenadas fuera de rango | Revisar el formato del archivo (ver sección 5) |
| "Seleccione una estrategia de consumo." / "Seleccione un drone." | Se pulsó *Calcular* sin elegir estrategia o dron | Elegir ambos en la pestaña Consumo |
| "No hay tareas programadas en la mision." | Se pulsó *Ejecutar mision* con la lista vacía (o ya se ejecutó) | Programar tareas con **Agregar a la mision** |
| "Seleccione una tarea de la lista para quitar." | Se pulsó *Quitar* sin seleccionar una tarea | Seleccionar una tarea de la lista |
| *Medir temperatura* dice "lectura simulada" | El dron no está suscrito o aún no se consultó el clima | Suscribirlo y pulsar **Consultar clima** en la pestaña Clima |

---

## 16. Trabajo con Git (Eclipse / EGit)

1. Abrir la vista *Window → Show View → Git → Git Staging*.
2. Revisar que **`.env` no aparezca** en la lista de cambios.
3. Pasar los archivos a *Staged Changes*, escribir el mensaje y pulsar **Commit and Push**.

Para ignorar un archivo nuevo: clic derecho → *Team → Ignore*.
