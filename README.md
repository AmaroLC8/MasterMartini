# MasterMartini B2B - Aplicación Móvil de Capacitación Técnica

Aplicación móvil desarrollada en **Android (Kotlin + Jetpack Compose)** para la empresa **Master Martini Chile**, diseñada para la capacitación y asistencia técnica de clientes B2B (panaderías, pastelerías, chocolaterías y heladerías).

---

##  Descripción del Proyecto

El proyecto responde a la necesidad de **Master Martini** de estructurar la entrega de contenido técnico (recetas, masterclasses en video e insumos) a sus clientes B2B, ofreciendo una experiencia accesible para usuarios con diverso nivel de manejo digital.

---

##  Integrantes

- Amaro Lopez - Maximiliano Mella - Lukas Meza
- **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105) · Duoc UC

---

##  Arquitectura y Tecnologías Aplicadas

- **Lenguaje:** Kotlin
- **Toolkit de UI:** Jetpack Compose + Material Design 3
- **Patrón Arquitectónico:** MVVM (Model - View - ViewModel) desacoplado
- **Gestión de Estado Reactivo:** `StateFlow` y `collectAsState()`
- **Persistencia Local:** `SharedPreferences` (`AlmacenamientoLocal`)
- **Navegación:** `Navigation Component` (`NavHost`, `rememberNavController`)

---

## Funcionalidades Implementadas
[]()
### 1. Formulario de Registro B2B y Autenticación
- Validaciones en tiempo real desde `RegistroViewModel`.
- Verificación sintáctica y cálculo de dígito verificador de RUT con algoritmo Módulo 11.
- Validación de correo electrónico y teléfono celular chileno (9 dígitos).
- Selección de especialidad técnica y tamaño del negocio mediante `FilterChips`.
- Persistencia local de datos del perfil.

### 2. Catálogo de Cursos Desacoplado (MVVM)
- Lista de masterclasses consumida reactivamente desde `CursoRepository` y `HomeViewModel` con `StateFlow`.
- Visualización de insumos principales, chefs instructores y duraciones.

### 3. Encuesta de Preferencias e Intereses
- Formulario de encuestas con selección múltiple de áreas de interés y nivel técnico.
- Validaciones centralizadas por campo en `EncuestaViewModel` con íconos de alerta y mensajes de retroalimentación.

### 4. Integración de Recursos Nativos del Dispositivo
- **Cámara Nativa:** Captura de foto de perfil del local mediante `ActivityResultLauncher` (`TakePicturePreview`) con gestión de permisos en tiempo de ejecución.
- **Feedback Háptico (Vibración):** Pulso de vibración mediante `Vibrator` al completar acciones clave.
- **Ubicación GPS Nativa:** Obtención de coordenadas del taller/negocio utilizando `Play Services Location` (`FusedLocationProviderClient`) con solicitud dinámica de permisos (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`).

### 5. Reproductor de Video Local Offline
- Reproductor de video integrado (`VideoView`) para masterclasses en almacenamiento interno (`res/raw`).

---

## Pasos para Ejecutar el Proyecto

1. **Clonar el repositorio desde GitHub:**

