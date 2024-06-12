package page

import PlatFormUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

//        val ipAddress = remember {
//            mutableStateOf("")
//        }

        val password = remember {
            mutableStateOf("")
        }

//        OutlinedTextField(value = ipAddress.value, onValueChange = {
//            ipAddress.value = it
//        }, label = { Text(text = "服务器ip")})
//

        OutlinedTextField(value = username.value, onValueChange = {
            username.value = it
        }, label = { Text(text = "用户名")})

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
                        password.value = "Android Error: ${throwable.message}"
                    } else {
                        password.value = "Desktop Error: ${throwable.message}"
                    }
                }.collect {
                    when (it.state) {
                        0 -> {
                            password.value = "用户不存在"
                            println("$TAG: 用户不存在")
                        }
                        1 -> {
                            password.value = "密码不正确"
                            println("$TAG: 密码不正确")
                        }
                        2 -> {
                            println("$TAG: 登录成功")
                            page.value = Page.HomePage
                        }
                        3 -> {
                            password.value = "后端错误"
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
