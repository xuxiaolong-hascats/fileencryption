package page

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomePageForAndroid(username: String, page: MutableState<Page>) {
//    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
    ) {

        val isDownloadPage = remember { mutableStateOf(true) }

        if (isDownloadPage.value) {
            DownloadColumn(username)

        } else {
            UploadColumn()
        }

        Row(
            modifier = Modifier.height(40.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = {
                isDownloadPage.value = true
            }) {
                Text("下载")
            }
            Button(onClick = {
                isDownloadPage.value = false
            }) {
                Text("上传")
            }
        }
    }


}
