package com.albrimon.app.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Foto nueva lista para que la cámara escriba en [uri]; [relativePath] es lo que se guarda en la BD. */
data class NewPhoto(val relativePath: String, val uri: Uri)

/**
 * Fotos de retazos en el almacenamiento PRIVADO de la app (filesDir/scrap_photos).
 * No necesita permisos de almacenamiento; la cámara escribe mediante FileProvider.
 */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val directory: File
        get() = File(context.filesDir, DIR).apply { mkdirs() }

    fun newPhoto(): NewPhoto {
        val name = "${UUID.randomUUID()}.jpg"
        val file = File(directory, name).also { it.createNewFile() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return NewPhoto("$DIR/$name", uri)
    }

    /** Devuelve el archivo solo si la ruta es válida (dentro de scrap_photos) y existe. */
    fun file(relativePath: String?): File? = scrapPhotoFile(context, relativePath)

    /** Copia una imagen elegida en la galería al almacenamiento privado. */
    suspend fun importFromGallery(source: Uri): String = withContext(Dispatchers.IO) {
        val photo = newPhoto()
        val target = File(context.filesDir, photo.relativePath)
        try {
            val input = context.contentResolver.openInputStream(source)
                ?: throw IOException("No se pudo abrir la imagen seleccionada.")
            input.use { src -> target.outputStream().use { out -> src.copyTo(out) } }
            if (target.length() == 0L) throw IOException("La imagen seleccionada está vacía.")
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        photo.relativePath
    }

    fun delete(relativePath: String?) {
        if (relativePath.isNullOrBlank() || !relativePath.startsWith("$DIR/") || relativePath.contains("..")) return
        File(context.filesDir, relativePath).delete()
    }

    companion object {
        const val DIR = "scrap_photos"
    }
}

/**
 * Archivo de la foto de un retazo, o null si la ruta no es válida (fuera de scrap_photos) o no existe.
 * Función suelta para que las pantallas la usen con LocalContext sin depender del contenedor de Hilt.
 */
fun scrapPhotoFile(context: Context, relativePath: String?): File? {
    if (relativePath.isNullOrBlank() ||
        !relativePath.startsWith("${PhotoStorage.DIR}/") ||
        relativePath.contains("..")
    ) {
        return null
    }
    return File(context.filesDir, relativePath).takeIf { it.exists() && it.length() > 0 }
}
