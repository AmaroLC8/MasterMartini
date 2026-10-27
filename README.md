# MasterMartini B2B - Aplicación Móvil de Capacitación Técnica

Aplicación móvil desarrollada en **Android (Kotlin + Jetpack Compose)** para la empresa **Master Martini Chile**, diseñada para la capacitación y asistencia técnica de clientes B2B (panaderías, pastelerías, chocolaterías y heladerías).

---

## Descripción del Proyecto

El proyecto responde a la necesidad de **Master Martini** de estructurar la entrega de contenido técnico (recetas, masterclasses en video e insumos) a sus clientes B2B, ofreciendo una experiencia accesible para usuarios con diverso nivel de manejo digital.

Los usuarios principales son emprendedores de panadería, pastelería y chocolatería, muchos de ellos mayores de 50 años. Por eso la app usa textos grandes, botones de al menos 48 dp, íconos acompañados de texto, colores de alto contraste y mensajes de error claros en cada campo.

---

## Integrantes

- Amaro López
- Maximiliano Mella
- Lukas Meza

**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105) · Duoc UC

---

## Arquitectura y Tecnologías Aplicadas

- **Lenguaje:** Kotlin
- **Toolkit de UI:** Jetpack Compose + Material Design 3
- **Patrón arquitectónico:** MVVM (Model - View - ViewModel). Las pantallas solo dibujan el estado y las validaciones están en los ViewModel.
- **Gestión de estado reactivo:** `StateFlow` (privado `_uiState`, público `uiState` de solo lectura) y `collectAsState()`
- **Navegación:** Navigation Compose (`NavHost`, `rememberNavController`)
- **Persistencia local (clave-valor):** `SharedPreferences` (`AlmacenamientoLocal`) para el perfil, la contraseña y la encuesta
- **Base de datos local:** Room (con KSP) para los cursos favoritos
- **Ubicación:** Google Play Services Location (`FusedLocationProviderClient`)

---

## Estructura de Paquetes

Paquete base: `com.example.mastermartini`

```
com.example.mastermartini
├── MainActivity.kt   → Punto de entrada: tema, NavHost con las rutas y creación de los ViewModels
├── model/            → Clases de datos (Curso)
├── data/             → Repositorios (CursoRepository, FavoritoRepository) y AlmacenamientoLocal (SharedPreferences)
│   └── local/        → Room: FavoritoEntity (tabla), FavoritoDao (consultas) y MasterMartiniDatabase
├── viewmodel/        → Estado y validaciones: RegistroViewModel, LoginViewModel, HomeViewModel, EncuestaViewModel
└── ui/
    ├── screens/      → Pantallas: Login, Registro, Home, Encuesta y el reproductor de video
    ├── components/   → Piezas reutilizables: IconoValidacion y LogoMasterMartini
    └── theme/        → Colores corporativos, tipografía y tema Material 3
```

---

## Funcionalidades Implementadas

### 1. Registro de Emprendedor
- Validaciones en tiempo real desde `RegistroViewModel` (la pantalla no valida nada):
  - **Nombre:** mínimo 3 caracteres.
  - **RUT:** formato (7 u 8 números, guion y dígito o K, acepta puntos) y dígito verificador calculado con el algoritmo **módulo 11**.
  - **Correo:** debe incluir `@` y punto.
  - **Teléfono celular chileno:** solo números, empieza con 9 y tiene 9 dígitos (se ignoran los espacios).
  - **Contraseña:** mínimo 4 caracteres.
- Cada campo tiene un ícono que indica qué dato va, un ícono ✓/⚠ de estado (`IconoValidacion`) y su mensaje de error.
- Selección de especialidad técnica y tamaño del negocio con `FilterChips`.
- El botón "REGISTRAR Y ACCEDER" solo se habilita cuando todo el formulario es válido.
- El perfil se guarda en `SharedPreferences`. Al editar los datos desde Home, el formulario aparece precargado.

### 2. Inicio de Sesión
- Validación en `LoginViewModel`: los campos no pueden quedar vacíos.
- Se ingresa con el **RUT o el correo** registrado, más la contraseña.
- Las credenciales se comparan con lo guardado en `AlmacenamientoLocal`. Si no coinciden, se muestra un aviso con ícono de advertencia.

### 3. Catálogo de Cursos (MVVM)
- Flujo de datos: `CursoRepository` → `HomeViewModel` → `StateFlow` → `collectAsState()` → `HomeScreen`.
- Catálogo de 10 cursos con su insumo Master Martini, chef instructor y duración.

