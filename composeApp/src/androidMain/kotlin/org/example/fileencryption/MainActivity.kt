package org.example.fileencryption

import AndroidPlatform
import App
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import okhttp3.*
import utils.baseUrl
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")

        AndroidPlatform.context = this
        setContent {
            App()
        }
    }

    fun downloadFile(fileName: String) {
        println("Android download file : $fileName")
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        println(filepath.path)
        val client = OkHttpClient()
        val request = Request.Builder()
            .get()
            .url(baseUrl() + "/download/" + fileName)
            .build()
        val call = client.newCall(request)
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Android download file onFailure ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val inputStream = response.body?.byteStream() ?: return
                    val outputStream = FileOutputStream(File(filepath, fileName))

                    try {
                        val buffer = ByteArray(2048)
                        var len =inputStream.read(buffer)
                        while(len != -1) {
                            outputStream.write(buffer, 0, len)
                            len = inputStream.read(buffer)
                        }
                        outputStream.flush()
                        println("Android download file success, file path: ${"$filepath/$fileName"}")
                    } catch (e: Exception) {
                        println("Android download file write error ${e.message}")
                    }
                }

            }
        )
    }

    fun downloadPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    fun filePath(fileName: String): String {
        val path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + fileName
        println("Android upload path: $path")
        return path
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}