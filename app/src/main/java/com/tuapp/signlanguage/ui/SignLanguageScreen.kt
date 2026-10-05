package com.tuapp.signlanguage.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuapp.signlanguage.SavedSentence
import com.tuapp.signlanguage.SignViewModel

enum class AppTab(val title: String) {
    TRANSLATOR("Traductor"),
    DICTIONARY("Diccionario"),
    HISTORY("Historial")
}

data class DictionaryItem(
    val title: String,
    val category: String,
    val icon: String,
    val description: String
)

private val DICTIONARY_ITEMS = listOf(
    DictionaryItem("A", "Abecedario", "✊", "Puño cerrado con el pulgar a un lado."),
    DictionaryItem("B", "Abecedario", "✋", "Mano abierta con dedos juntos y pulgar doblado."),
    DictionaryItem("C", "Abecedario", "🤏", "Mano curvada formando una letra C."),
    DictionaryItem("D", "Abecedario", "☝️", "Índice hacia arriba, pulgar y otros dedos formando círculo."),
    DictionaryItem("E", "Abecedario", "✊", "Dedos doblados tocando la yema del pulgar."),
    DictionaryItem("F", "Abecedario", "👌", "Índice y pulgar unidos en O, demás dedos estirados."),
    DictionaryItem("G", "Abecedario", "👈", "Índice y pulgar apuntando horizontalmente."),
    DictionaryItem("H", "Abecedario", "✌️", "Índice y medio extendidos juntos horizontalmente."),
    DictionaryItem("I", "Abecedario", "🤙", "Dedo meñique estirado verticalmente."),
    DictionaryItem("L", "Abecedario", "👆", "Índice y pulgar formando una letra L."),
    DictionaryItem("M", "Abecedario", "👊", "Tres dedos sobre el pulgar."),
    DictionaryItem("N", "Abecedario", "👊", "Dos dedos sobre el pulgar."),
    DictionaryItem("O", "Abecedario", "👌", "Todos los dedos tocando el pulgar en forma de O."),
    DictionaryItem("P", "Abecedario", "🤞", "Dedo medio extendido abajo e índice horizontal."),
    DictionaryItem("Q", "Abecedario", "👇", "Índice y pulgar apuntando hacia abajo."),
    DictionaryItem("R", "Abecedario", "🤞", "Dedos índice y medio cruzados."),
    DictionaryItem("S", "Abecedario", "✊", "Puño cerrado con el pulgar frente a los dedos."),
    DictionaryItem("T", "Abecedario", "👊", "Pulgar metido entre el índice y el medio."),
    DictionaryItem("U", "Abecedario", "✌️", "Dedos índice y medio estirados juntos hacia arriba."),
    DictionaryItem("V", "Abecedario", "✌️", "Dedos índice y medio separados en forma de V."),
    DictionaryItem("W", "Abecedario", "🤟", "Dedos índice, medio y anular levantados."),
    DictionaryItem("X", "Abecedario", "☝️", "Dedo índice encorvado en forma de gancho."),
    DictionaryItem("Y", "Abecedario", "🤙", "Pulgar y meñique estirados (seña de teléfono)."),
    DictionaryItem("Hola", "Saludos", "👋", "Mano abierta moviéndose suavemente."),
    DictionaryItem("Gracias", "Saludos", "🙏", "Toque en la barbilla llevado hacia adelante."),
    DictionaryItem("Por Favor", "Saludos", "🤲", "Palma en el pecho girando en círculo."),
    DictionaryItem("Sí", "Respuestas", "✊", "Puño cerrado asintiendo de arriba a abajo."),
    DictionaryItem("No", "Respuestas", "🤌", "Unir índice, medio y pulgar repetidamente.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignLanguageScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }

    if (!hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Permiso de Cámara Requerido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "La aplicación utiliza la cámara para detectar y traducir las señas de las manos en tiempo real mediante IA.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conceder Permiso")
                    }
                }
            }
        }
        return
    }

    val vm: SignViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(AppTab.TRANSLATOR) }
    val showOverlay by vm.showOverlay.collectAsStateWithLifecycle()
    val autoSpeak by vm.autoSpeak.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SignLanguage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Language AI",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    if (selectedTab == AppTab.TRANSLATOR) {
                        IconButton(onClick = { vm.toggleOverlay() }) {
                            Icon(
                                imageVector = if (showOverlay) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Alternar esqueleto de mano",
                                tint = if (showOverlay) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        IconButton(onClick = { vm.toggleCamera() }) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Cambiar cámara"
                            )
                        }
                        IconButton(onClick = { vm.toggleAutoSpeak() }) {
                            Icon(
                                imageVector = if (autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Lectura por voz",
                                tint = if (autoSpeak) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == AppTab.TRANSLATOR,
                    onClick = { selectedTab = AppTab.TRANSLATOR },
                    icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                    label = { Text("Traductor") }
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.DICTIONARY,
                    onClick = { selectedTab = AppTab.DICTIONARY },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Diccionario") }
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.HISTORY,
                    onClick = { selectedTab = AppTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Historial") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.TRANSLATOR -> LiveTranslatorTab(vm = vm)
                AppTab.DICTIONARY -> DictionaryTab()
                AppTab.HISTORY -> HistoryTab(vm = vm)
            }
        }
    }
}

