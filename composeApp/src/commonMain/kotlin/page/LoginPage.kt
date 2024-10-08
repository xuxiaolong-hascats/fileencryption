package page

import PlatFormUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.HttpUtils
import utils.ip

const val TAG = "LoginPage"
@Composable
fun LoginPage(username: MutableState<String> , page: MutableState<Page>) {

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        val password = remember { mutableStateOf("") }

        val errorMsg = remember { mutableStateOf("") }



        Text(text = "资产定位数据加密传输与加密存储", fontSize = 20.sp)

        Row(modifier = Modifier.fillMaxWidth()) {
            PlatFormUtils.image("main_page_img", modifier = Modifier.weight(1f).width(0.dp))
            PlatFormUtils.image("main_page_img2", modifier = Modifier.weight(1f).width(0.dp))
        }

        OutlinedTextField(value = username.value, onValueChange = {
            username.value = it
        }, label = { Text(text = "用户名")})

        if (errorMsg.value.isNotEmpty()) {
            Text(errorMsg.value)
        }

        OutlinedTextField(value = password.value, onValueChange = {
            password.value = it
        }, label = { Text("密码")})

        Button(onClick = {
//            ip = ipAddress.value
            coroutineScope.launch {
                HttpUtils().post<LoginResult>(
                    "/login" ,
                    mapOf("username" to username.value, "password" to password.value)
                ) { throwable ->
                    if (PlatFormUtils.isAndroid()) {
                        errorMsg.value = "Android Error: ${throwable.message}"
                    } else {
                        errorMsg.value = "Desktop Error: ${throwable.message}"
                    }
                }.collect {
                    when (it.state) {
                        0 -> {
                            errorMsg.value = "用户不存在"
                            println("$TAG: 用户不存在")
                        }
                        1 -> {
                            errorMsg.value = "密码不正确"
                            println("$TAG: 密码不正确")
                        }
                        2 -> {
                            println("$TAG: 登录成功")
                            page.value = Page.HomePage
                        }
                        3 -> {
                            errorMsg.value = "后端错误"
                            println("$TAG: 后端错误")
                        }
                    }
                }
            }
        }) {
            Text(text = "登录")
        }
    }

}


@Serializable
class LoginResult(
    /**
     * 0: 用户不存在
     * 1：密码错误
     * 2: 登录成功
     * 3: 后端异常
     */
    val state: Int? = null
)
