package com.forestcoins.game

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestcoins.game.data.GraphicsQuality
import com.forestcoins.game.data.GraphicsSettingsRepository
import com.forestcoins.game.ui.theme.ForestCoinsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsRepository by lazy { GraphicsSettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForestCoinsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    val currentScreen = remember { mutableStateOf(Screen.Menu) }
                    val qualityState = remember { mutableStateOf(GraphicsQuality.HIGH) }

                    LaunchedEffect(Unit) {
                        qualityState.value = settingsRepository.readQuality()
                    }

                    Crossfade(targetState = currentScreen.value, label = "mainNavigation") { screen ->
                        when (screen) {
                            Screen.Menu -> MainMenu(
                                quality = qualityState.value,
                                onPlay = { startGame() },
                                onSettings = { currentScreen.value = Screen.Settings }
                            )

                            Screen.Settings -> GraphicsSettingsScreen(
                                selectedQuality = qualityState.value,
                                onBack = { currentScreen.value = Screen.Menu },
                                onSelect = { quality ->
                                    qualityState.value = quality
                                    scope.launch { settingsRepository.saveQuality(quality) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startGame() {
        startActivity(Intent(this, GameActivity::class.java))
    }
}

enum class Screen { Menu, Settings }

@Composable
private fun MainMenu(quality: GraphicsQuality, onPlay: () -> Unit, onSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFB2FF59), Color(0xFF69F0AE), Color(0xFF80DEEA))
                )
            )
            .padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        DecorativeBackground()
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Text(
                    text = "Forest Coins",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Собери все золотые монеты в волшебном лесу!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC80)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Text(
                        text = "Играть",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF5D4037)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                ElevatedButton(
                    onClick = onSettings,
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF80DEEA)),
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Настройки", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Текущее качество: ${quality.name.lowercase().replaceFirstChar { it.uppercaseChar() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun DecorativeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val hillPaint = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)))
        drawRoundRect(
            brush = hillPaint,
            topLeft = Offset(-width * 0.2f, height * 0.55f),
            size = androidx.compose.ui.geometry.Size(width * 1.4f, height * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.7f, width * 0.7f)
        )
        val sunCenter = Offset(width * 0.8f, height * 0.2f)
        drawCircle(Color(0xFFFFF59D), radius = width * 0.18f, center = sunCenter)
        drawCircle(Color(0xFFFFFDE7), radius = width * 0.14f, center = sunCenter)
        val treeColors = listOf(Color(0xFF66BB6A), Color(0xFF81C784), Color(0xFFA5D6A7))
        treeColors.forEachIndexed { index, color ->
            val baseX = width * (0.15f + index * 0.15f)
            drawIntoCanvas { canvas ->
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(baseX, height * 0.7f)
                    lineTo(baseX - width * 0.05f, height * 0.9f)
                    lineTo(baseX + width * 0.05f, height * 0.9f)
                    close()
                }
                canvas.drawPath(path, androidx.compose.ui.graphics.Paint().apply { color = color })
            }
        }
    }
}

@Composable
private fun GraphicsSettingsScreen(
    selectedQuality: GraphicsQuality,
    onBack: () -> Unit,
    onSelect: (GraphicsQuality) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF69F0AE), Color(0xFF40C4FF))
                )
            )
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Выберите качество графики, чтобы адаптировать производительность к вашему устройству.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            GraphicsQuality.entries.forEach { quality ->
                QualityCard(
                    title = when (quality) {
                        GraphicsQuality.LOW -> "Низкое"
                        GraphicsQuality.MEDIUM -> "Среднее"
                        GraphicsQuality.HIGH -> "Высокое"
                    },
                    description = describeQuality(quality),
                    selected = quality == selectedQuality,
                    onClick = { onSelect(quality) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QualityCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFE082) else Color(0xFFFFFFFF).copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 8.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4E342E)
            )
        }
    }
}

private fun describeQuality(quality: GraphicsQuality): String = when (quality) {
    GraphicsQuality.LOW -> "Меньше деревьев и монет, повышенная производительность."
    GraphicsQuality.MEDIUM -> "Баланс визуальных эффектов и стабильной работы."
    GraphicsQuality.HIGH -> "Максимальная детализация леса и плавность анимации."
}
