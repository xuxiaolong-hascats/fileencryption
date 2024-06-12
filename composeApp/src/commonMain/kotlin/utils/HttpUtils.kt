package utils

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.content.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import page.DownloadState
import java.io.File
import java.io.FileOutputStream

var ip = "101.126.85.58"
fun baseUrl() = "http://$ip:8080"

class HttpUtils() {
    val httpClient = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
            }
        }
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
        }
        install(DefaultRequest) {
            url { baseUrl() }
        }
    }

    fun close() {
        httpClient.close()
    }

    inline fun <reified T> get(path: String): Flow<T> {
        return flow {
            val response = httpClient.get(baseUrl()+path)
            val result = response.body<T>()
            emit(result)
        }.catch { throwable: Throwable ->
            throw throwable
        }.onCompletion { cause ->
            close()
        }.flowOn(Dispatchers.IO)
    }


     @OptIn(InternalAPI::class)
     inline fun <reified T> post(path: String, params: Map<String, String> = emptyMap(), crossinline throwableHandler: (Throwable) -> Unit): Flow<T> {
        return flow {
            val response = httpClient.post(baseUrl()+path) {
                body = Gson().toJson(params)
                contentType(ContentType.Application.Json)
            }
            val result = response.body<T>()
            emit(result)
        }.catch { throwable: Throwable ->
            throwableHandler.invoke(throwable)
        }.onCompletion { cause ->
            close()
        }.flowOn(Dispatchers.IO)
    }

    inline fun <reified T> uploadExcelFile(toUserName: String, filename: String, crossinline throwableHandler: (Throwable) -> Unit): Flow<T> {
        return flow<T> {
            val response = httpClient.submitFormWithBinaryData(
                url = "${baseUrl()}/upload/$toUserName",
                formData = formData {
                    append("file", File(filename).readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Xlsx.toString())
                        append(HttpHeaders.ContentDisposition, "filename=${filename}")
                    })
                }
            ) {
                header(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
            }
            val result = response.body<T>()
            emit(result)
        }.catch { throwable: Throwable ->
            throwableHandler.invoke(throwable)
        }.onCompletion { cause ->
            close()
        }.flowOn(Dispatchers.IO)
    }

    inline fun <reified T> downloadFile(username: String, filename: String, downloadPath: String, crossinline onCompletion: (Throwable?) -> Unit): Flow<T> {
        val outputStream = FileOutputStream(File(downloadPath))
        return flow<T> {
            val response = httpClient.get("${baseUrl()}/download/?filename=$filename&username=$username")
            response.bodyAsChannel().toInputStream().copyTo(outputStream)
        }.catch { throwable: Throwable ->
            println("throwable ${throwable.message}")
        }.onCompletion { cause ->
            onCompletion(cause)
            outputStream.close()
            close()
        }.flowOn(Dispatchers.IO)
    }

}
