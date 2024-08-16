import android.os.Build
import androidx.compose.runtime.Composable
import org.example.fileencryption.MainActivity
import org.example.fileencryption.PickSth

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun downloadFile(userName: String, fileName: String, onSuccess:(String) -> Unit) {
        context?.downloadFile(userName, fileName, onSuccess)
    }


    override fun downloadPath(): String {
        return context?.downloadPath() ?: ""
    }

    override fun uploadFile(fileName: String, targetUserName: String) {
        context?.uploadFile(fileName, targetUserName)
    }

    @Composable
    override fun PickFile(callBack: (String, String)->Unit) {
        return PickSth(callBack)
    }

    var context: MainActivity? = null
}

actual fun getPlatform(): Platform = AndroidPlatform