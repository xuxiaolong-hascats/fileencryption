import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import okhttp3.internal.platform.Android10Platform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}

object PlatFormUtils {
    private val platform by lazy { getPlatform() }

    fun isAndroid(): Boolean {
        return platform.name.startsWith("Android")
    }

    fun androidDownloadFile(userName: String, fileName: String,  excel :Boolean, onSuccess: (String) -> Unit) {
        if (isAndroid()) {
            platform.downloadFile(userName, fileName, excel, onSuccess)
        }
    }

    fun androidGetFileContent(userName: String, fileName: String, onSuccess: (String, String) -> Unit) {
        if (isAndroid()) {
            platform.getFileContent(userName, fileName, onSuccess)
        }
    }


    fun androidUploadFile(fileName: String, targetUserName: String, onSuccess: () -> Unit) {
        if (isAndroid()) {
            platform.uploadFile(fileName, targetUserName, onSuccess)
        }
    }

    fun androidDownloadPath(): String {
        return if (isAndroid()) {
            platform.downloadPath()
        } else ""
    }

    @Composable
    fun image(name: String, modifier: Modifier) {
        if (isAndroid()) {
            platform.image(name, modifier)
        }
    }

    @Composable
    fun androidPickFile(callBack: (filePath: String, String)->Unit) {
        if (isAndroid()) {
            platform.PickFile(callBack)
        }
    }

    fun isDesktop(): Boolean {
        return platform.name.startsWith("Java")
    }
}