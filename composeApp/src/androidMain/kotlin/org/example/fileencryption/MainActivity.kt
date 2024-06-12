package org.example.fileencryption

import AndroidPlatform
import App
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //用户拒绝权限，重新申请
            if (!Environment.isExternalStorageManager()) {
                requestManagerPermission();
            }
        }
    }

    private fun requestManagerPermission() {
        //当系统在11及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 没文件管理权限时申请权限
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + baseContext.packageName));
                startActivityForResult(intent, 1);
            }
        }
    }

    fun downloadFile(userName: String, fileName: String) {
        println("Android download file : $userName $fileName")
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val cw = ContextWrapper(applicationContext)
//        val directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        println(directory?.path)
        val client = OkHttpClient()
        val request = Request.Builder()
            .get()
            .url(baseUrl() + "/download/?filename=$fileName&username=$userName")
            .build()
        val call = client.newCall(request)
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Android download file onFailure ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val inputStream = response.body?.byteStream() ?: return
                    val outputStream = FileOutputStream(file)

                    try {
                        val buffer = ByteArray(2048)
                        var len =inputStream.read(buffer)
                        while(len != -1) {
                            outputStream.write(buffer, 0, len)
                            len = inputStream.read(buffer)
                        }
                        outputStream.flush()
                        println("Android download file success, file path: ${"$directory/$fileName"}")
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

    fun uploadFile(fileName: String, targetUserName: String): String {
        val path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + fileName
        println("Android upload path: $path")
        val file = File(path)
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file" ,fileName, file.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
            .build()
        val request = Request.Builder()
            .url(baseUrl() + "/upload/$targetUserName")
            .post(requestBody)
            .build()
        val call = client.newCall(request)
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Android[uploadFile] onFailure ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    println("Android[uploadFile] onResponse ${response.message}")
                }

            }
        )
        return path
    }

    fun privateEncryption() {
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}