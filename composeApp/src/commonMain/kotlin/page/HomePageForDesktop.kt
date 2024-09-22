package page

import PlatFormUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import utils.AESCrypt
import utils.FileUtils
import utils.HttpUtils
import java.io.File

@Composable
fun HomePageForDeskTop(username: String, page: MutableState<Page>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            DownloadColumn(username)
            Divider(
                color = Color.Black,
                modifier = Modifier
                    .height(300.dp)
                    .width(5.dp)
            )
            UploadColumn()
        }
        Button(onClick = {
            page.value = Page.LoginPage
        },
            modifier = Modifier.defaultMinSize()
        ) {
            Text(text = "退出")
        }
    }
}


@Composable
fun DownloadColumn(username: String) {
    Column(
        modifier = Modifier.wrapContentHeight(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val coroutineScope = rememberCoroutineScope()
        val receiveFiles = remember { mutableStateOf<List<String>>(listOf()) }
        val willDownloadFiles = remember { mutableListOf<String>() }
        val openDialog = remember { mutableStateOf(false) }
        val dialogState = remember { mutableStateOf("") }
        val lastPreviewFile = remember { mutableStateOf("") }
        val lastPreviewFileEncrypt = remember { mutableStateOf("") }
        BaseDialog(openDialog = openDialog, dialogState = dialogState)
        LaunchedEffect(null) {
            launch {
                HttpUtils().get<JsonObject>("/receive/${username}").collect {
                    println("[receive] $it")
                    Gson().fromJson(it.toString(), ReceiveFiles::class.java)?.receiveFiles?.let {
                        receiveFiles.value = it
                    }
                }
            }
        }
        Text("我的待下载文件", color = Color.Magenta)
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                modifier = Modifier.border(2.dp, Color.Black, shape = RoundedCornerShape(2.dp)),
            ) {
                receiveFiles.value.forEach {
                    Row {
                        val currentFile = remember { mutableStateOf(it) }
                        val checked = remember { mutableStateOf(false) }
                        Text(
                            text = it,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .offset(x = 2.dp)
                        )
                        Checkbox(checked = checked.value, onCheckedChange = {
                            checked.value = it
                            if (it) {
                                willDownloadFiles.add(currentFile.value)
                                PlatFormUtils.androidGetFileContent(username, currentFile.value) { o, e ->
                                    lastPreviewFile.value = o
                                    lastPreviewFileEncrypt.value = e
                                }
                            } else {
                                willDownloadFiles.remove(currentFile.value)
                                if (willDownloadFiles.isEmpty()) {
                                    lastPreviewFile.value = ""
                                    lastPreviewFileEncrypt.value = ""
                                }
                            }
                        })
                    }
                }


            }



            Button(onClick = {
                if (willDownloadFiles.isEmpty()) {
                    println("没有可下载的文件")
                }
                openDialog.value = !openDialog.value
                dialogState.value = "下载中..."
                coroutineScope.launch {
                    delay(timeMillis = 1000)
                    willDownloadFiles.forEach {path ->
                        println("download file $path")
                        if (PlatFormUtils.isAndroid()) {
                            PlatFormUtils.androidDownloadFile(username, path, excel = true) { downloadName ->
                                receiveFiles.value = receiveFiles.value.filter { it != downloadName }
                            }
                        } else {
                            HttpUtils().downloadFile<DownloadState>(username, path, path) { cause ->
                                println("download file result $cause")
                                coroutineScope.launch{
                                    FileUtils.decryptExcelAndSave(path, listOf())
                                }
                            }
                        }

                    }
                    dialogState.value = "下载完成!"
                }
            }) {
                Text(text = "下载")
            }
        }

        Spacer(modifier = Modifier.width(5.dp))


        if (lastPreviewFile.value.isNotEmpty()) {
            Text(
                text = "密文:"
            )

            Text(
                text = lastPreviewFileEncrypt.value,
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .border(width = 2.dp, color = Color.Black)
                    .padding(5.dp),
                overflow = TextOverflow.Ellipsis

            )

            Text(
                text = "解密信息："
            )

            Text(
                text = lastPreviewFile.value,
                modifier = Modifier
                    .heightIn(min= 250.dp, max = 250.dp)
                    .border(width = 2.dp, color = Color.Black)
                    .padding(5.dp),
                overflow = TextOverflow.Ellipsis

            )
        }





    }

}

