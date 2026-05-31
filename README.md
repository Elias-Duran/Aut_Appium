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

## Autor

Elias Duran
QA Automation Engineer
