package com.derda.tclauncher.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.derda.tclauncher.data.LauncherRepository
import com.derda.tclauncher.data.LauncherRow
import com.derda.tclauncher.data.LauncherSettings
import com.derda.tclauncher.data.RowType
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    repository: LauncherRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by repository.settingsFlow.collectAsState(initial = LauncherSettings())
    val rows by repository.rowsFlow.collectAsState(initial = emptyList())

    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            scope.launch {
                repository.saveSettings(settings.copy(wallpaperUri = uri.toString()))
            }
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF0B0E11))
            .padding(40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onBack, colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))) {
                Text(text = "←  Ana Ekrana Dön", color = Color.White, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(text = "Ayarlar", color = Color.White, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(24.dp))

        TvLazyColumn {

            item { SectionTitle("Sistem") }
            item {
                SettingActionCard(
                    title = "Bu uygulamayı Ana Ekran (Home) yap",
                    subtitle = "Sistem ekranı açılır, listeden bu uygulamayı seç",
                    onClick = {
                        runCatching {
                            context.startActivity(Intent("android.settings.HOME_SETTINGS"))
                        }
                    }
                )
            }

            item { SectionTitle("Görünüm") }
            item {
                SettingActionCard(
                    title = "Arka plan resmi seç",
                    subtitle = "Galeriden bir görsel seçip ana ekrana uygula",
                    onClick = { wallpaperPicker.launch(arrayOf("image/*")) }
                )
            }
            item {
                SettingToggleCard(
                    title = "Koyu tema",
                    checked = settings.darkTheme,
                    onToggle = { scope.launch { repository.saveSettings(settings.copy(darkTheme = it)) } }
                )
            }
            item {
                SettingToggleCard(
                    title = "Saati göster",
                    checked = settings.showClock,
                    onToggle = { scope.launch { repository.saveSettings(settings.copy(showClock = it)) } }
                )
            }
            item {
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    listOf("#1565C0", "#2E7D32", "#C62828", "#6A1B9A", "#EF6C00").forEach { hex ->
                        ColorDot(hex = hex, selected = settings.accentColorHex == hex) {
                            scope.launch { repository.saveSettings(settings.copy(accentColorHex = hex)) }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }

            item { SectionTitle("Ana Ekran Düzeni (Satırlar)") }
            item {
                Text(
                    text = "Üstteki satırlar önce, alttakiler sonra görünür. Bir satırın sırasını/görünürlüğünü değiştirmek için ana ekranda o satırın en solundaki karta gidip iki kez sola bas.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(rows.sortedBy { it.order }) { row ->
                RowSummaryCard(row = row)
            }

            item {
                SettingActionCard(
                    title = "+ Yeni özel satır ekle",
                    subtitle = "Kendi seçtiğin uygulamalarla yeni bir satır oluştur",
                    onClick = {
                        val newRow = LauncherRow(
                            id = UUID.randomUUID().toString(),
                            title = "Yeni Satır",
                            type = RowType.CUSTOM_APPS,
                            order = (rows.maxOfOrNull { it.order } ?: 0) + 1
                        )
                        scope.launch { repository.saveRows(rows + newRow) }
                    }
                )
            }

            item {
                SettingActionCard(
                    title = "+ Video/izlenenler satırı ekle",
                    subtitle = "İzlediğin video/dizi uygulamalarını burada takip et",
                    onClick = {
                        val newRow = LauncherRow(
                            id = UUID.randomUUID().toString(),
                            title = "İzlenenler",
                            type = RowType.WATCH_HISTORY,
                            order = (rows.maxOfOrNull { it.order } ?: 0) + 1
                        )
                        scope.launch { repository.saveRows(rows + newRow) }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.06f),
            focusedContainerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SettingToggleCard(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        onClick = { onToggle(!checked) },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.06f),
            focusedContainerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 16.sp)
            Text(text = if (checked) "Açık" else "Kapalı", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ColorDot(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Blue)
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(containerColor = color),
        shape = ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
        modifier = Modifier.size(if (selected) 40.dp else 32.dp)
    ) {}
}

@Composable
private fun RowSummaryCard(row: LauncherRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = row.title, color = Color.White, fontSize = 15.sp)
        Text(
            text = if (row.visible) "Görünür" else "Gizli",
            color = if (row.visible) Color(0xFF81C784) else Color(0xFFEF9A9A),
            fontSize = 13.sp
        )
    }
}
