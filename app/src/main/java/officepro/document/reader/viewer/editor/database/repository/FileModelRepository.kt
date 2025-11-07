package officepro.document.reader.viewer.editor.database.repository

import androidx.lifecycle.LiveData
import officepro.document.reader.viewer.editor.common.SortState
import officepro.document.reader.viewer.editor.model.FileModel

interface FileModelRepository {
    suspend fun insert(fileModel: FileModel)

    suspend fun insert(fileModels: List<FileModel>)

    suspend fun delete(fileModel: FileModel)

    suspend fun delete(fileModels: List<FileModel>)

    suspend fun removeFavourite(fileModels: List<FileModel>)

    suspend fun setNotRecently(fileModels: List<FileModel>)

    suspend fun deleteAll()

    suspend fun deleteAllRecent()

    suspend fun deleteAllFavorite()

     fun getNumberOfTodayAddedFile(text: String): LiveData<Int>
     fun getNumberOfTotalFile(text: String): LiveData<Int>
     fun getTotalRecentFiles(): LiveData<Int>
     fun getTotalFavoriteFiles(): LiveData<Int>

    fun getFileByPath(path: String): FileModel?

    fun getAllFiles(textSearch: String): LiveData<List<FileModel>>

    fun getAllFiles(sortState: SortState): LiveData<List<FileModel>>

    fun getPdfFiles(sortState: SortState): LiveData<List<FileModel>>

    fun getWordFiles(sortState: SortState): LiveData<List<FileModel>>

    fun getExcelFiles(sortState: SortState): LiveData<List<FileModel>>
    fun getTxtFiles(sortState: SortState): LiveData<List<FileModel>>

    fun getPptFiles(sortState: SortState): LiveData<List<FileModel>>

    suspend fun getAllFiles(): List<FileModel>
    suspend fun getOldestUnreadFile( currentTime: Long, minDaysInMillis: Long): FileModel?

    suspend fun getRecentAllFiles(sortState: SortState): LiveData<List<FileModel>>

    suspend fun getRecentPdfFiles(): LiveData<List<FileModel>>

    suspend fun getRecentExcelFiles(): LiveData<List<FileModel>>

    suspend fun getRecentTxtFiles(): LiveData<List<FileModel>>
    suspend fun getRecentPptFiles(): LiveData<List<FileModel>>

    suspend fun getRecentWordFiles(): LiveData<List<FileModel>>

    suspend fun getFavoriteAllFiles(sortState: SortState): LiveData<List<FileModel>>

    suspend fun getFavoritePdfFiles(): LiveData<List<FileModel>>

    suspend fun getFavoriteWordFiles(): LiveData<List<FileModel>>

    suspend fun getFavoritePptFiles(): LiveData<List<FileModel>>

    suspend fun getFavoriteExcelFiles(): LiveData<List<FileModel>>

    suspend fun getFavoriteTxtFiles(): LiveData<List<FileModel>>

    suspend fun getLatestFiles (): List<FileModel>

    fun mergeFileModel(fileInDevice: List<FileModel>, fileModels: List<FileModel>)
}