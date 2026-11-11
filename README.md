# Albrimon

App Android para diseñadores de moda y sus clientas. Versión **26.1.0**.

Permite registrar clientes con sus medidas, crear proyectos (prendas) con piezas de patronaje, administrar retazos de tela con foto, calcular un plan de corte y los costos de una prenda, generar la ficha técnica en PDF y vincular la cuenta de una clienta con su ficha mediante un código QR.

## Funciones

- **Clientes:** alta, lista y detalle con medidas corporales.
- **Proyectos:** fecha de entrega, estado, cliente asociado y piezas (nombre, ancho y alto en cm).
- **Retazos:** foto desde cámara o galería, medidas y cantidad.
- **Plan de corte:** acomoda las piezas de un proyecto en los retazos disponibles (`CutPlanner`).
- **Costos:** materiales, mano de obra, imprevistos (%) e IVA (`CostCalculator`).
- **Ficha técnica en PDF:** proyecto, cliente, medidas y piezas; se comparte por WhatsApp, correo, Drive, etc.
- **Vínculo por QR:** el diseñador muestra un QR en la ficha del cliente y la clienta lo escanea al registrarse.

Roles: `DISENADOR` (administra todo) y `CLIENTE` (ve los proyectos vinculados).

## Tecnologías

| Capa | Detalle |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Arquitectura | MVVM: ViewModel + StateFlow |
| Inyección | Hilt |
| Red | Retrofit, OkHttp, Gson; JWT con `AuthInterceptor` |
| Local | Room (caché de retazos) y DataStore (sesión) |
| Cámara | Cámara y galería con `FileProvider` |
| PDF | `android.graphics.pdf.PdfDocument` |
| QR | ZXing (generar) y zxing-android-embedded (escanear) |
| Backend | Spring Boot, SQLite, JWT (carpeta `albrimon-backend`) |

## Integraciones (rúbrica)

1. **Cámara / galería:** foto de los retazos.
2. **PDF y compartir:** ficha técnica del proyecto.
3. **QR:** vínculo de cuenta de cliente.

## Estructura

```
app/src/main/java/com/albrimon/app/
├── core/          DI (Hilt) y componentes de UI
├── data/          remote (API), local (Room, DataStore), repository, file (PDF, fotos)
├── domain/        modelos, CutPlanner, CostCalculator
├── navigation/    rutas y NavGraph
└── presentation/  pantallas y ViewModels por módulo
```

Flujo de datos: **API → Repository → ViewModel → UI**.

## Requisitos

- Android Studio reciente con SDK Android (minSdk 24).
- JDK 21 o superior para el backend y Maven 3.9+.

## Cómo correr el backend

Desde la carpeta `albrimon-backend`, en PowerShell:

```powershell
$env:BOOTSTRAP_DESIGNER_NAME="Confeccionador Albrimon"
$env:BOOTSTRAP_DESIGNER_EMAIL="disenador@albrimon.local"
$env:BOOTSTRAP_DESIGNER_PASSWORD="<elige-una-contraseña>"
$env:JWT_SECRET="pon-aqui-una-clave-aleatoria-de-32-caracteres-o-mas"
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api`. La base SQLite se crea en `./data/albrimon.db`. Para comprobar que responde: `http://localhost:8080/api/health`.

Los valores entre `<...>` los eliges tú. Con el correo y la contraseña que pongas se inicia sesión como diseñador en la app. No guardes contraseñas reales en el repositorio.

## Cómo correr la app

1. Abre la carpeta del proyecto en Android Studio y espera la sincronización de Gradle.
2. Con el backend corriendo, elige un emulador o un celular y pulsa **Run**.

### Dirección del servidor (`BASE_URL`)

Por defecto la app usa `http://10.0.2.2:8080/api/`, que es como el emulador de Android llega al `localhost` de la PC.

En un **celular físico** usa la IP de la PC en la misma red Wi-Fi. Agrega esta línea en `gradle.properties` y sincroniza:

```
albrimon.baseUrl=http://192.168.X.X:8080/api/
```

Si no conecta, permite Java (puerto 8080) en el firewall de Windows para redes privadas.

## Generar el APK

**Build → Build Bundle(s) / APK(s) → Build APK(s)**. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Pruebas

Pruebas unitarias de `CutPlanner` y `CostCalculator`:

```
./gradlew testDebugUnitTest
```

## Flujo de trabajo en Git

Ramas `feat/...` → Pull Request → `develop`. `main` contiene la versión estable.

## Equipo

Proyecto del curso, entrega 10 de noviembre de 2026.
