import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.fileencryption.MainActivity
import org.example.fileencryption.PickSth

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun downloadFile(userName: String, fileName: String, excel :Boolean, onSuccess:(String) -> Unit) {
        context?.downloadFile(userName, fileName, excel, onSuccess)
    }

    override fun getFileContent(userName: String, fileName: String, onSuccess:(String, String) -> Unit) {
        context?.getFileContent(userName, fileName, onSuccess)
    }


    override fun downloadPath(): String {
        return context?.downloadPath() ?: ""
    }

    override fun uploadFile(fileName: String, targetUserName: String, onSuccess: () -> Unit) {
        context?.uploadFile(fileName, targetUserName, onSuccess)
    }

    @Composable
    override fun image(name : String, modifier: Modifier) {
        context?.image(name, modifier)
    }

    @Composable
    override fun PickFile(callBack: (String, String)->Unit) {
        return PickSth(callBack)
    }

    var context: MainActivity? = null
}

actual fun getPlatform(): Platform = AndroidPlatform