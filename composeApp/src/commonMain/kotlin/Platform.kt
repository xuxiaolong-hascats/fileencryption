import androidx.compose.runtime.Composable

interface Platform {
    val name: String

    fun downloadFile(userName: String, fileName: String, onSuccess: (String) -> Unit) {}

    fun uploadFile(fileName: String, targetUserName: String)
    fun downloadPath(): String = ""

    @Composable
    fun PickFile(callBack: (String, String)->Unit){}
}

expect fun getPlatform(): Platform
