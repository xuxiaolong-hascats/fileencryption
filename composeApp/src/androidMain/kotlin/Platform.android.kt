import android.os.Build
import org.example.fileencryption.MainActivity

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun downloadFile(fileName: String) {
        context?.downloadFile(fileName)
    }


    override fun downloadPath(): String {
        return context?.downloadPath() ?: ""
    }

    override fun filePath(fileName: String): String {
        return context?.filePath(fileName) ?: ""
    }

    var context: MainActivity? = null
}

actual fun getPlatform(): Platform = AndroidPlatform