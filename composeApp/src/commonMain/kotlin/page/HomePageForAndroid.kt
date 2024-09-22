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

        val isDownloadPage = remember { mutableStateOf(false) }

        if (isDownloadPage.value) {
            DownloadColumn(username)

        } else {
            UploadColumn()
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.height(40.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = {
                isDownloadPage.value = false
            }) {
                Text("发送页")
            }
            Button(onClick = {
                isDownloadPage.value = true
            }) {
                Text("接收页")
            }
            Button(onClick = {
                page.value = Page.LoginPage
            }) {
                Text("退出登录")
            }
        }
    }


}
