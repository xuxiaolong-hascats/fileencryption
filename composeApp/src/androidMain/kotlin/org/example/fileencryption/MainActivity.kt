package org.example.fileencryption

import AndroidPlatform
import App
import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
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
        AndroidPlatform.context = this
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
//        requestPermissions(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )
    }

    private fun requestPermissions(vararg permissions: String) {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val failed = result.filter { !it.value }.keys
            failed.toList()
        }.launch(arrayOf(*permissions))
    }

    fun downloadFile(userName: String, fileName: String, onSuccess:(String) -> Unit) {
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
                    try {
                        Gson().fromJson(response.body?.string(), DownloadResult::class.java)?.run {
                            if (state == 0) {
                                println("Android download file error state == 0, message $message")
                            }
                            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(directory, fileName)
                            content?.let {
                                file.writeText(content)
                                onSuccess(fileName)
                                println("Android download file success, file path: ${"$directory/$fileName"}")
                            }
                        }
                    } catch (e: Exception) {
                        println("Android download file write error ${e.message}")
                    }
                }

            }
        )
    }

    @Serializable
    class DownloadResult(
        @SerializedName("state")
        val state: Int? = null,
        @SerializedName("filename")
        val filename: String? = null,
        @SerializedName("content")
        val content: String? = null,
        @SerializedName("message")
        val message: String? = null
    )

    fun downloadPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    fun uploadFile(fileName: String, targetUserName: String): String {
//        val path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + fileName
        println("Android upload path: $fileName")
        val file = File(fileName)
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
        return fileName
    }

    fun privateEncryption() {
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}