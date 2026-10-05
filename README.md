# Reconocimiento de Lenguaje de Señas en Tiempo Real

Aplicación Android desarrollada en Kotlin con Jetpack Compose, CameraX y MediaPipe Tasks Vision. El sistema realiza la detección de puntos clave de la mano (21 landmarks) en tiempo real y clasifica señas y letras mediante inteligencia artificial basada en analisis de caracteristicas geometricas.

## Características Principales

- Vista previa de cámara frontal en tiempo real optimizada con CameraX.
- Deteccion de manos y esqueleto de 21 articulaciones mediante MediaPipe Hand Landmarker.
- Clasificador de inteligencia artificial (SignClassifier) para reconocimento de señas:
  - Letras del alfabeto: A, B, C, D, E, I, L, O, U, V, Y.
  - Gestos comunes: Hola / Palma Abierta, Bien / Pulgar Arriba, OK, Te Quiero (ILY), Llamada.
- Bufer de traduccion de texto para construir palabras y frases letra por letra.
- Renderizado grafico dinamico sobre Canvas ajustado al escalado de la pantalla.
- Optimizacion de memoria y prevencion de fugas de hilos para ejecucion fluida.

## Requisitos del Sistema

- Android Studio Hedgehog (2023.1.1) o superior.
- Min SDK: 24 (Android 7.0).
- Target SDK: 34 o superior.
- Dispositivo fisico Android con camara frontal.

## Configuración del Proyecto

### 1. Dependencias (app/build.gradle.kts)

Añadir las siguientes dependencias al archivo de configuracion del modulo:

```kotlin
dependencies {
    val cameraxVersion = "1.3.4"
    
    // CameraX
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    
    // MediaPipe Tasks Vision
    implementation("com.google.mediapipe:tasks-vision:0.10.14")
    
    // Jetpack Compose & Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
}
```

### 2. Permisos de Cámara (AndroidManifest.xml)

Asegurar los siguientes permisos e indicativos de hardware:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

### 3. Modelo de MediaPipe

1. Descargar el archivo de modelo oficial `hand_landmarker.task` desde la documentacion de Google MediaPipe.
2. Colocar el archivo en el directorio de recursos del proyecto: `app/src/main/assets/hand_landmarker.task`.

## Estructura de Archivos

Copiar los archivos en la estructura de paquetes correspondiente (`com.tuapp.signlanguage`):

- `MainActivity.kt`: Punto de entrada de la aplicacion.
- `SignViewModel.kt`: Gestiona el estado de deteccion de señas y el bufer de texto traducido.
- `ml/HandLandmarkerHelper.kt`: Inicializa MediaPipe, procesa frames de camara y convierte imagenes de forma segura.
- `ml/SignClassifier.kt`: Modulo de inteligencia artificial que clasifica señas en base a los 21 puntos clave.
- `ui/CameraPreview.kt`: Envoltorio Compose para PreviewView e ImageAnalysis de CameraX con manejo de ciclo de vida.
- `ui/HandOverlay.kt`: Dibuja las conexiones y nodos sobre la imagen de la camara.
- `ui/SignLanguageScreen.kt`: Pantalla principal con interfaz de usuario, indicador de confianza y controles de traduccion.

## Instrucciones de Ejecución

1. Conectar un dispositivo Android fisico mediante depuracion USB.
2. Compilar y ejecutar la aplicacion desde Android Studio.
3. Otorgar los permisos de camara solicitados.
4. Realizar señas con la mano frente a la camara frontal para visualizar la deteccion y clasificacion en tiempo real.
