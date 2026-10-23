# Gestión de Drones — Arquitectura Hexagonal

Aplicación de escritorio en **JavaFX** para gestionar drones de **vigilancia** sobre **PostgreSQL**, consultar el **clima** de varias ciudades mediante las APIs de **Open-Meteo** y **evaluar si ese clima permite volar**, construida con **arquitectura hexagonal (puertos y adaptadores)**.

Es la evolución del primer proyecto (`Proyecto-1` / `sw2`). Conserva la misma base de datos, pero reorganiza el código para que la lógica de negocio no dependa de la interfaz gráfica, de la tecnología de persistencia ni de los servicios externos.

**Autores:** Alejandra Cano y Juan Rosero
**Asignatura:** Ingeniería de Software 2 — Politécnico Grancolombiano

---

## 1. Alcance

El proyecto se limita deliberadamente a lo esencial:

- **5 casos de uso del CRUD:** crear, modificar, eliminar, buscar por ID y listar todos.
- **4 casos de uso del clima:** suscribir/desuscribir drones a una ciudad, consultar el clima de una ciudad, notificar a los drones suscritos y **validar si el clima permite operar drones**.
- **Un único tipo de dron:** `Vigilancia` (con su atributo `deteccionTermica`). No es posible cambiar a otro tipo; los drones de agricultura que existan en la base de datos simplemente no se muestran.
- **Tres patrones de diseño:**
  - **Singleton**, aplicado a la conexión con la base de datos (`ConexionBD`).
  - **Observer**, aplicado a las notificaciones del clima (`WeatherSubject`, `WeatherNotifier`, `WeatherObserver` y `Drone`).
  - **Chain of Responsibility**, aplicado a la validación de la viabilidad de vuelo (`WeatherHandler` y sus cinco eslabones).

> La explicación de la **arquitectura hexagonal** (capas, puertos, adaptadores y casos de uso) está en la **Wiki** del repositorio.

La interfaz tiene **dos pestañas**:

| Pestaña | Qué permite |
|---|---|
| **Drones** | CRUD de drones de vigilancia: formulario, búsqueda por ID, listado y tabla de resultados |
| **Clima** | Suscribir drones a una ciudad, consultar el clima, ver qué clima recibió cada dron y si **puede volar** con ese clima (columna **Puede volar** y motivo del rechazo) |

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

## 5. Clima: APIs de Open-Meteo

El puerto de salida `WeatherProvider` se implementa en `infraestructura/weather` con las APIs públicas de **Open-Meteo**, que **no requieren API key**.

| Clase | Responsabilidad |
|---|---|
| `OpenWeatherAdapter` | Implementa `WeatherProvider`: obtiene coordenadas, consulta el clima y arma el `Weather` |
| `GeoApiAdapter` | Convierte una `Ciudad` en `Coordenadas` y **guarda el resultado en caché** (las coordenadas no cambian) |
| `GeoApiClient` | Cliente HTTP de la geocodificación (`geocoding-api.open-meteo.com`) |
| `OpenWeatherClient` | Cliente HTTP del clima actual (`api.open-meteo.com`); pide viento en km/h |
| `ClienteHttp` | Utilidad común de peticiones GET con `java.net.http.HttpClient` (tiempo de espera de 10 s) |
| `LectorJson` | Lector mínimo de JSON con Java estándar, sin librerías externas |
| `Coordenadas` | Latitud y longitud de una ciudad |

Notas:

- El flujo es: **geocodificación → coordenadas → clima actual → `Weather`**.
- La API entrega la **visibilidad en metros**; el adaptador la convierte a **kilómetros**.
- Si falta alguno de los cinco datos, o es nulo, se lanza `ClimaException`.
- Los errores de red o de la API se traducen a `ClimaException`, que hereda de `DronException`, así la interfaz los maneja con el mismo `catch` que el resto de errores del dominio.
- **La pestaña Clima necesita conexión a internet.** El resto de la aplicación solo necesita PostgreSQL.

---

