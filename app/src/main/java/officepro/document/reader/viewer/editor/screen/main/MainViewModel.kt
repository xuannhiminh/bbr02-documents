package officepro.document.reader.viewer.editor.screen.main

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import officepro.document.reader.viewer.editor.common.LoadingState
import officepro.document.reader.viewer.editor.common.PresKey
import officepro.document.reader.viewer.editor.common.SortState
import officepro.document.reader.viewer.editor.database.repository.FileModelRepository
import officepro.document.reader.viewer.editor.model.FileModel
import officepro.document.reader.viewer.editor.utils.FileUtils
import officepro.document.reader.viewer.editor.utils.RenameStatus
import com.ezteam.baseproject.EzListener
import com.ezteam.baseproject.utils.DateUtils
import com.ezteam.baseproject.utils.PathUtils
import com.ezteam.baseproject.utils.PreferencesUtils
import com.ezteam.baseproject.utils.TemporaryStorage
import com.ezteam.baseproject.utils.permisson.PermissionUtils
import com.ezteam.baseproject.viewmodel.BaseViewModel
import officepro.document.reader.viewer.editor.common.BottomTab
import officepro.document.reader.viewer.editor.common.FileTab
import officepro.document.reader.viewer.editor.screen.base.CurrentStatusAdsFiles
import officepro.document.reader.viewer.editor.utils.FileSaveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel(
    application: Application,
    var repository: FileModelRepository
) : BaseViewModel(application) {

    private val KEY_IMPORTED_PATH = "imported_file_path"
    val sortState = PreferencesUtils.getInteger(PresKey.SORT_STATE, 4)
    var loadingObservable: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.IDLE)
    var searchCharObservable: MutableLiveData<String> = MutableLiveData("")
    var sortStateObservable: MutableLiveData<SortState> = MutableLiveData(SortState.getSortState(sortState))


    private val _currentBottomTab = MutableLiveData(BottomTab.HOME)
    val currentBottomTab: LiveData<BottomTab> = _currentBottomTab
    private val _currentFileTab = MutableLiveData(FileTab.ALL_FILE)
    val currentFileTab: LiveData<FileTab> = _currentFileTab

    private val _currentAdsFilesStatus = MutableLiveData(CurrentStatusAdsFiles(true, null))
    val currentAdsFilesStatus: MutableLiveData<CurrentStatusAdsFiles> = _currentAdsFilesStatus

    fun updateAdsFilesStatus (currentStatusAdsFiles: CurrentStatusAdsFiles) {
        Log.d("MainViewModel", "fupdateAdsFilesStatus")
      //  _currentAdsFilesStatus.value = currentStatusAdsFiles
    }

    fun updateFileTab(tab: FileTab) {
        Log.d("MainViewModel", "updateFileTab: $tab")
        _currentFileTab.value = tab
    }

    fun updateBottomTab(tab: BottomTab) {
        Log.d("MainViewModel", "updateBottomTab: $tab")
        _currentBottomTab.value = tab
    }
    // 1) ALL files
    val allFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentFile()
            BottomTab.FAVORITE -> getListFavoriteFile()
            BottomTab.HOME     -> getListAllFile()
            else               -> getListAllFile()
        }
    }

    // 2) PDF files
    val pdfFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentPdfFile()
            BottomTab.FAVORITE -> getListFavoritePdfFile()
            BottomTab.HOME     -> getListPdfFile()
            else               -> getListPdfFile()
        }
    }

    // 3) WORD files
    val wordFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentWordFile()
            BottomTab.FAVORITE -> getListFavoriteWordFile()
            BottomTab.HOME     -> getListWordFile()
            else               -> getListWordFile()
        }
    }

    // 4) PPT files
    val pptFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentPptFile()
            BottomTab.FAVORITE -> getListFavoritePptFile()
            BottomTab.HOME     -> getListPptFile()
            else               -> getListPptFile()
        }
    }

    // 5) EXCEL files
    val excelFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentExcelFile()
            BottomTab.FAVORITE -> getListFavoriteExcelFile()
            BottomTab.HOME     -> getListExcelFile()
            else               -> getListExcelFile()
        }
    }

    // 5) TXT files
    val txtFilesLiveData: LiveData<List<FileModel>> = _currentBottomTab.switchMap { tab ->
        when (tab) {
            BottomTab.RECENT   -> getListRecentTxtFile()
            BottomTab.FAVORITE -> getListFavoriteTxtFile()
            BottomTab.HOME     -> getListTxtFile()
            else               -> getListTxtFile()
        }
    }

