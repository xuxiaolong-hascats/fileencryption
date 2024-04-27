package utils

import PlatFormUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.xmlbeans.impl.store.Locale
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtils {

    suspend fun encryptExcelAndSave(path: String, encryptColumns: List<String>) = withContext(Dispatchers.IO) {
        return@withContext runCatching<String?> {
            val originFilePath = if (PlatFormUtils.isAndroid()) {
                PlatFormUtils.androidDownloadPath() + "/$path"
            } else {
                path
            }
            Locale
            val inputStream = FileInputStream(originFilePath)
            val encryptFilePath = path.addEncryptToPath()
            println("encryptExcelAndSave $encryptFilePath")
            val outputStream = FileOutputStream(encryptFilePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.physicalNumberOfRows
            val headerIndex = sheet.getRow(0).mapIndexedNotNull { index, cell ->
                if (cell.stringCellValue in encryptColumns) {
                    index
                } else {
                    null
                }
            }

            for (i in 1 until rows) { //  除了标题都加密
                val row = sheet.getRow(i)
                headerIndex.forEach {
                    val cell = row.getCell(it)
                    print("${cell}\t")
                    cell.setCellValue(AESCrypt.encrypt(cell.stringCellValue, "1234567887654321"))
                    print("${cell}\t")
                }
                println()
            }
            workbook.write(outputStream)
            outputStream.flush()
            outputStream.close()
            workbook.close()
            encryptFilePath
        }.getOrElse {
            println("encryptExcelAndSave error：" + it.message)
            null
        }

    }

    suspend fun decryptExcelAndSave(path: String, decryptColumns: List<String>) = withContext(Dispatchers.IO) {
        return@withContext runCatching<Boolean> {
            val inputStream = FileInputStream(path)
            val decryptFilePath = path.addDecryptToPath()
            val outputStream = FileOutputStream(decryptFilePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.physicalNumberOfRows
            val headerIndex = sheet.getRow(0).mapIndexedNotNull { index, cell ->
                if (cell.stringCellValue in decryptColumns) {
                    index
                } else {
                    null
                }
            }

            for (i in 1 until rows) { //  除了标题都加密
                val row = sheet.getRow(i)
                headerIndex.forEach {
                    val cell = row.getCell(it)
                    print("${cell}\t")
                    cell.setCellValue(AESCrypt.decrypt(cell.stringCellValue, "1234567887654321"))
                    print("${cell}\t")
                }
                println()
            }
            workbook.write(outputStream)
            outputStream.flush()
            outputStream.close()
            workbook.close()
            true
        }.getOrElse {
            println(it.message)
            false
        }

    }




    suspend fun getExcelColumns(path: String) = withContext(Dispatchers.IO) {
        val result = mutableListOf<String>()
        return@withContext runCatching<List<String>> {
            val inputStream = FileInputStream(path)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val row = sheet.getRow(0)
            val cols = row.physicalNumberOfCells
            for (j in 0 until cols) {
                result.add(row.getCell(j).stringCellValue)
            }
            result
        }.getOrElse {
            result
        }
    }

}

fun String.addEncryptToPath(): String{
    return if (PlatFormUtils.isAndroid()){
        PlatFormUtils.androidDownloadPath() + "/encrypt_$this"
    } else {
        "encrypt_$this"
    }
}

fun String.addDecryptToPath(): String{
    return if (PlatFormUtils.isAndroid()){
        PlatFormUtils.androidDownloadPath() + "/decrypt_$this"
    } else {
        "decrypt_$this"
    }
}

fun String.originPath(): String{
    return if (this.startsWith("encrypt_") || this.startsWith("decrypt_")){
        this.substringAfter('_')
    } else {
        this
    }
}