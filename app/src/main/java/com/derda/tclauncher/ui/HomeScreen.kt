package com.derda.tclauncher.ui

import android.text.format.DateFormat
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.derda.tclauncher.data.*
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    repository: LauncherRepository,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val rows by repository.rowsFlow.collectAsState(initial = emptyList())
    val settings by repository.settingsFlow.collectAsState(initial = LauncherSettings())
    val recentPackages by repository.recentPackagesFlow.collectAsState(initial = emptyList())
    val hiddenPackages by repository.hiddenPackagesFlow.collectAsState(initial = emptySet())

    val installedApps = remember { InstalledAppsProvider.getLaunchableApps(context) }
    val appsByPackage = remember(installedApps) { installedApps.associateBy { it.packageName } }
    val visibleApps = remember(installedApps, hiddenPackages) {
        installedApps.filter { it.packageName !in hiddenPackages }
    }

    var rowSettingsTarget by remember { mutableStateOf<LauncherRow?>(null) }
    var quickActionsTarget by remember { mutableStateOf<AppEntry?>(null) }

    fun openApp(packageName: String) {
        val intent = InstalledAppsProvider.getLaunchIntent(context, packageName) ?: return
        context.startActivity(intent)
        scope.launch { repository.recordAppOpened(packageName) }
    }

    Box(modifier = modifier.background(Color.Black)) {

        val slideshowImages = remember(settings.wallpaperUris, settings.wallpaperUri) {
            settings.wallpaperUris.ifEmpty { listOfNotNull(settings.wallpaperUri) }
        }

        if (slideshowImages.isNotEmpty()) {
            var currentIndex by remember(slideshowImages) { mutableStateOf(0) }

            LaunchedEffect(slideshowImages, settings.wallpaperIntervalMinutes) {
                if (slideshowImages.size <= 1) return@LaunchedEffect
                while (true) {
                    kotlinx.coroutines.delay(settings.wallpaperIntervalMinutes.coerceAtLeast(1) * 60_000L)
                    currentIndex = (currentIndex + 1) % slideshowImages.size
                }
            }

            androidx.compose.animation.Crossfade(
                targetState = slideshowImages.getOrNull(currentIndex),
                animationSpec = androidx.compose.animation.core.tween(1200),
                label = "wallpaper_slideshow"
            ) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Okunabilirlik için hafif karartma
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // Üst bar: saat + ayarlar erişimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (settings.showClock) {
                    ClockText()
                }
                if (settings.showWeather && settings.weatherCity.isNotBlank()) {
                    Spacer(modifier = Modifier.width(20.dp))
                    WeatherText(city = settings.weatherCity)
                }
                Spacer(modifier = Modifier.weight(1f))
                SearchEntryButton(onClick = onOpenSearch)
                Spacer(modifier = Modifier.width(12.dp))
                SettingsEntryButton(onClick = onOpenSettings)
            }

            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(rows.filter { it.visible }) { row ->
                    val entries: List<AppEntry> = when (row.type) {
                        RowType.ALL_APPS -> visibleApps
                        RowType.CUSTOM_APPS, RowType.WATCH_HISTORY ->
                            row.packageNames.mapNotNull { appsByPackage[it] }
                        RowType.RECENTLY_USED ->
                            recentPackages.mapNotNull { appsByPackage[it] }.filter { it.packageName !in hiddenPackages }
                    }

                    RowSection(
                        row = row,
                        entries = entries,
                        onAppClick = { openApp(it.packageName) },
                        onOpenRowSettings = { rowSettingsTarget = row },
                        onLongPressApp = { quickActionsTarget = it }
                    )
                }

                if (rows.none { it.visible }) {
                    item {
                        Text(
                            text = "Tüm satırlar gizli. Ayarlar'dan bir satırı görünür yapabilirsin.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }

        rowSettingsTarget?.let { target ->
            RowSettingsDialog(
                row = target,
                allRows = rows,
                onDismiss = { rowSettingsTarget = null },
                onSave = { updatedRows ->
                    scope.launch { repository.saveRows(updatedRows) }
                    rowSettingsTarget = null
                }
            )
        }

        quickActionsTarget?.let { target ->
            AppQuickActionsDialog(
                entry = target,
                isHidden = target.packageName in hiddenPackages,
                onDismiss = { quickActionsTarget = null },
                onToggleHidden = {
                    val newState = target.packageName !in hiddenPackages
                    scope.launch { repository.setAppHidden(target.packageName, newState) }
                }
            )
        }
    }
}

@Composable
private fun WeatherText(city: String) {
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    LaunchedEffect(city) {
        while (true) {
            weather = WeatherFetcher.fetch(city)
            kotlinx.coroutines.delay(30 * 60_000) // 30 dakikada bir tazele
        }
    }
    weather?.let {
        Text(text = "${it.tempCelsius}°C · ${it.description}", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
    }
}

@Composable
private fun ClockText() {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            kotlinx.coroutines.delay(30_000)
        }
    }
    val context = LocalContext.current
    val text = DateFormat.getTimeFormat(context).format(now)
    Text(text = text, color = Color.White, fontSize = 22.sp)
}

