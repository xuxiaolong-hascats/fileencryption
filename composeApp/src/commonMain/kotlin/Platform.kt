interface Platform {
    val name: String

    fun downloadFile(userName: String, fileName: String) {}

    fun uploadFile(fileName: String, targetUserName: String)
    fun downloadPath(): String = ""
}

expect fun getPlatform(): Platform
