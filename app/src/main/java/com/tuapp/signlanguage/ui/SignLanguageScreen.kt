package com.tuapp.signlanguage.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuapp.signlanguage.SignViewModel

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
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Se necesita permiso de cámara para la detección con IA")
        }
        return
    }

    val vm: SignViewModel = viewModel()
    val result by vm.handResult.collectAsStateWithLifecycle()
    val translatedText by vm.translatedText.collectAsStateWithLifecycle()
    var showOverlay by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        CameraPreview(Modifier.fillMaxSize()) { vm.onFrame(it) }

        if (showOverlay) {
            HandOverlay(result, Modifier.fillMaxSize())
        }

        // Card Superior: Resultado de Detección e IA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
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
                        text = "Reconocimiento de IA",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Manos: ${result.hands.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val label = result.prediction.label
                val confidencePct = (result.prediction.confidence * 100).toInt()

                Text(
                    text = label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (confidencePct > 70) MaterialTheme.colorScheme.onSurface else Color.Gray
                )

                if (result.prediction.description.length > 0) {
                    Text(
                        text = result.prediction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { result.prediction.confidence },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = if (confidencePct > 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
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

        // Card Inferior: Búfer de Traducción de Texto y Botones de Acción
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Texto Traducido:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (translatedText.length == 0) "Haz una seña y presiona 'Añadir'..." else translatedText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (translatedText.length == 0) FontWeight.Normal else FontWeight.Bold,
                        color = if (translatedText.length == 0) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { vm.appendCurrentSign() },
                        enabled = result.prediction.confidence > 0.6f
                    ) {
                        Text("+ Añadir")
                    }

                    OutlinedButton(onClick = { vm.addSpace() }) {
                        Text("Espacio")
                    }

                    OutlinedButton(onClick = { vm.deleteLastChar() }) {
                        Text("⌫")
                    }

                    TextButton(onClick = { vm.clearText() }) {
                        Text("Limpiar")
                    }
                }
            }
        }
    }
}
