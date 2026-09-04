package com.derda.tclauncher.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.derda.tclauncher.data.AppEntry

/**
 * Bir uygulama kartına uzun basınca (OK/Enter tuşunu basılı tutunca) açılan menü.
 * Projectivy Launcher'daki "long-press" hızlı işlemlerinin karşılığı.
 */
@Composable
fun AppQuickActionsDialog(
    entry: AppEntry,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onToggleHidden: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            onClick = {},
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1A1E23)),
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
            modifier = Modifier.fillMaxWidth(0.4f).padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = entry.label, color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                QuickActionItem(label = "Uygulamayı aç") {
                    context.packageManager.getLaunchIntentForPackage(entry.packageName)?.let {
                        context.startActivity(it)
                    }
                    onDismiss()
                }

                QuickActionItem(label = "Uygulama bilgisi") {
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${entry.packageName}")
                            }
                        )
                    }
                    onDismiss()
                }

                QuickActionItem(label = if (isHidden) "Listede tekrar göster" else "Listeden gizle") {
                    onToggleHidden()
                    onDismiss()
                }

                QuickActionItem(label = "Kaldır (Uninstall)", danger = true) {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${entry.packageName}")
                            }
                        )
                    }
                    onDismiss()
                }

                Spacer(modifier = Modifier.height(8.dp))
                QuickActionItem(label = "Kapat", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun QuickActionItem(label: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = if (danger) Color(0xFFB3261E) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (danger) Color(0xFFFF8A80) else Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
