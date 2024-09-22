package utils

import PlatFormUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.xmlbeans.impl.store.Locale
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtils {

    suspend fun download2Excel(path: String, content: List<List<String>>, onSuccess: (String) -> Unit) = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            var originFilePath = if (PlatFormUtils.isAndroid()) {
                PlatFormUtils.androidDownloadPath() + "/$path"
            } else {
                path
            }
            originFilePath = originFilePath.split('.').first() + ".xlsx"
            val inputStream = FileInputStream(originFilePath)
            val outputStream = FileOutputStream(originFilePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 0 until content.size) {
                val row = content.get(i)
                for (j in 0 until row.size) {
                    sheet.getRow(i).getCell(j).setCellValue(content[i][j])
                    print("\t")
                }
                println()
            }

            workbook.write(outputStream)
            outputStream.flush()
            outputStream.close()
            workbook.close()
            onSuccess(path)
        }.getOrElse {
            println("download2Excel error：" + it.message)
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