@Composable
fun UploadColumn() {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        val coroutineScope = rememberCoroutineScope()
        val excelPath = remember { mutableStateOf("") }
        val fileContent = remember { mutableStateOf("") }
        val targetUsername = remember { mutableStateOf("") }
        val openDialog = remember { mutableStateOf(false) }
        val dialogState = remember { mutableStateOf("") }
        BaseDialog(openDialog = openDialog, dialogState = dialogState)
        Text("发送文件给其他用户", color = Color.Magenta)
        Row {
            TextField(value = excelPath.value,
                placeholder = {
                    Text(text = "请输入文件路径")
                } ,onValueChange = {
                    excelPath.value = it
                })
            if (PlatFormUtils.isAndroid()) {
                PlatFormUtils.androidPickFile { path, content ->
                    excelPath.value = path
                    fileContent.value = content
                    println("[androidPickFile] $path")
                }
            }
        }


        if (fileContent.value.isNotEmpty()) {
            Text(
                text = "原文:"
            )

            Text(
                text = fileContent.value,
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .border(width = 2.dp, color = Color.Black)
                    .padding(5.dp),
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "加密预览："
            )

            Text(
                text = AESCrypt.aesEncryptSimple(fileContent.value),
                modifier = Modifier
                    .heightIn(max = 250.dp)
                    .border(width = 2.dp, color = Color.Black)
                    .padding(5.dp),
                overflow = TextOverflow.Ellipsis
            )
        }


        Row {
            TextField(
                modifier = Modifier.wrapContentSize(),
                value = targetUsername.value,
                placeholder = {
                    Text(text = "请输入发送的用户名")
                }, onValueChange = {
                    targetUsername.value = it
                })
            Button(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    dialogState.value = "加密中..."
                    openDialog.value = !openDialog.value
                    coroutineScope.launch {
                        if (PlatFormUtils.isAndroid()) {
                            delay(timeMillis = 1000)
                            PlatFormUtils.androidUploadFile(excelPath.value, targetUsername.value) {
                                fileContent.value = ""
                                dialogState.value = "传输完成！"
                                excelPath.value = ""
                                targetUsername.value = ""
                            }
                        } else {
                            println("upload ${excelPath.value}")
                            HttpUtils().uploadExcelFile<UploadState>(targetUsername.value, excelPath.value) {
                                println("upload ${excelPath.value}: " + it.message)
                            }.collect {
                                when (it.state) {
                                    1 -> {
                                        println("${it.filename} upload succeed.")
                                    }
                                    else -> {}
                                }
                            }
                        }

                    }
                }) {
                Text(text = "加密传输")
            }
        }


    }

}

@Composable
fun BaseDialog(openDialog:MutableState<Boolean>, dialogState: MutableState<String>){
    if(openDialog.value){
        Dialog(onDismissRequest = { openDialog.value = false } ,) {
            Text(text = dialogState.value,
                modifier = Modifier
                .size(200.dp, 50.dp)
                .background(Color.Gray))
        }
    }
}



@Serializable
class UploadState(
    val filename: String? = null,
    val state: Int? = null
)

@Serializable
class DownloadState(
    val filename: String? = null,
    val errorMsg: String? = null
)

@Serializable
class ReceiveFiles(
    val state: Int? = null,
    @SerializedName("receive_files")
    val receiveFiles: List<String>? = null
)


fun String.toTable():List<List<String>> {
    return this.split('\n').map { it.split('\t') }
}