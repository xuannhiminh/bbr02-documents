package com.ezteam.ezpdflib.viewmodel

import android.app.Application
import android.graphics.PointF
import android.net.Uri
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ezstudio.pdftoolmodule.utils.pdftool.Watermark
import com.ezstudio.pdftoolmodule.utils.pdftool.WatermarkModel
import com.ezteam.ezpdflib.activity.Mode
import com.ezteam.ezpdflib.activity.signature.PDSPDFDocument
import com.ezteam.ezpdflib.activity.signature.SignatureUtils
import com.ezteam.ezpdflib.database.DatabaseService
import com.ezteam.ezpdflib.database.repository.FileRepository
import com.ezteam.ezpdflib.model.*
import com.ezteam.ezpdflib.util.Config
import com.ezteam.ezpdflib.util.FileSaveManager
import com.ezteam.ezpdflib.util.PdfUtils
import com.ezteam.ezpdflib.util.PreferencesUtils
import com.ezteam.nativepdf.Annotation
import com.ezteam.nativepdf.MuPDFCore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

@Keep
class DetailViewmodel(application: Application) : AndroidViewModel(application) {

    var compositeDisposable = CompositeDisposable()
    val scope = CoroutineScope(Dispatchers.IO)
    var coroutineFunction: CoroutineFuntion = CoroutineFuntion(scope, application)
    var isLoading = MutableLiveData(false)
    var currentPage = MutableLiveData<Int>()
    var mode = MutableLiveData(Mode.Normal)
    var lastIndexSearch: Int? = null
    var uriSearch: Uri? = null
    var listbookmark = MutableLiveData(mutableListOf<Bookmark>())
    var mapUriPage = hashMapOf<Int, Uri>()
    var pageLoadedFinish: ((Boolean) -> Unit)? = null
    var pageSliderRes = MutableLiveData(0)
    var currentZoom: Float = 1f

    var path = MutableLiveData("")
    private val repository: FileRepository by lazy {
        FileRepository(DatabaseService.getInstance(application).fileDao())
    }

    fun getPageSliderRes(): Int {
        return pageSliderRes.value?.let {
            kotlin.math.max(it, 0)
        } ?: 0
    }

    fun getCurrentPage(): Int {
        return currentPage.value?.let {
            kotlin.math.max(it, 0)
        } ?: 0
    }

    fun convertDisplayPage(page: Int, lstIndexAds: MutableList<Int>?): Int {
        if (lstIndexAds.isNullOrEmpty()) {
            return page
        }
        return page + lstIndexAds.filter {
            it <= page
        }.size
    }

    fun convertSavePage(page: Int, lstIndexAds: MutableList<Int>?): Int {
        if (lstIndexAds.isNullOrEmpty()) {
            return page
        }
        return page - lstIndexAds.filter {
            it <= page
        }.size
    }

    fun getAnnotation(muPDFCore: MuPDFCore, indexPage: Int?) {
        scope.launch(Dispatchers.IO) {
            indexPage?.let {
                SingleAnnotation.getInstance().annotation = muPDFCore.getAnnoations(it)
                val point = muPDFCore.getPageSize(it)
                SingleSize.getInstance().pointX = point.x
                SingleSize.getInstance().pointY = point.y

                val ratio = point.x / point.y
                val sizeDrawX: Float
                val sizeDrawY: Float
                if (ratio < 1.0f) {
                    sizeDrawY = SingleSize.getInstance().screenH.toFloat()
                    sizeDrawX = sizeDrawY * ratio
                } else {
                    sizeDrawX = SingleSize.getInstance().screenW.toFloat()
                    sizeDrawY = sizeDrawX / ratio
                }
                SingleSize.getInstance().pageWidth = sizeDrawX
                SingleSize.getInstance().pageHeight = sizeDrawY
            }
        }
    }