### 4. Favoritos Persistentes con Room
- Arquitectura completa: `FavoritoEntity` (tabla `favoritos`), `FavoritoDao`, `MasterMartiniDatabase` (singleton) y `FavoritoRepository`.
- Cada curso tiene un botón de corazón (área táctil de 56 dp) para marcarlo o desmarcarlo.
- Contador de favoritos en la barra superior, que se actualiza solo porque Room emite los cambios con `Flow`.
- Los favoritos se mantienen al rotar la pantalla y al cerrar la app.

### 5. Encuesta de Preferencias e Intereses
- Selección múltiple de áreas de interés (`Checkbox`) y nivel técnico (`RadioButton`).
- Validación en `EncuestaViewModel`: al menos un interés y un nivel.
- Las respuestas se guardan en `SharedPreferences` (los intereses con `putStringSet`) y vuelven a aparecer marcadas al abrir la encuesta de nuevo.

### 6. Recursos Nativos del Dispositivo
- **Cámara:** foto del local con `ActivityResultLauncher` (`TakePicturePreview`) y permiso `CAMERA` pedido en tiempo de ejecución. Si se niega el permiso o no hay cámara, se muestra un aviso y el registro sigue funcionando.
- **GPS:** ubicación actual del taller con `FusedLocationProviderClient.getCurrentLocation` (alta precisión), con permisos `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` pedidos en tiempo de ejecución. Si se niega el permiso o no hay señal, se muestra un aviso y la encuesta se puede guardar igual.
- **Vibración:** pulso de 100 ms con `Vibrator` al tocar el botón de registro.

### 7. Animaciones
- Mensajes de error y avisos que aparecen y desaparecen con `AnimatedVisibility`.
- Banner "Formulario validado correctamente" con `fadeIn` / `fadeOut`.
- Corazón de favoritos que crece con rebote usando `animateFloatAsState` + `spring`.

### 8. Video Local en Home
- Masterclass destacada reproducida sin internet con `VideoView` y `MediaController`, desde `res/raw/video_receta_master.mp4`.

---

## Requisitos

- Android Studio reciente (con soporte para AGP 9 y compileSdk 37).
- `minSdk` 24 (Android 7.0) y `targetSdk` 37.
- Emulador o dispositivo físico con una API reciente. Para el GPS, usar una imagen del emulador **con Google Play Services**.

---

## Pasos para Ejecutar el Proyecto

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/AmaroLC8/MasterMartini.git
   ```
2. **Abrir el proyecto en Android Studio:** `File → Open` y seleccionar la carpeta `MasterMartini`.
3. **Esperar el Sync de Gradle:** Android Studio descarga las dependencias automáticamente. Si no parte solo, usar `File → Sync Project with Gradle Files`.
4. **Crear o elegir un emulador:** `Device Manager → Create Virtual Device`, o conectar un celular con la depuración USB activada.
5. **Ejecutar:** seleccionar el dispositivo y presionar **Run** (▶) o `Shift + F10`.

---

## Cómo Usar la App

1. **Registro:** la app abre en el Login. Tocar "¿No tienes cuenta? Regístrate aquí", completar el formulario (por ejemplo, RUT `12345678-5` y celular `912345678`), crear una contraseña y tocar "REGISTRAR Y ACCEDER".
2. **Login:** las siguientes veces, ingresar con el RUT o el correo registrado y la contraseña.
3. **Home:**
   - Ver los datos del perfil y reproducir la masterclass en video.
   - Marcar cursos como favoritos con el corazón. El contador aparece en la barra superior.
   - "Editar Datos del Registro" abre el formulario precargado.
4. **Encuesta:** desde Home, tocar "Encuesta de Preferencias y Ubicación (GPS)". Marcar intereses y nivel, tocar "Usar mi ubicación actual" (aceptar el permiso) y luego "GUARDAR PREFERENCIAS".

### Simular la ubicación en el emulador

El emulador no tiene GPS real, así que la ubicación se le entrega a mano:

1. En la barra lateral del emulador, abrir **⋯ (Extended Controls) → Location**.
2. Escribir una latitud y longitud (por ejemplo, Santiago: `-33.4489`, `-70.6693`) o elegir un punto en el mapa.
3. Presionar **Set Location**.
4. Volver a la app y tocar "Usar mi ubicación actual": aparecen las coordenadas.

---

## Nota

Todos los datos de la aplicación (cursos, chefs, insumos y perfiles) son **ficticios** y el proyecto tiene **fines exclusivamente académicos** para la asignatura DSY1105 de Duoc UC.
