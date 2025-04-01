package com.example.bibliobit.utils


import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun savePhotoToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            // Buat direktori untuk menyimpan foto cover
            val coverDir = File(context.filesDir, "cover_photos")
            if (!coverDir.exists()) {
                coverDir.mkdirs()
            }

            // Buat file baru untuk foto
            val fileName = "book_cover_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
            val file = File(coverDir, fileName)

            // Salin data dari Uri ke file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // Kembalikan path absolut file
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}