    fun getTextPage(muPDFCore: MuPDFCore, indexPage: Int?) {
        scope.launch(Dispatchers.IO) {
            indexPage?.let {
                SingleTextword.getInstance().textword = muPDFCore.textLines(it)
            }
        }
    }

    fun markupSelection(
        quadPoints: ArrayList<PointF>?,
        muPDFCore: MuPDFCore,
        indexPage: Int?,
        success: (Int, Uri) -> Unit
    ) {
        val type = when (mode.value) {
            Mode.HighLight -> {
                Annotation.Type.HIGHLIGHT
            }
            Mode.Unline -> {
                Annotation.Type.UNDERLINE
            }
            Mode.Strikeout -> {
                Annotation.Type.STRIKEOUT
            }
            else -> {
                null
            }
        }
        scope.launch(Dispatchers.IO) {
            indexPage?.let {
                quadPoints?.toTypedArray()?.let { arrPoints ->
                    type?.let { mode ->
                        if (arrPoints.isNotEmpty()) {
                            val arrColor =
                                PreferencesUtils
                                    .getAnnotation(Config.getPreferencesKeyByMode(this@DetailViewmodel.mode.value))
                                    .getColorPushNative()
                            muPDFCore.addMarkupAnnotation(
                                indexPage,
                                arrPoints,
                                mode,
                                arrColor[0],
                                arrColor[1],
                                arrColor[2]
                            )
                        }
                        Thread.sleep(200)
                        coroutineFunction.updatePage(muPDFCore, indexPage, currentZoom) { page, uri ->
                            getAnnotation(muPDFCore, page)
                            scope.launch(Dispatchers.Main) {
                                success(page, uri)
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteAnnotation(
        muPDFCore: MuPDFCore,
        indexPage: Int?,
        indexAnnotation: Int?,
        success: (Int, Uri) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            indexAnnotation?.let {
                indexPage?.let {
                    muPDFCore.deleteAnnotation(indexPage, indexAnnotation)
                    Thread.sleep(200)
                    coroutineFunction.updatePage(muPDFCore, indexPage, currentZoom) { page, uri ->
                        getAnnotation(muPDFCore, page)
                        scope.launch(Dispatchers.Main) {
                            success(page, uri)
                        }
                    }
                }
            }
        }
    }

    fun saveDraw(
        muPDFCore: MuPDFCore,
        drawing: ArrayList<ArrayList<PointF>>?,
        paintDraw: ArrayList<AnnotationValue>?,
        indexPage: Int?,
        success: (Int, Uri) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            indexPage?.let {
                drawing?.let { it ->
                    for (i in it.indices) {
                        val path: Array<Array<PointF>?> = arrayOfNulls(1)
                        val arc: ArrayList<PointF> = it[i]
                        path[0] = arc.toTypedArray()
                        paintDraw?.let { value ->
                            val arrColor = value[i].getColorPushNative()
                            muPDFCore.addInkAnnotation(
                                indexPage,
                                path,
                                arrColor[0],
                                arrColor[1],
                                arrColor[2],
                                value[i].getThicknessPushNative()
                                        * (SingleSize.getInstance().pointX / SingleSize.getInstance().pageWidth)
                            )
                        } ?: run {
                            muPDFCore.addInkAnnotation(
                                indexPage,
                                path,
                                0.0f,
                                1.0f,
                                0.0f,
                                10.0f
                                        * (SingleSize.getInstance().pointX / SingleSize.getInstance().pageWidth)
                            )
                        }
                    }
                }
                Thread.sleep(200)
                coroutineFunction.updatePage(muPDFCore, it, currentZoom) { page, uri ->
                    getAnnotation(muPDFCore, page)
                    scope.launch(Dispatchers.Main) {
                        success(page, uri)
                    }
                }
            }
        }
    }

    fun saveInternal(muPDFCore: MuPDFCore, success: (Unit) -> Unit) {
        isLoading.postValue(true)
        scope.launch(Dispatchers.IO) {
            muPDFCore.save()
            scope.launch(Dispatchers.Main) {
                isLoading.postValue(false)
                success(Unit)
            }
        }
    }

    fun searchText(
        text: String,
        direction: Int,
        muPDFCore: MuPDFCore,
        result: (Int, Int) -> Unit
    ) {
        val currentPage = currentPage.value ?: 0
        var startIndex = lastIndexSearch?.let {
            it + direction
        } ?: currentPage
        scope.launch(Dispatchers.IO) {
            while (0 <= startIndex && startIndex < muPDFCore.countPages()) {
                val searchHits = muPDFCore.searchPage(startIndex, text)
                if (searchHits.isNotEmpty()) {
                    coroutineFunction.loadZoomedPage(
                        muPDFCore,
                        startIndex,
                        currentZoom,
                        searchHits,
                        success = { _, uri ->
                            uriSearch = uri
                            scope.launch(Dispatchers.Main) {
                                result(startIndex, currentPage)
                            }
                            lastIndexSearch = startIndex
                        })
                    break
                }
                startIndex += direction
            }
        }
    }

    fun getBitmapPage(
        muPDFCore: MuPDFCore,
        startPage: Int,
        success: (Int, Uri) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val totalPage = muPDFCore.countPages()
            var i = 0
            while ((startPage + i <= totalPage - 1) || (startPage - i >= 0)) {
                withContext(scope.coroutineContext + Dispatchers.IO) {
                    if (startPage + i <= totalPage - 1) {
                        coroutineFunction.loadZoomedPage(
                            muPDFCore,
                            startPage + i,
                            currentZoom,
                            success = { page, uri ->
                                mapUriPage[page] = uri
                                scope.launch(Dispatchers.Main) {
                                    if (mapUriPage.size == muPDFCore.countPages()) {
                                        pageLoadedFinish?.invoke(true)
                                    }
                                    success(page, uri)
                                }
                            })
                    }
                }

                withContext(scope.coroutineContext + Dispatchers.IO) {
                    if (startPage - i >= 0) {
                        coroutineFunction.loadZoomedPage(
                            muPDFCore,
                            startPage - i,
                            currentZoom,
                            success = { page, uri ->
                                mapUriPage[page] = uri
                                scope.launch(Dispatchers.Main) {
                                    if (mapUriPage.size == muPDFCore.countPages()) {
                                        pageLoadedFinish?.invoke(true)
                                    }
                                    success(page, uri)
                                }
                            })
                    }
                }
                i++
            }
        }
    }

    fun isbookmark(page: Int): Boolean {
        getListbookmark()?.let {
            for (bookmark in it) {
                if (bookmark.page == page) return true
            }
        }
        return false
    }

    fun getListbookmark(): MutableList<Bookmark>? {
        return if (listbookmark.value == null) ArrayList<Bookmark>() else listbookmark.value
    }

    fun updatebookmark(path: String, isAdd: Boolean, page: Int) {
        val datas = getListbookmark()
        if (isAdd) {
            if (!isbookmark(page)) {
                datas?.add(Bookmark(page, System.currentTimeMillis()))
            }
        } else {
            if (!isbookmark(page)) return
            datas?.let {
                for (i in it.indices.reversed()) {
                    if (datas[i].page == page) {
                        datas.removeAt(i)
                        break
                    }
                }
            }
        }
        val fileStatus = FileData(path)
        fileStatus.lstbookmark = getListbookmark()
        fileStatus.totalPage = 0
        fileStatus.currentPage = 0
        Observable.just(fileStatus)
            .observeOn(Schedulers.io())
            .subscribe { data: FileData? ->
                repository.updateFileStatus(data)
            }
        listbookmark.setValue(datas)
    }

    fun getFileStatus(path: String?) {
        compositeDisposable.add(repository.getFileStatus(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { filsData: FileData? ->
                if (filsData != null) {
                    if (filsData.lstbookmark != null) {
                        listbookmark.value = filsData.lstbookmark
                    }
                }
            }
        )
    }

    inline fun <T : Any> ifLet(vararg elements: T?, closure: (List<T>) -> Unit) {
        if (elements.all { it != null }) {
            closure(elements.filterNotNull())
        }
    }

    fun signaturePDF(
        bitmapPoints: FloatArray,
        urlFile: String?,
        fileSignature: File?,
        indexPage: Int?,
        widthScreen: Float,
        heightScreen: Float,
        ratioByWidth: Boolean,
        result: (Boolean) -> Unit
    ) {
        ifLet(indexPage, fileSignature, urlFile) {
            isLoading.postValue(true)
            scope.launch(Dispatchers.IO) {
                val fileOriginal = File(urlFile)
                if (!fileOriginal.exists()) {
                    isLoading.postValue(false)
                    result.invoke(false)
                } else {
                    val document = PDSPDFDocument(getApplication(), Uri.fromFile(fileOriginal))
                    SignatureUtils.signaturePDF(
                        getApplication(),
                        bitmapPoints,
                        document,
                        fileSignature!!,
                        indexPage!!,
                        widthScreen,
                        heightScreen,
                        ratioByWidth
                    )?.let { pathNew ->
                        FileSaveManager.deleteFile(getApplication(), fileOriginal.path) {
                            FileSaveManager.saveFileStorage(
                                getApplication(),
                                pathNew,
                                fileOriginal.parent,
                                FilenameUtils.getBaseName(fileOriginal.path)
                            )

                            scope.launch(Dispatchers.Main) {
                                isLoading.postValue(false)
                                result.invoke(true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun signaturePDF2(
        urlFile: String?,
        fileSignature: File?,
        indexPage: Int?,
        result: (Boolean) -> Unit
    ) {
        ifLet(indexPage, fileSignature, urlFile) {
            isLoading.postValue(true)
            scope.launch(Dispatchers.IO) {
                val fileOriginal = File(urlFile)
                if (!fileOriginal.exists()) {
                    isLoading.postValue(false)
                    result.invoke(false)
                } else {
                    val document = PDSPDFDocument(getApplication(), Uri.fromFile(fileOriginal))

                    SignatureUtils.signaturePDF2(
                        getApplication(),
                        document,
                        fileSignature!!,
                        indexPage!!
                    )?.let { pathNew ->
                        FileSaveManager.deleteFile(getApplication(), fileOriginal.path) {
                            FileSaveManager.saveFileStorage(
                                getApplication(),
                                pathNew,
                                fileOriginal.parent,
                                FilenameUtils.getBaseName(fileOriginal.path)
                            )

                            scope.launch(Dispatchers.Main) {
                                isLoading.postValue(false)
                                result.invoke(true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun addWatermark(
        filePath: String,
        watermarkModel: WatermarkModel,
        result: (Boolean) -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            isLoading.postValue(true)
            val file = File(filePath)
            val parentSave: String =
                if (FilenameUtils.getBaseName(filePath) == watermarkModel.fileName) {
                    getApplication<Application>().cacheDir.path + "/watermark"
                } else {
                    file.parent
                }
            File(parentSave).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val finPath =
                Watermark.start(
                    getApplication(),
                    filePath,
                    parentSave,
                    watermarkModel
                )
            if (finPath.isNullOrEmpty()) {
                isLoading.postValue(false)
                result.invoke(false)
            } else if (parentSave.contains(getApplication<Application>().cacheDir.path)) {
                FileSaveManager.deleteFile(getApplication(), file.path) {
                    FileSaveManager.saveFileStorage(
                        getApplication(),
                        finPath,
                        file.parent,
                        FilenameUtils.getBaseName(file.path)
                    )
                    File(finPath).delete()
                    isLoading.postValue(false)
                    result.invoke(true)
                }
            } else {
                isLoading.postValue(false)
                result.invoke(true)
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
        compositeDisposable.clear()
    }

}