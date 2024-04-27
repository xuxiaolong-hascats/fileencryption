package page

import androidx.compose.runtime.*

@Composable
fun PageManager() {
    val currentPage = remember { mutableStateOf(Page.LoginPage) }
    val username = remember { mutableStateOf("") }
    when (currentPage.value) {
        Page.LoginPage -> {
            LoginPage(username,currentPage)
        }
        Page.HomePage -> {
            HomePage(username.value, currentPage)
        }
    }
}

enum class Page {
    LoginPage,
    HomePage
}

