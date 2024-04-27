import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import page.PageManager

@Composable
@Preview
fun App() {

    MaterialTheme {
        PageManager()

    }
}