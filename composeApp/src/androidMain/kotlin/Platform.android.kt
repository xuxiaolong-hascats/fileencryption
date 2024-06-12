import android.os.Build
import org.example.fileencryption.MainActivity

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun downloadFile(userName: String, fileName: String) {
        context?.downloadFile(userName, fileName)
    }


    override fun downloadPath(): String {
        return context?.downloadPath() ?: ""
    }

    override fun uploadFile(fileName: String, targetUserName: String) {
        context?.uploadFile(fileName, targetUserName)
    }

    var context: MainActivity? = null
}

actual fun getPlatform(): Platform = AndroidPlatform