@Composable
private fun SearchEntryButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "🔍", fontSize = 18.sp)
        }
    }
}

@Composable
private fun SettingsEntryButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.CircleShape),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = "⚙", fontSize = 20.sp)
        }
    }
}

@Composable
private fun RowSection(
    row: LauncherRow,
    entries: List<AppEntry>,
    onAppClick: (AppEntry) -> Unit,
    onOpenRowSettings: () -> Unit,
    onLongPressApp: (AppEntry) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = row.title,
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 32.dp, bottom = 10.dp)
        )

        if (entries.isEmpty()) {
            Text(
                text = "Bu satırda henüz bir şey yok. Ayarlardan ekleyebilirsin (sol uçtaki karta girip iki kez sola bas).",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 32.dp)
            )
        }

        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(entries, key = { _, e -> e.packageName }) { index, entry ->
                AppCard(
                    entry = entry,
                    isFirstInRow = index == 0,
                    onOpenRowSettings = onOpenRowSettings,
                    onClick = { onAppClick(entry) },
                    onLongPress = { onLongPressApp(entry) }
                )
            }
        }
    }
}

/** TvLazyRow için basit itemsIndexed yardımcı fonksiyonu */
private inline fun androidx.tv.foundation.lazy.list.TvLazyListScope.itemsIndexed(
    items: List<AppEntry>,
    crossinline key: (Int, AppEntry) -> Any,
    crossinline itemContent: @Composable (Int, AppEntry) -> Unit
) {
    items(items.size) { index ->
        itemContent(index, items[index])
    }
}

@Composable
private fun FallbackIcon(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .background(Color.White.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
    ) {
        Text(text = label.take(1).uppercase(), fontSize = 20.sp)
    }
}

@Composable
private fun AppCard(
    entry: AppEntry,
    isFirstInRow: Boolean,
    onOpenRowSettings: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var lastLeftPressAt by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    var longPressJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var longPressFired by remember { mutableStateOf(false) }

    val leftDoublePressModifier = if (isFirstInRow) {
        Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp && event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                val now = System.currentTimeMillis()
                val isDouble = now - lastLeftPressAt < 500
                lastLeftPressAt = now
                if (isDouble) {
                    onOpenRowSettings()
                    lastLeftPressAt = 0L
                    true // olayı burada tüket, odak dışarı kaçmasın
                } else {
                    // ilk basış: normal davranışa izin ver (odak zaten en solda, bir şey olmaz)
                    true
                }
            } else false
        }
    } else Modifier

    // OK/Enter tuşunu ~500ms basılı tutunca hızlı işlem menüsünü açar (Projectivy'deki "uzun basma")
    val longPressModifier = Modifier.onPreviewKeyEvent { event ->
        val isSelectKey = event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER
        if (!isSelectKey) return@onPreviewKeyEvent false

        when (event.type) {
            KeyEventType.KeyDown -> {
                if (longPressJob == null) {
                    longPressFired = false
                    longPressJob = scope.launch {
                        kotlinx.coroutines.delay(500)
                        longPressFired = true
                        onLongPress()
                    }
                }
                false // basılı tutma başlarken normal odak/tıklama davranışını engelleme
            }
            KeyEventType.KeyUp -> {
                longPressJob?.cancel()
                longPressJob = null
                if (longPressFired) {
                    longPressFired = false
                    true // uzun basma zaten tetiklendi, normal "tıklama"yı (uygulamayı açmayı) engelle
                } else {
                    false // kısa basış: Surface'ın kendi onClick'i normal şekilde çalışsın
                }
            }
            else -> false
        }
    }

    Surface(
        onClick = onClick,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.08f),
            focusedContainerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        modifier = Modifier
            .size(width = 140.dp, height = 130.dp)
            .then(leftDoublePressModifier)
            .then(longPressModifier)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            if (entry.icon != null) {
                val iconBitmap = remember(entry.packageName) {
                    runCatching { entry.icon.toComposeImageBitmap() }.getOrNull()
                }
                if (iconBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = iconBitmap,
                        contentDescription = entry.label,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    FallbackIcon(entry.label)
                }
            } else {
                FallbackIcon(entry.label)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.label,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}