//    val filesLiveData: LiveData<List<FileModel>> =
//        _currentBottomTab.switchMap { bottomTab ->
//            _currentFileTab.switchMap { type ->
//                liveData(Dispatchers.IO) {
//                    // pick the exact repository call:
//                    val source: LiveData<List<FileModel>> = when (bottomTab) {
//                        BottomTab.RECENT -> when (type) {
//                            FileTab.ALL_FILE   -> repository.getRecentAllFiles()
//                            FileTab.PDF   -> repository.getRecentPdfFiles()
//                            FileTab.WORD  -> repository.getRecentWordFiles()
//                            FileTab.PPT   -> repository.getRecentPptFiles()
//                            FileTab.EXCEL -> repository.getRecentExcelFiles()
//                            else -> repository.getRecentAllFiles()
//                        }
//                        BottomTab.FAVORITE -> when (type) {
//                            FileTab.ALL_FILE   -> repository.getFavoriteAllFiles()
//                            FileTab.PDF   -> repository.getFavoritePdfFiles()
//                            FileTab.WORD  -> repository.getFavoriteWordFiles()
//                            FileTab.PPT   -> repository.getFavoritePptFiles()
//                            FileTab.EXCEL -> repository.getFavoriteExcelFiles()
//                            else  -> repository.getFavoriteAllFiles()
//
//                        }
//                        BottomTab.HOME -> when (type) {
//                            FileTab.ALL_FILE   -> getListAllFile()
//                            FileTab.PDF   -> getListPdfFile()
//                            FileTab.WORD  -> getListWordFile()
//                            FileTab.PPT   -> getListPptFile()
//                            FileTab.EXCEL -> getListExcelFile()
//                            else  -> getListAllFile()
//                        }
//                    }
//                    emitSource(source)
//                }
//            }
//        }


    private val _recentFiles = MutableLiveData<List<FileModel>>()
    val recentFiles: LiveData<List<FileModel>> = _recentFiles

    fun loadRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = repository.getLatestFiles()
            _recentFiles.postValue(files)
        }
    }

    // LiveData for files sorted by DATE_TODAY
    val todayFilesLiveData: LiveData<List<FileModel>> = sortStateObservable.switchMap { sortState ->
        liveData(Dispatchers.IO) {
            if (sortState == SortState.DATE_TODAY) {
                emitSource(repository.getAllFiles(sortState))
            } else {
                emit(emptyList())
            }
        }
    }

    // When the file type changes, switch the LiveData source to get the correct count.
    val loadAddedTodayFiles: LiveData<Int> = _currentFileTab.switchMap { type ->
        liveData(Dispatchers.IO) {
            val condition: String = when (type) {
                FileTab.ALL_FILE -> "ALL"
                FileTab.PDF -> "PDF"
                FileTab.WORD -> "WORD"
                FileTab.PPT -> "PPT"
                FileTab.EXCEL -> "EXCEL"
                FileTab.TXT -> "TXT"
                else -> "ALL"
            }
            emitSource(repository.getNumberOfTodayAddedFile(condition))
        }
    }
    fun loadTotalFiles(type: String): LiveData<Int> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getNumberOfTotalFile(type))
        }
    }
    fun loadTotalRecentFiles(): LiveData<Int> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getTotalRecentFiles())
        }
    }
    fun loadFavoriteFiles(): LiveData<Int> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getTotalFavoriteFiles())
        }
    }


    private fun Context.getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return when (ext) {
            "pdf" -> "application/pdf"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> {
                val fromMap = android.webkit.MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(ext)
                fromMap ?: "application/octet-stream"
            }
        }
    }

    private fun copyPdfFromAssetsToDownloads(fileName: String): FileModel? {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir == null) return null;
            val destFile = File(downloadsDir, fileName)
            if (destFile.exists()) {
                return FileModel.getInstanceFromUrl(destFile.path)
            }
            getApplication<Application>().applicationContext.assets.open(fileName).use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaScannerConnection.scanFile(
                    getApplication<Application>().applicationContext,
                    arrayOf(destFile.absolutePath),
                    arrayOf( getApplication<Application>().applicationContext.getMimeType(destFile)),
                    null
                )
            } else {
                getApplication<Application>().applicationContext.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(destFile)
                    )
                )
            }
            return FileModel.getInstanceFromUrl(destFile.path)
        } catch (e: IOException) {
           // E.printStackTrace()
            return null
        }
    }



    private fun copyPdfFromAssetsToInternalDir(fileName: String): FileModel? {
        val file = File(File(getApplication<Application>().applicationContext.filesDir, "defaultFiles").apply { if(!exists()) mkdirs() }, fileName)

        if (file.exists()) return FileModel.getInstanceFromUrl(file.path)

        return try {
            getApplication<Application>().applicationContext.assets.open(fileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            FileModel.getInstanceFromUrl(file.path)
        } catch (e: IOException) {
            Log.e("MainViewModel", "Error copying file from assets to internal dir: ${e.message}")
            //e.printStackTrace()
            null
        }
    }

    private fun isAcceptManagerStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            PermissionUtils.checkPermissonAccept(
                getApplication<Application>().applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun migrateFileDataViewModelScope() {
        GlobalScope.launch(Dispatchers.IO) {
            migrateFileData()
        }
    }
    suspend fun  migrateFileData() = withContext(Dispatchers.IO) {
        loadingObservable.postValue(LoadingState.START)
        val timeStart = System.currentTimeMillis()
        Log.d("File", "Start Scan")
        FileUtils.checkFolderSystem(getApplication())
        Log.d("File", "Scan checkFolderSystem done in ${System.currentTimeMillis() - timeStart} ms")

        val fileModels   = repository.getAllFiles()
        val fileInDevice = FileUtils.getFileListByExtensions(getApplication())
        Log.d("File", "Scan getFileListByExtensions done in ${System.currentTimeMillis() - timeStart} ms")


        Log.d("File", "Stop Scan")

        repository.mergeFileModel(fileInDevice, fileModels)
        repository.deleteAll()
        repository.insert(fileInDevice)
        loadingObservable.postValue(LoadingState.FINISH)
        Log.i("File", "Scan migrateFileData done in ${System.currentTimeMillis() - timeStart} ms")

        Log.d("File", "Db: ${fileModels.size}")
        Log.d("File", "InDevice: ${fileInDevice.size}")

        // move addSameFiles() here if it belongs to "migration"
        Log.d("File", "Scan migrateFileData done")

        if (isAcceptManagerStorage()) {
            addSameFilesExternal()
        }

    }

    fun addSameFilesInternal() {
        val sampleFiles = listOf("file_example_PDF.pdf", "file_example_DOC.doc", "file_example_PPT.ppt", "file_example_XLS.xls")
        var newFiles = sampleFiles.mapNotNull { fileName -> copyPdfFromAssetsToInternalDir(fileName) }
        var time = 966326400000 // 15082000150000
        newFiles.forEach { fileModel ->
            fileModel.isSample = true
            fileModel.timeAdd = time
            fileModel.date = time--
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(newFiles)
        }
    }

    fun addSameFilesExternal() {
        val sampleFiles = listOf("file_example_PDF.pdf", "file_example_DOC.doc", "file_example_PPT.ppt", "file_example_XLS.xls")
        TemporaryStorage.isSavingFileNotNoti = true
        var newFiles = sampleFiles.mapNotNull { fileName ->
            copyPdfFromAssetsToDownloads(fileName)
        }
        TemporaryStorage.isSavingFileNotNoti = false

        var time = 966326400000 // 15082000150000
        newFiles.forEach { fileModel ->
            fileModel.isSample = true
            fileModel.timeAdd = time
            fileModel.date = time--
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(newFiles)
        }
    }

    public fun getListAllFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getAllFiles(sortState))
            }
        }
    }

    public fun getListFileBaseOnFileTab(fileTab: FileTab) :  LiveData<List<FileModel>> {
        return when (fileTab) {
            FileTab.ALL_FILE -> getListAllFile()
            FileTab.PDF -> getListPdfFile()
            FileTab.WORD -> getListWordFile()
            FileTab.PPT -> getListPptFile()
            FileTab.EXCEL -> getListExcelFile()
            FileTab.TXT -> getListTxtFile()
        }
    }


    public fun getCurrentFiles(fileTab: FileTab) :  LiveData<List<FileModel>> {
        return when (fileTab) {
            FileTab.ALL_FILE -> allFilesLiveData
            FileTab.PDF -> pdfFilesLiveData
            FileTab.WORD -> wordFilesLiveData
            FileTab.PPT -> pptFilesLiveData
            FileTab.EXCEL -> excelFilesLiveData
            FileTab.TXT -> txtFilesLiveData
        }
    }

    private fun getListPdfFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getPdfFiles(sortState))
            }
        }
    }
    private fun getListExcelFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getExcelFiles(sortState))
            }
        }
    }
    private fun getListTxtFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getTxtFiles(sortState))
            }
        }
    }
    private fun getListWordFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            repository.getWordFiles(sortState)
        }
    }
    private fun getListPptFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getPptFiles(sortState))
            }
        }
    }

    fun getListSearchFile(): LiveData<List<FileModel>> {
        return searchCharObservable.switchMap { searchChar: String ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getAllFiles(searchChar))
            }
        }
    }

     fun getListRecentFile(): LiveData<List<FileModel>> {
         return sortStateObservable.switchMap { sortState: SortState ->
             liveData(Dispatchers.IO) {
                 emitSource(repository.getRecentAllFiles(sortState))
             }
         }
    }

     fun getListRecentPdfFile(): LiveData<List<FileModel>> {
         return liveData(Dispatchers.IO) {
             emitSource(repository.getRecentPdfFiles())
         }
    }

     fun getListRecentWordFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getRecentWordFiles())
        }
    }

     fun getListRecentPptFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getRecentPptFiles())
        }
    }

     fun getListRecentExcelFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getRecentExcelFiles())
        }
    }
    fun getListRecentTxtFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getRecentTxtFiles())
        }
    }

    suspend fun getLatestFile(): List<FileModel> {
        return repository.getLatestFiles()
    }

    fun getListFavoriteFile(): LiveData<List<FileModel>> {
        return sortStateObservable.switchMap { sortState: SortState ->
            liveData(Dispatchers.IO) {
                emitSource(repository.getFavoriteAllFiles(sortState))
            }
        }
    }

     fun getListFavoritePdfFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getFavoritePdfFiles())
        }
    }

     fun getListFavoriteWordFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getFavoriteWordFiles())
        }
    }

    fun getListFavoritePptFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getFavoritePptFiles())
        }
    }

     fun getListFavoriteExcelFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getFavoriteExcelFiles())
        }
    }
    fun getListFavoriteTxtFile(): LiveData<List<FileModel>> {
        return liveData(Dispatchers.IO) {
            emitSource(repository.getFavoriteTxtFiles())
        }
    }

    private fun getFileByPath(path: String): FileModel? {
        return repository.getFileByPath(path)
    }

    fun reactRecentFile(fileModel: FileModel, isAdd: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            if (fileModel.fromDatabase) {
                fileModel.isRecent = isAdd
                fileModel.timeRecent = if (isAdd) DateUtils.getCurrentDate() else ""
                fileModel.unixTimeRecent = if (isAdd) System.currentTimeMillis() else 0
                repository.insert(fileModel)
            }
        }
    }

    fun reactFavorite(fileModel: FileModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(fileModel)
        }
    }


    fun renameFile(fileModel: FileModel, newName: String, onSuccess: ((FileModel) -> Unit)? = null, onFail: ((RenameStatus) -> Unit)? = null) {
        val fileFrom = File(fileModel.path)
        FileUtils.rename(getApplication(), fileFrom, newName+"."+fileFrom.extension, {
            when(it) {
                RenameStatus.FAIL, RenameStatus.EXISTS -> {
                    onFail?.invoke(it)
                }
                else -> { }
            }
        }, {
            viewModelScope.launch(Dispatchers.IO) {
                repository.delete(fileModel)
                fileModel.name = newName
                fileModel.path = it
                repository.insert(fileModel)
                onSuccess?.invoke(fileModel)
            }
        })
    }

    fun deleteFile(fileModel: FileModel, listener: (() -> Unit?)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(fileModel)
            fileModel.file.delete()
            FileUtils.scanFile(getApplication(), fileModel.path, object : EzListener {
                override fun onListener() {
                    listener?.let {
                        it()
                    }
                }
            })
        }
    }
    fun deleteFiles(fileModels: List<FileModel>, listener: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(fileModels) // Xóa dữ liệu trong repository

            fileModels.forEach { fileModel ->
                fileModel.file.delete() // Xóa file từ bộ nhớ
                FileUtils.scanFile(getApplication(), fileModel.path, object : EzListener {
                    override fun onListener() {
                        listener?.invoke() // Gọi callback sau khi quét xong
                    }
                })
            }
        }
    }

    fun deleteAllRecent() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRecent()
        }
    }

    fun deleteAllFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllFavorite()
        }
    }

    fun sortFile(state: SortState) {
        PreferencesUtils.putInteger(PresKey.SORT_STATE, state.value)
        sortStateObservable.postValue(state)
    }

    private fun searchFileFromCursor(uri: Uri?): String {
        val fileName = PathUtils.getFileName(getApplication(), uri).substringBeforeLast(".")
        if (fileName.isEmpty()) {
            return ""
        }
        val fileModels: List<FileModel> = FileUtils.getPdfFileList(getApplication())
        for (fileModel in fileModels) {
            if (fileModel.name.equals(fileName)) {
                return fileModel.path
            }
        }
        return ""
    }

    suspend fun searchFileFromDict(uri: Uri?): String {
        return withContext(viewModelScope.coroutineContext) {
            val fileName = PathUtils.getFileName(getApplication(), uri)
            return@withContext if (fileName == null || fileName.isEmpty()) {
                ""
            } else FileUtils.searchPathInFolder(getApplication(), fileName)
        }
    }

    fun getPathFromUri(uri: Uri?): String {
        // Path from path utils
        val pathParse: String? =
            PathUtils.getPath(getApplication(), uri)

        pathParse?.let {
            if (it.isNotEmpty()) {
                Log.d("File", "Path: From path utils")
                return it
            }
        }

        // Path from cursor
//        val pathFromCursor = searchFileFromCursor(uri)
//        if (pathFromCursor.isNotEmpty()) {
//            Log.d("File", "Path: From cursor")
//            return pathFromCursor
//        }

        return ""
    }

    fun searchFileFromDict(uri: Uri?, success: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pathFromDict = searchFileFromDict(uri)
            if (pathFromDict.isNotEmpty()) {
                Log.d("File", "Path: From dict")
                success(pathFromDict)
            }
        }
    }


    suspend fun getFileModelByPath(path: String) : FileModel  = withContext(Dispatchers.IO){
        val fileModel = getFileByPath(path)
        fileModel?.let {
            Log.d("File", "Open from database")
            return@let it
        } ?: kotlin.run {
            val tempFileModel = FileModel()
            tempFileModel.path = path
            tempFileModel.name = PathUtils.getFileName(getApplication(), Uri.parse(path))
            tempFileModel.fromDatabase = false
            Log.d("File", "Open from path")
            return@run tempFileModel
        }
    }

    suspend fun checkIfOurAppRecognizeThisFile(path: String): Boolean = withContext(Dispatchers.IO) {
        getFileByPath(path) != null
    }


    fun createFile(path: String, onSuccess: ((FileModel) -> Unit)?) {
        viewModelScope.launch(Dispatchers.IO) {
            FileUtils.scanFile(getApplication(), path, null)
            val file = File(path)
            if (file.exists()) {
                val model = FileModel()
                model.path = path
                model.name  = FileUtils.getFileName(path).substringBeforeLast(".")
                model.date = file.lastModified()
                model.size = (file.length()/1024).toString()
                model.fromDatabase = false
                repository.insert(model)
                onSuccess?.invoke(model)
            }
        }
    }

    fun updateReadingStatus(fileModel: FileModel, readDone: Boolean, currentPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            fileModel.isReadDone = readDone
            fileModel.currentPage = currentPage
            repository.insert(fileModel)
        }
    }

    fun setNotRecently(fileModels: List<FileModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setNotRecently(fileModels)
        }
    }
    fun removeFavouriteFiles(fileModels: List<FileModel>, listener: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.removeFavourite(fileModels)
            }
        }
    }

    /**
     * Import a file URI into internal storage (filesDir), persist its path.
     * Call this from SplashActivity (with `lifecycleScope.launch`)
     */
