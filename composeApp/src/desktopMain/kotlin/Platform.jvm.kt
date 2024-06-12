class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override fun uploadFile(fileName: String, targetUserName: String) {
        TODO("Not yet implemented")
    }
}

actual fun getPlatform(): Platform = JVMPlatform()