## 6. Validaciones de los datos del dron

Las reglas están en `ValidadorDrone` y las usan los casos de uso de crear y modificar:

- **Serial:** obligatorio y con al menos un carácter alfanumérico.
- **Fabricante:** obligatorio, con al menos un carácter alfanumérico y **no puede ser solamente numérico**.
- **Peso:** obligatorio, número válido (acepta coma o punto decimal), **mayor que cero**, y **no puede ser `NaN` ni `Infinity`**.

> `Double.parseDouble("NaN")` e `Double.parseDouble("Infinity")` no lanzan `NumberFormatException`, y como `NaN <= 0` e `Infinity <= 0` son `false`, pasaban la validación del peso. Ahora se rechazan de forma explícita con el mensaje "El peso debe ser un numero valido." (corregido a raíz de `ValidadorDroneTest`).

---

## 7. Tecnologías

| Componente | Versión |
|---|---|
| Java | 11 |
| JavaFX (controls + fxml) | 17.0.9 |
| Maven / javafx-maven-plugin | 0.0.8 |
| PostgreSQL JDBC | 42.7.3 |
| JUnit | 5.10.2 |
| Cliente HTTP | `java.net.http` (incluido en el JDK, sin dependencias extra) |
| API del clima | Open-Meteo (clima + geocodificación) |
| IDE | Eclipse (con EGit para Git) |

El `module-info.java` declara `requires java.net.http` para poder llamar a las APIs, abre `infraestructura.ui` y `dominio.modelo` a JavaFX para que `FXML` y `TableView` funcionen por reflexión, y exporta `dominio.clima` porque `Drone` usa `Weather` y `WeatherObserver` en su API pública.

---

## 8. Base de datos

Se usa **la misma base de datos del primer proyecto** (`dronesdb`), sin cambios. El esquema aplica herencia por tablas:

- **`dron`** (tabla padre): `id`, `serial` (único), `fabricante`, `modelo`, `peso`, `tipo`.
- **`dron_vigilancia`** (tabla hija): `id_dron` (referencia a `dron.id` con `ON DELETE CASCADE`), `deteccion_termica`.

El repositorio solo lee y modifica registros con `tipo = 'VIGILANCIA'`. Crear y modificar se hacen en una transacción sobre ambas tablas, de modo que un fallo parcial no deja registros huérfanos. Los errores de PostgreSQL se traducen a excepciones de dominio según su SQLState (`23505` duplicado, `23514` y `23502` dato inválido).

> Al compartir la base de datos con el primer proyecto, cualquier cambio sobre un dron de vigilancia se refleja en ambas aplicaciones.
> El clima y las suscripciones **no usan la base de datos**.

---

## 9. Estructura del proyecto

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
    │   │       │   └── excepcion/       DronException y derivadas, ClimaException
    │   │       ├── aplicacion/
    │   │       │   ├── puerto/entrada/  casos de uso (drones y clima),
    │   │       │   │                    ValidarClimaUseCase,
    │   │       │   │                    Suscripcion, ResultadoNotificacion
    │   │       │   ├── puerto/salida/   DroneRepository, WeatherProvider
    │   │       │   └── servicio/        servicios de drones, ValidadorDrone,
    │   │       │                        GetWeatherByCityServicio,
    │   │       │                        WeatherNotificationService,
    │   │       │                        ValidarClimaServicio
    │   │       └── infraestructura/
    │   │           ├── persistencia/    ConexionBD, CargadorEnv,
    │   │           │                    PostgresDroneRepository
    │   │           ├── weather/         adaptadores y clientes de Open-Meteo
    │   │           ├── ui/              ControlDrone, ControlClima
    │   │           └── web/             App (punto de entrada y wiring)
    │   └── resources/
    │       └── co/edu/poli/sw2/vista/
    │           ├── drone.fxml
    │           ├── clima.fxml
    │           └── styles.css
    └── test/java/
