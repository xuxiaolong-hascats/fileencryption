interface Platform {
    val name: String

    fun downloadFile(fileName: String) {}

    fun filePath(fileName: String): String = ""

    fun downloadPath(): String = ""
}

expect fun getPlatform(): Platform
