package com.derda.tclauncher.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Uygulama ikonlarını (PackageManager'dan gelen Drawable) Compose'un
 * doğrudan çizebileceği bir ImageBitmap'e çevirir.
 */
fun Drawable.toComposeImageBitmap(sizePx: Int = 128): ImageBitmap {
    val width = if (intrinsicWidth > 0) intrinsicWidth else sizePx
    val height = if (intrinsicHeight > 0) intrinsicHeight else sizePx
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}
