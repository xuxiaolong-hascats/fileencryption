package org.example.fileencryption

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Output
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.OutputStream


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PickSth(callBack: (filePath: String, String)-> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                println("PickSth $uri")
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    inputStream?.let {
                        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val fileName = uri.path?.split("/")?.last()
                        val file = File(directory, fileName)
                        callBack(file.path, file.readText())

                    }
                }
            }
        }
    }
    val permission = Manifest.permission.READ_EXTERNAL_STORAGE
    val allowAccessFile = remember { mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) }
    // 基于 LocalLifecycleOwner 获取 Lifecycle
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val singlePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            allowAccessFile.value = isGranted
        })
    // 在 Activity onStart 时，发起权限事情，如果权限已经获得则跳过
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    singlePermissionResultLauncher.launch(permission)
                }
            }
        }
    }


    val readExternalStoragePermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )


    Button({
//        singlePermissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (allowAccessFile.value) {
            val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Download") // 默认打开的目录 下载目录
//        val initUri = Uri.parse("content://com.android.externalstorage.documents/document/udisk1") // 默认打开的目录 u盘
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE) // 可打开的文件类别
                type = "*/*" // "application/octet-stream" 二进制文件; "*/*"全部文件
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
            }
            launcher.launch(Intent.createChooser(intent, "选择一个文件"))
        } else {
            singlePermissionResultLauncher.launch(permission)
        }
    }){
        Text("选择文件")
    }
}


