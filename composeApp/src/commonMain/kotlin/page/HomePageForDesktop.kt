package page

import PlatFormUtils
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
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import utils.FileUtils
import utils.HttpUtils

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
        modifier = Modifier.height(300.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val coroutineScope = rememberCoroutineScope()
        val receiveFiles = remember { mutableStateOf<List<String>>(listOf()) }
        val willDownloadFiles = remember { mutableListOf<String>() }
        LaunchedEffect(null) {
            launch {
                HttpUtils().get<JsonObject>("/receive/${username}").collect {
                    Gson().fromJson(it.toString(), ReceiveFiles::class.java)?.receiveFiles?.let {
                        receiveFiles.value = it
                    }
                }
            }
        }
        Text("我的待下载文件", color = Color.Magenta)
        Column(
            modifier = Modifier.border(2.dp, Color.Black, shape = RoundedCornerShape(2.dp))
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
                        } else {
                            willDownloadFiles.remove(currentFile.value)
                        }
                    })
                }
            }
        }

        Button(onClick = {
            if (willDownloadFiles.isEmpty()) {
                println("没有可下载的文件")
            }
            coroutineScope.launch {
                willDownloadFiles.forEach {
                    println("download file $it")
                    if (PlatFormUtils.isAndroid()) {
                        PlatFormUtils.androidDownloadFile(it)
                    } else {
                        HttpUtils().downloadFile<DownloadState>(it, it) { cause ->
                            println("download file result $cause")
                            coroutineScope.launch{
                                FileUtils.decryptExcelAndSave(it, listOf())
                            }
                        }
                    }

                }
            }
        }) {
            Text(text = "下载并解密")
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
        val targetUsername = remember { mutableStateOf("") }
        val excelColumns = remember { mutableStateListOf<String>() }
        val encryptColumns = remember { mutableListOf<String>() }
        Text("上传文件给其他用户", color = Color.Magenta)
        Row {
            TextField(value = excelPath.value,
                placeholder = {
                    Text(text = "请输入文件路径")
                } ,onValueChange = {
                    excelPath.value = it
                })
            Button(onClick = {
                coroutineScope.launch {
                    excelColumns.clear()
                    excelColumns.addAll(FileUtils.getExcelColumns(excelPath.value))
                }
            }){
                Text("确认加密字段")
            }
        }

        Column{
            Column(
                modifier = Modifier.border(2.dp, Color.Black, shape = RoundedCornerShape(2.dp))
                    .height(200.dp)
                    .verticalScroll(rememberScrollState())
            ) { excelColumns.forEach {
                Row {
                    val currentColumnName = remember { mutableStateOf(it) }
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
                            encryptColumns.add(currentColumnName.value)
                        } else {
                            encryptColumns.remove(currentColumnName.value)
                        }
                    })
                }
            } }
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
                    coroutineScope.launch {
                        FileUtils.encryptExcelAndSave(excelPath.value,encryptColumns)?.let { encryptFilePath ->
                            println("upload $encryptFilePath")
                            HttpUtils().uploadExcelFile<UploadState>(targetUsername.value, encryptFilePath) {
                                println("upload ${encryptFilePath}: " + it.message)
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
                Text(text = "加密并发送")
            }
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