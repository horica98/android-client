package com.ocaterinca.ocaterinca.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64OutputStream
//import androidx.exifinterface.media.ExifInterface
import java.io.*

object ImageUtils {


    fun createBitmapFromPath(
        path: String?,
        options: BitmapFactory.Options = BitmapFactory.Options()
    ): Bitmap? {
        if (path == null) {
            return null
        }
        val exif = ExifInterface(path)
        val exifRotation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val rotation = when (exifRotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        val bitmap = BitmapFactory.decodeFile(path, options)
        if (rotation == 0) {
            return bitmap
        }
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedBitmap
    }

    fun resizeImageKeepAspectRatio(photoPath: String?, maxWidth: Int, maxHeight: Int): Base64Image {
        var image = createBitmapFromPath(photoPath)!!
        val width = image.width
        val height = image.height
        val ratioBitmap = width / height.toFloat();
        val ratioMax = maxWidth / maxHeight.toFloat()
        var finalWidth = maxWidth;
        var finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        val newFile = File.createTempFile("image", ".jpg")
        image.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(newFile))
        return Base64Image(newFile, convertImageFileToBase64(newFile))
    }

    private fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(
                    outputStream,
                    android.util.Base64.DEFAULT
                ).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close()
                    outputStream.toString()
                }
            }
        }
    }

    data class Base64Image(val file: File, val base64: String)
}