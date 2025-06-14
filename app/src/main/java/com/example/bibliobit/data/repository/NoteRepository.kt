package com.example.bibliobit.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.remote.RemoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    @ApplicationContext private val context: Context
) {

    suspend fun getNotesByUserLibraryId(userLibraryId: Long): List<Note> {
        return remoteDataSource.getNotes(userLibraryId)
    }

    suspend fun deleteNote(noteId: Long) {
        remoteDataSource.deleteNote(noteId)
    }

    suspend fun addNote(userLibraryId: Long, content: String, imageUri: Uri?): Note {
        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        // Gunakan fungsi baru untuk mengkompres gambar
        val imagePart = uriToCompressedMultipartBodyPart(imageUri, "image")
        return remoteDataSource.createNote(userLibraryId, contentRequestBody, imagePart)
    }

    suspend fun updateNote(noteId: Long, content: String, imageUri: Uri?): Note {
        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        // Gunakan fungsi baru untuk mengkompres gambar
        val imagePart = uriToCompressedMultipartBodyPart(imageUri, "image")
        return remoteDataSource.updateNote(noteId, contentRequestBody, imagePart)
    }

    private fun uriToCompressedMultipartBodyPart(uri: Uri?, partName: String): MultipartBody.Part? {
        if (uri == null) return null

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream?.close()

            val originalBitmap = context.contentResolver.openInputStream(uri)?.let {
                BitmapFactory.decodeStream(it)
            } ?: return null
            context.contentResolver.openInputStream(uri)?.close()

            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(originalBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(originalBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(originalBitmap, 270f)
                else -> originalBitmap
            }

            val tempFile = File.createTempFile("compressed_image_", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
