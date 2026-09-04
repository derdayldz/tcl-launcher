package com.derda.tclauncher.data

import kotlinx.serialization.Serializable

/**
 * Ana ekrandaki tek bir satırı temsil eder (örn. "Uygulamalarım", "Son İzlenenler").
 */
@Serializable
data class LauncherRow(
    val id: String,
    var title: String,
    var type: RowType,
    var visible: Boolean = true,
    var order: Int = 0,
    /** type = CUSTOM_APPS için: kullanıcının bu satıra elle eklediği paket adları */
    var packageNames: MutableList<String> = mutableListOf()
)

@Serializable
enum class RowType {
    ALL_APPS,       // Cihazdaki tüm yüklü uygulamalar otomatik listelenir
    CUSTOM_APPS,    // Kullanıcının elle seçtiği uygulamalar
    RECENTLY_USED,  // Launcher'ın kendi tuttuğu "en son açılan uygulamalar" geçmişi
    WATCH_HISTORY   // Kullanıcının izlediği video/dizi/film geçmişi (uygulama bazlı)
}

/**
 * Genel launcher görünüm ayarları.
 */
@Serializable
data class LauncherSettings(
    val wallpaperUri: String? = null,       // Eski tek-resim ayarı (geriye dönük uyumluluk için tutuluyor)
    val wallpaperUris: List<String> = emptyList(),
    val wallpaperIntervalMinutes: Int = 15,
    val darkTheme: Boolean = true,
    val accentColorHex: String = "#1565C0",
    val showClock: Boolean = true,
    val showWeather: Boolean = false,
    val weatherCity: String = ""
)
