package com.derda.tclauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.*
import com.derda.tclauncher.data.AppEntry
import com.derda.tclauncher.data.InstalledAppsProvider
import com.derda.tclauncher.data.LauncherRepository
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    repository: LauncherRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val hiddenPackages by repository.hiddenPackagesFlow.collectAsState(initial = emptySet())
    val allApps = remember { InstalledAppsProvider.getLaunchableApps(context) }

    var query by remember { mutableStateOf("") }

    val results = remember(query, hiddenPackages) {
        if (query.isBlank()) emptyList()
        else allApps.filter {
            it.packageName !in hiddenPackages &&
                it.label.contains(query, ignoreCase = true)
        }
    }

    fun openApp(entry: AppEntry) {
        InstalledAppsProvider.getLaunchIntent(context, entry.packageName)?.let {
            context.startActivity(it)
            scope.launch { repository.recordAppOpened(entry.packageName) }
            onBack()
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF0B0E11))
            .padding(40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text(text = "←  Ana Ekrana Dön", color = Color.White, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f)),
            shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "🔍", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.material3.TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { androidx.compose.material3.Text("Uygulama ara...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (query.isBlank()) {
            Text(
                text = "Yazmaya başla, uygulamalar anında filtrelenecek.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        } else if (results.isEmpty()) {
            Text(
                text = "\"$query\" ile eşleşen bir uygulama bulunamadı.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(results) { entry ->
                    SearchResultCard(entry = entry, onClick = { openApp(entry) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(entry: AppEntry, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
        modifier = Modifier.size(width = 140.dp, height = 130.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            val iconBitmap = remember(entry.packageName) {
                entry.icon?.let { runCatching { it.toComposeImageBitmap() }.getOrNull() }
            }
            if (iconBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBitmap,
                    contentDescription = entry.label,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                ) {
                    Text(text = entry.label.take(1).uppercase(), fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = entry.label, fontSize = 12.sp, maxLines = 2)
        }
    }
}
