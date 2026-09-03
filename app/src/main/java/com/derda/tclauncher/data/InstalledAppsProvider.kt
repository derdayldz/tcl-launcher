package com.derda.tclauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

object InstalledAppsProvider {

    /** Ana ekrandan/başlatıcıdan açılabilen tüm uygulamaları döner (kendi launcher'ımız hariç). */
    fun getLaunchableApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        var resolved = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)

        // TV banner'ı olmayan bazı uygulamalar LEANBACK_LAUNCHER'da çıkmayabilir, normal launcher'ı da tara.
        if (resolved.isEmpty()) {
            val fallback = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            resolved = pm.queryIntentActivities(fallback, PackageManager.MATCH_ALL)
        }

        return resolved
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                AppEntry(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(pm).toString(),
                    icon = runCatching { info.loadIcon(pm) }.getOrNull()
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun getLaunchIntent(context: Context, packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    fun isSystemApp(info: ApplicationInfo): Boolean {
        return (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}
