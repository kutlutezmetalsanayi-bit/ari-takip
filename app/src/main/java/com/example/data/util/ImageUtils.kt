package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
  private const val MAX_DIMENSION = 1200
  private const val JPEG_QUALITY = 80

  /**
   * Reads an image from Uri, fixes EXIF rotation, scales down to max 1200px,
   * compresses to JPEG 80%, and saves locally in app private files directory.
   */
  suspend fun compressAndSaveImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
    try {
      val contentResolver = context.contentResolver

      // 1. Decode bounds
      val boundsStream = contentResolver.openInputStream(sourceUri) ?: return@withContext null
      val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeStream(boundsStream, null, options)
      boundsStream.close()

      // Calculate sample size
      var sampleSize = 1
      var width = options.outWidth
      var height = options.outHeight

      while (width / 2 >= MAX_DIMENSION || height / 2 >= MAX_DIMENSION) {
        width /= 2
        height /= 2
        sampleSize *= 2
      }

      // 2. Decode scaled bitmap
      val decodeStream = contentResolver.openInputStream(sourceUri) ?: return@withContext null
      val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
      }
      val decodedBitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
      decodeStream.close()

      if (decodedBitmap == null) return@withContext null

      // 3. Fix EXIF orientation
      val orientation = getExifOrientation(context, sourceUri)
      val matrix = Matrix()
      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
      }

      val rotatedBitmap = if (!matrix.isIdentity) {
        Bitmap.createBitmap(
          decodedBitmap,
          0,
          0,
          decodedBitmap.width,
          decodedBitmap.height,
          matrix,
          true
        ).also {
          if (it != decodedBitmap) decodedBitmap.recycle()
        }
      } else {
        decodedBitmap
      }

      // 4. Save to app private directory
      val imagesDir = File(context.filesDir, "hive_photos").apply { if (!exists()) mkdirs() }
      val photoFile = File(imagesDir, "photo_${UUID.randomUUID()}.jpg")

      FileOutputStream(photoFile).use { out ->
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        out.flush()
      }
      rotatedBitmap.recycle()

      photoFile.absolutePath
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun getExifOrientation(context: Context, uri: Uri): Int {
    return try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
      } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (e: Exception) {
      ExifInterface.ORIENTATION_NORMAL
    }
  }
}
