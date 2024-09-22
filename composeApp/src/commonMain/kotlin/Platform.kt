import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String

    fun downloadFile(userName: String, fileName: String, excel :Boolean, onSuccess: (String) -> Unit) {}

    fun getFileContent(userName: String, fileName: String, onSuccess:(String, String) -> Unit) {}

    fun uploadFile(fileName: String, targetUserName: String, onSuccess: () -> Unit)
    fun downloadPath(): String = ""

    @Composable
    fun image(name :String, modifier: Modifier) {}

    @Composable
    fun PickFile(callBack: (String, String)->Unit){}
}

expect fun getPlatform(): Platform
