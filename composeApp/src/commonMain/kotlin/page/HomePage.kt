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
import utils.FileUtils
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import utils.HttpUtils
import utils.originPath

@Composable
fun HomePage(username: String, page: MutableState<Page>) {

    if (PlatFormUtils.isAndroid()) {
        HomePageForAndroid(username, page)
    } else {
        HomePageForDeskTop(username, page)
    }
}