@Composable
fun LiveTranslatorTab(vm: SignViewModel) {
    val context = LocalContext.current
    val result by vm.handResult.collectAsStateWithLifecycle()
    val translatedText by vm.translatedText.collectAsStateWithLifecycle()
    val isFrontCamera by vm.isFrontCamera.collectAsStateWithLifecycle()
    val showOverlay by vm.showOverlay.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        // Vista previa de cámara en vivo
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFrontCamera = isFrontCamera
        ) { vm.onFrame(it) }

        // Esqueleto gráfico de puntos clave
        if (showOverlay) {
            HandOverlay(result, Modifier.fillMaxSize())
        }

        // Tarjeta Superior: Estado del Reconocimiento de IA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (result.hands.isNotEmpty()) Color(0xFF4CAF50) else Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reconocimiento IA",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Manos: ${result.hands.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val label = result.prediction.label
                val confidencePct = (result.prediction.confidence * 100).toInt()

                Text(
                    text = label,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (confidencePct > 70) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (result.prediction.description.isNotEmpty()) {
                    Text(
                        text = result.prediction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = result.prediction.confidence,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = when {
                            confidencePct > 80 -> Color(0xFF4CAF50)
                            confidencePct > 50 -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$confidencePct%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tarjeta Inferior: Texto Traducido y Panel de Control
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Texto Traducido",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(
                            onClick = {
                                if (translatedText.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Traducción", translatedText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = translatedText.isNotBlank()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar texto")
                        }

                        IconButton(
                            onClick = { vm.speakText(translatedText) },
                            enabled = translatedText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Escuchar texto",
                                tint = if (translatedText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        IconButton(
                            onClick = {
                                vm.saveCurrentSentence()
                                Toast.makeText(context, "Guardado en Historial", Toast.LENGTH_SHORT).show()
                            },
                            enabled = translatedText.isNotBlank()
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Guardar frase")
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (translatedText.isEmpty()) "Realiza señas frente a la cámara y presiona '+ Añadir'..." else translatedText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (translatedText.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                        color = if (translatedText.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de botones de acción rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { vm.appendCurrentSign() },
                        enabled = result.prediction.confidence > 0.5f,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Añadir", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { vm.addSpace() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Espacio")
                    }

                    OutlinedButton(
                        onClick = { vm.deleteLastChar() },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Backspace, contentDescription = "Borrar último", modifier = Modifier.size(18.dp))
                    }

                    TextButton(
                        onClick = { vm.clearText() },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("Limpiar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryTab() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todo") }

    val categories = listOf("Todo", "Abecedario", "Saludos", "Respuestas")

    val filteredItems = remember(searchQuery, selectedCategory) {
        DICTIONARY_ITEMS.filter { item ->
            val matchesCategory = (selectedCategory == "Todo" || item.category == selectedCategory)
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar seña o letra...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron señas para \"$searchQuery\"",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    DictionaryCard(item = item)
                }
            }
        }
    }
}

@Composable
fun DictionaryCard(item: DictionaryItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.icon,
                fontSize = 38.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryTab(vm: SignViewModel) {
    val context = LocalContext.current
    val history by vm.savedHistory.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Frases Guardadas (${history.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (history.isNotEmpty()) {
                TextButton(onClick = { vm.clearHistory() }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar todo", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aún no has guardado ninguna frase traducida.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Traduce señas y presiona 'Guardar' para verlas aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history, key = { it.id }) { item ->
                    HistoryCard(item = item, vm = vm, context = context)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: SavedSentence, vm: SignViewModel, context: Context) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Row {
                IconButton(onClick = { vm.speakText(item.text) }) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Escuchar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Frase guardada", item.text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                }

                IconButton(onClick = { vm.deleteSavedSentence(item.id) }) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
