# Aut_Appium

Arquetipo básico de automatización móvil desarrollado con Appium, Java y Maven.

Este proyecto está diseñado siguiendo el patrón Page Object Model (POM), permitiendo una estructura organizada, reutilizable y escalable para pruebas automatizadas en aplicaciones Android.

## Tecnologías Utilizadas

- Java 17
- Maven
- Appium
- UiAutomator2
- Selenium WebDriver
- Android Studio
- JUnit/TestNG

## Estructura del Proyecto

src
├── main
│   └── java
│
└── test
    └── java
        └── com.automation
            ├── base
            ├── driver
            ├── pages
            └── tests

## Componentes Principales

### DriverFactory
Responsable de crear e inicializar el driver de Appium con todas las capacidades necesarias para la ejecución.

### DriverContext
Permite almacenar y compartir la instancia del driver durante toda la ejecución de las pruebas.

### BaseTest
Clase base encargada de preparar el entorno antes de cada prueba y liberar recursos al finalizar.

### BasePage
Contiene métodos reutilizables para interactuar con elementos de la aplicación, como clics, escritura de texto y esperas explícitas.

### Pages
Representan las distintas pantallas de la aplicación siguiendo el patrón Page Object Model.

### Tests
Contienen los casos de prueba automatizados que validan el comportamiento de la aplicación.

## Objetivo

Construir un framework de automatización Android simple, mantenible y escalable, que sirva como base para futuros proyectos de pruebas móviles utilizando Appium.

## Estado Actual

Proyecto en construcción. Actualmente incluye la configuración inicial del framework, manejo centralizado del driver y estructura base para implementar Page Objects y casos de prueba.

## CI con Jenkins

El proyecto incluye un [`Jenkinsfile`](Jenkinsfile) declarativo para ejecutar las pruebas en un agente local con Android y Appium.

### Requisitos del agente Jenkins

1. Etiqueta del nodo: `android-appium`
2. Plugins: Pipeline, HTML Publisher
3. Herramientas globales (nombres usados en el Jenkinsfile):
   - JDK: `jdk-17`
   - Maven: `Maven-3.9`
4. Variables del sistema en el agente:
   - `ANDROID_HOME` configurado
   - `adb`, `emulator` y `appium` disponibles en PATH
5. Emulador Android accesible como `emulator-5554` (recomendado: dejarlo encendido en el agente dedicado)

### Variables de entorno del pipeline

| Variable | Default | Descripción |
|---|---|---|
| `APPIUM_SERVER_URL` | `http://127.0.0.1:4723` | URL del servidor Appium |
| `ANDROID_DEVICE_NAME` | `emulator-5554` | Nombre del dispositivo/emulador |
| `APP_PATH` | `{workspace}/src/test/resources/app/...apk` | Ruta del APK a instalar |

`DriverFactory` lee estas variables; si no existen, usa los valores por defecto.

### Crear el job Pipeline

1. En Jenkins: **New Item** → **Pipeline**
2. En **Pipeline** → **Definition**: *Pipeline script from SCM*
3. Seleccionar el repositorio y rama
4. Script Path: `Jenkinsfile`
5. Ejecutar el primer build con el emulador ya encendido (`START_EMULATOR=false`)

### Parámetros del build

- `START_EMULATOR` (default: `false`): arranca un AVD en el pipeline si no tienes emulador permanente
- `AVD_NAME` (default: `Pixel_7_API_34`): nombre del AVD cuando `START_EMULATOR=true`

### Artefactos generados

- Reporte HTML: `target/reports/ExtentReport.html` (publicado vía HTML Publisher)
- Screenshots: `target/reports/screenshots/`
- En fallos: `target/debug/logcat.txt` y logs de Appium/emulador

### Ejecución local (sin Jenkins)

```bash
# Con emulador y Appium ya levantados
mvn clean test -B
```

## Autor

Elias Duran
QA Automation Engineer