//    suspend fun importUriToInternal(uri: Uri): FileModel? = withContext(Dispatchers.IO) {
//        val context = getApplication<Application>()
//
//        // Query display name
//        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            if (cursor.moveToFirst() && nameIndex != -1) {
//                cursor.getString(nameIndex)
//            } else "imported_file"
//        } ?: "imported_file"
//
//        // Target file in internal storage
//        val documentsDir = File(context.filesDir, "DocumentsFromOutside").apply { mkdirs() }
//        val target = File(documentsDir, name)
//
//        try {
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                FileOutputStream(target).use { output ->
//                    input.copyTo(output)
//                }
//            }
//
//            // Persist absolute path (other Activities can load it later)
//            PreferencesUtils.putString(KEY_IMPORTED_PATH, target.absolutePath)
//
////            _importedFilePath.postValue(target.absolutePath)
//
//
//            FileModel().apply {
//                    path = target.absolutePath
//                    this.name = target.name.substringBeforeLast(".")
//                    date = target.lastModified()
//                    size = (target.length() / 1024).toString()
//                    fromDatabase = false
//                    isReadDone = false
//                    repository.insert(this)
//                }
//
//        } catch (e: Exception) {
//            Log.e("SplashViewModel", "Failed to import file", e)
//            null
//        }
//    }

    suspend fun importUriToDownloadAllPDFTripSoft(uri: Uri): FileModel? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()

        val file = FileSaveManager.saveUriToDownloads(context, uri) ?: return@withContext null

        return@withContext file.let {
            FileModel().apply {
                path = it.absolutePath
                name = it.name.substringBeforeLast(".")
                date = it.lastModified()
                size = (it.length() / 1024).toString()
                fromDatabase = false
                repository.insert(this)
            }
        }
    }

    /**
     * Retrieve the file path stored from a previous import
     */
    fun getImportedFileToOpenFromOtherApp(): File? {
        val context = getApplication<Application>()
        val path = PreferencesUtils.getString(KEY_IMPORTED_PATH, null)
        return path?.let { File(it) }?.takeIf { it.exists() }
    }


}