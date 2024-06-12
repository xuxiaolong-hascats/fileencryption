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

    fun androidDownloadFile(userName: String, fileName: String) {
        if (isAndroid()) {
            platform.downloadFile(userName, fileName)
        }
    }


    fun androidUploadFile(fileName: String, targetUserName: String) {
        if (isAndroid()) {
            platform.uploadFile(fileName, targetUserName)
        }
    }

    fun androidDownloadPath(): String {
        return if (isAndroid()) {
            platform.downloadPath()
        } else ""
    }

    fun isDesktop(): Boolean {
        return platform.name.startsWith("Java")
    }
}