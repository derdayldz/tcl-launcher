package com.derda.tclauncher.ui

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
import androidx.compose.ui.window.Dialog
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import com.derda.tclauncher.data.AppEntry
import com.derda.tclauncher.data.InstalledAppsProvider
import com.derda.tclauncher.data.LauncherRow
import com.derda.tclauncher.data.RowType

/**
 * Bir satırın en solundaki karta girip iki kez sola basınca açılan ayar penceresi.
 * Satırı yeniden adlandırma, gizleme, silme, sırasını değiştirme ve
 * (özel/izlenenler satırları için) hangi uygulamaların yer alacağını seçme burada yapılır.
 */
@Composable
fun RowSettingsDialog(
    row: LauncherRow,
    allRows: List<LauncherRow>,
    onDismiss: () -> Unit,
    onSave: (List<LauncherRow>) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(row.title) }
    var showAppPicker by remember { mutableStateOf(false) }
    var selectedPackages by remember { mutableStateOf(row.packageNames.toMutableList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1A1E23)),
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(text = "Satır Ayarları", color = Color.White, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Başlık", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Surface(
                    onClick = { /* basit sürüm: başlık düzenleme metin alanı yerine önceden tanımlı hızlı seçenekler */ },
                    colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Text(text = title, color = Color.White, modifier = Modifier.padding(14.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                ActionRow(
                    label = if (row.visible) "Bu satırı gizle" else "Bu satırı göster",
                    onClick = {
                        val updated = allRows.map { if (it.id == row.id) it.copy(visible = !it.visible) else it }
                        onSave(updated)
                    }
                )

                ActionRow(
                    label = "Yukarı taşı",
                    onClick = {
                        val updated = moveRow(allRows, row.id, -1)
                        onSave(updated)
                    }
                )

                ActionRow(
                    label = "Aşağı taşı",
                    onClick = {
                        val updated = moveRow(allRows, row.id, 1)
                        onSave(updated)
                    }
                )

                if (row.type == RowType.CUSTOM_APPS || row.type == RowType.WATCH_HISTORY) {
                    ActionRow(
                        label = "Uygulama ekle / çıkar",
                        onClick = { showAppPicker = true }
                    )
                }

                ActionRow(
                    label = "Bu satırı sil",
                    danger = true,
                    onClick = {
                        val updated = allRows.filterNot { it.id == row.id }
                        onSave(updated)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                ActionRow(label = "Kapat", onClick = onDismiss)
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            initiallySelected = selectedPackages,
            onDismiss = { showAppPicker = false },
            onConfirm = { picked ->
                selectedPackages = picked.toMutableList()
                val updated = allRows.map {
                    if (it.id == row.id) it.copy(packageNames = picked.toMutableList()) else it
                }
                showAppPicker = false
                onSave(updated)
            }
        )
    }
}

private fun moveRow(rows: List<LauncherRow>, id: String, delta: Int): List<LauncherRow> {
    val sorted = rows.sortedBy { it.order }.toMutableList()
    val index = sorted.indexOfFirst { it.id == id }
    val newIndex = (index + delta).coerceIn(0, sorted.lastIndex)
    if (index == -1 || newIndex == index) return rows
    val item = sorted.removeAt(index)
    sorted.add(newIndex, item)
    return sorted.mapIndexed { i, r -> r.copy(order = i) }
}

@Composable
private fun ActionRow(label: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = if (danger) Color(0xFFB3261E) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (danger) Color(0xFFFF8A80) else Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun AppPickerDialog(
    initiallySelected: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val allApps = remember { InstalledAppsProvider.getLaunchableApps(context) }
    var selected by remember { mutableStateOf(initiallySelected.toMutableSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1A1E23)),
            onClick = {},
            modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight(0.75f).padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Uygulama Seç", color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
                TvLazyColumn(modifier = Modifier.weight(1f)) {
                    items(allApps) { app: AppEntry ->
                        val isSelected = selected.contains(app.packageName)
                        Surface(
                            onClick = {
                                selected = if (isSelected) {
                                    (selected - app.packageName).toMutableSet()
                                } else {
                                    (selected + app.packageName).toMutableSet()
                                }
                            },
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (isSelected) Color(0xFF1565C0).copy(alpha = 0.4f) else Color.Transparent,
                                focusedContainerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = (if (isSelected) "✓ " else "  ") + app.label,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    ActionRow(label = "Kaydet", onClick = { onConfirm(selected.toList()) })
                    Spacer(modifier = Modifier.width(12.dp))
                    ActionRow(label = "Vazgeç", onClick = onDismiss)
                }
            }
        }
    }
}