```

### Puertos de la aplicación

| Tipo | Puerto | Implementado por |
|---|---|---|
| Entrada | `CrearDroneUseCase`, `ModificarDroneUseCase`, `EliminarDroneUseCase`, `BuscarDroneUseCase`, `ListarDronesUseCase` | `*Servicio` de cada caso de uso |
| Entrada | `GetWeatherByCityUseCase` | `GetWeatherByCityServicio` |
| Entrada | `SuscripcionClimaUseCase`, `NotificarClimaUseCase` | `WeatherNotificationService` |
| Entrada | `ValidarClimaUseCase` | `ValidarClimaServicio` (usa la cadena de `dominio/chain`) |
| Salida | `DroneRepository` | `PostgresDroneRepository` |
| Salida | `WeatherProvider` | `OpenWeatherAdapter` |

`App.java` hace el *wiring*: construye los adaptadores de salida, los servicios y, mediante un `controllerFactory`, inyecta los casos de uso en `ControlDrone` y `ControlClima` (que además recibe `ValidarClimaUseCase`).

> Los recursos (`drone.fxml`, `clima.fxml` y `styles.css`) **deben** estar en `src/main/resources`. Si quedan en otra carpeta, Maven no los copia y la aplicación falla al iniciar con `Location is not set`. `drone.fxml` incluye a `clima.fxml` con `fx:include`, así que ambos deben estar en la misma carpeta.

> El modelo conserva clases de la primera versión (`Agricultura`, `Sensor`, `Piloto`, `Mision`). **No hacen parte del alcance actual**: el CRUD y la interfaz solo trabajan con `Vigilancia`.

---

## 10. Cómo ejecutar el proyecto

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

---

## 11. Pruebas

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

Los casos de uso del CRUD se prueban con `RepositorioEnMemoria`, una implementación falsa del puerto `DroneRepository` que imita las reglas de la base de datos (ids, seriales únicos, no encontrado). El servicio de notificaciones se prueba con un `WeatherProvider` falso, los adaptadores de Open-Meteo con clientes falsos y JSON de ejemplo, y la cadena de validación con objetos `Weather` construidos a mano. Así toda la lógica de aplicación se prueba de forma aislada, sin PostgreSQL ni internet.

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

## 12. Seguridad de credenciales

- El archivo `.env` está en `.gitignore` y **nunca se sube** al repositorio. Cada integrante crea el suyo a partir de `.env.example`.
- `db.properties` también está ignorado.
- `ConexionBD.toString()` devuelve un texto fijo (`ConexionBD[configurada]`), así que las credenciales no se filtran ni en un `println` accidental.
- Las APIs de Open-Meteo no requieren clave, por lo que no hay ninguna credencial del clima que proteger.

---

## 13. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Location is not set` al iniciar | `drone.fxml` o `clima.fxml` no están en `src/main/resources` | Mover la carpeta `co/...` a `src/main/resources` y hacer *Maven → Update Project* |
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
| `The constructor ControlClima(...) is undefined` / `ControlDrone(...) is undefined` | `App.java` no coincide con los constructores actuales | `ControlDrone` recibe 6 casos de uso (el sexto es `SuscripcionClimaUseCase`) y `ControlClima` recibe 4 (el cuarto es `ValidarClimaUseCase`) |
| Un dron desaparece de la tabla de Clima al reabrir la app | Las suscripciones solo viven en memoria | Es el comportamiento esperado: volver a suscribirlo |
| `ValidadorDroneTest` falla en los casos `NaN` / `Infinity` | `parsearPeso` no rechaza esos valores | Verificar que `ValidadorDrone` tenga la validación `Double.isNaN` / `Double.isInfinite` (ver sección 6) |

---

## 14. Trabajo con Git (Eclipse / EGit)

1. Abrir la vista *Window → Show View → Git → Git Staging*.
2. Revisar que **`.env` no aparezca** en la lista de cambios.
3. Pasar los archivos a *Staged Changes*, escribir el mensaje y pulsar **Commit and Push**.

Para ignorar un archivo nuevo: clic derecho → *Team → Ignore*.
