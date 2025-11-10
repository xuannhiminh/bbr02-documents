package officepro.document.reader.viewer.editor.database

import androidx.lifecycle.LiveData
import androidx.room.*
import officepro.document.reader.viewer.editor.model.FileModel

@Dao
interface FileModelDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fileModel: FileModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fileModels: List<FileModel>)

    @Delete
    fun delete(fileModel: FileModel)

    @Delete
    fun delete(fileModels: List<FileModel>)

    @Query("DELETE FROM file")
    fun deleteAll()

    @Query("UPDATE file SET isRecent = 0 WHERE isRecent = 1")
    fun deleteAllRecent()

    @Query("UPDATE file SET isFavorite = 0 WHERE isFavorite = 1")
    fun deleteAllFavorite()

    @Query("UPDATE file SET isNoticed = 1 WHERE path = :path")
    fun setNotRecently(path: String)

    @Query("UPDATE file SET isNoticed = 1 WHERE path IN(:paths)")
    fun setNotRecently(paths: List<String>)
    @Query("UPDATE file SET isFavorite = 0 WHERE path IN(:paths)")
    fun removeFavourite(paths: List<String>)

    @Query("SELECT * FROM file WHERE name LIKE :textSearch ORDER BY name")
    fun getLiveDataSearchFile(textSearch: String): LiveData<List<FileModel>>

    @Query(
        """SELECT * FROM file
            WHERE (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
            ORDER BY 
                CASE WHEN :sortState = 1 THEN name END ASC,
                CASE WHEN :sortState = 2 THEN name END DESC,
                CASE WHEN :sortState = 3 THEN date END ASC,
                CASE WHEN :sortState = 4 THEN date END DESC,
                CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
                CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
                CASE WHEN :sortState = 7 THEN date END DESC
        """
    )
    fun getLiveDataAllFile(sortState: Int): LiveData<List<FileModel>>

    @Query(
        """SELECT * FROM file WHERE path LIKE '%.pdf' and (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
        CASE WHEN :sortState = 7 THEN date END DESC
        """
    )
    fun getPdfFiles(sortState: Int): LiveData<List<FileModel>>
    @Query(
        """SELECT * FROM file WHERE path LIKE '%.txt' and (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
        CASE WHEN :sortState = 7 THEN date END DESC
        """
    )
    fun getTxtFiles(sortState: Int): LiveData<List<FileModel>>
    @Query(
        """SELECT * FROM file WHERE 
                (path LIKE '%.doc' OR path LIKE '%.docx')
                AND (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
        CASE WHEN :sortState = 7 THEN date END DESC
        """
        // ***        CASE WHEN :sortState = 7 THEN CAST(size AS REAL) END DESC ONLY TODAY
    )
    fun getWordFiles(sortState: Int): LiveData<List<FileModel>>
    @Query(
        """SELECT * FROM file WHERE (path LIKE '%.xls' OR path LIKE '%.xlsx' OR path LIKE '%.xlsm')
            AND (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
        CASE WHEN :sortState = 7 THEN date END DESC
        """
    )
    fun getExcelFiles(sortState: Int): LiveData<List<FileModel>>
    @Query(
        """SELECT * FROM file WHERE (path LIKE '%.ppt' OR path LIKE '%.pptx')
            AND (:sortState != 7 OR (date >= (strftime('%s', 'now') - 86400) * 1000))
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC,
        CASE WHEN :sortState = 7 THEN date END DESC
        """
    )
    fun getPptFiles(sortState: Int): LiveData<List<FileModel>>

    @Query("SELECT * FROM file")
    fun getAllFile(): List<FileModel>

    @Query("""
        SELECT * FROM file 
        WHERE (:currentTime - date) >= :minDaysInMillis
        ORDER BY unixTimeRecent ASC, date ASC 
        LIMIT 1
    """)
    fun getOldestForgottenFile(currentTime: Long, minDaysInMillis: Long): FileModel?

    @Query(
        """
        SELECT * FROM file 
        WHERE LOWER(path) LIKE '%.pdf' and LOWER(path) NOT LIKE '%file_example%'
        AND isReadDone = 0 and (:currentTime - unixTimeRecent) >= :daysFilterInMillis
        ORDER BY unixTimeRecent ASC, date ASC 
        LIMIT 1
    """
    )
    fun getLatestUnFinishedFile(currentTime: Long, daysFilterInMillis: Long): FileModel?

    @Query(
        """SELECT * FROM file WHERE isRecent = 1
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC
        """
    )
    fun getRecentFiles(sortState: Int): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE path LIKE '%.pdf' AND isRecent = 1")
    fun getRecentPdfFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isRecent = 1 AND (path LIKE '%.doc' OR path LIKE '%.docx') ")
    fun getRecentWordFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isRecent = 1 AND (path LIKE '%.ppt' OR path LIKE '%.pptx') ")
    fun getRecentPptFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isRecent = 1 AND (path LIKE '%.xls' OR path LIKE '%.xlsx' OR path LIKE '%.xlsm')")
    fun getRecentExcelFiles(): LiveData<List<FileModel>>
    @Query("SELECT * FROM file WHERE path LIKE '%.txt' AND isRecent = 1")
    fun getRecentTxtFiles(): LiveData<List<FileModel>>
    @Query(
        """SELECT * FROM file WHERE isFavorite = 1
        ORDER BY 
        CASE WHEN :sortState = 1 THEN name END ASC,
        CASE WHEN :sortState = 2 THEN name END DESC,
        CASE WHEN :sortState = 3 THEN date END ASC,
        CASE WHEN :sortState = 4 THEN date END DESC,
        CASE WHEN :sortState = 5 THEN CAST(size AS REAL) END ASC,
        CASE WHEN :sortState = 6 THEN CAST(size AS REAL) END DESC
        """
    )
    fun getFavoriteFiles(sortState: Int): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isFavorite = 1 AND path LIKE '%.pdf'")
    fun getFavoritePdfFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isFavorite = 1 AND (path LIKE '%.doc' OR path LIKE '%.docx')")
    fun getFavoriteWordFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isFavorite = 1 AND (path LIKE '%.ppt' OR path LIKE '%.pptx')")
    fun getFavoritePptFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE isFavorite = 1 AND (path LIKE '%.xls' OR path LIKE '%.xlsx' OR path LIKE '%.xlsm')")
    fun getFavoriteExcelFiles(): LiveData<List<FileModel>>
    @Query("SELECT * FROM file WHERE isFavorite = 1 AND path LIKE '%.txt'")
    fun getFavoriteTxtFiles(): LiveData<List<FileModel>>

    @Query("SELECT * FROM file WHERE path = :path")
    fun getFileByPath(path: String): FileModel?

    @Query("SELECT * FROM file ORDER BY date DESC limit 3")
    fun getLatestFiles(): List<FileModel>

    @Query("""
        SELECT COUNT(*) FROM file 
        WHERE isNoticed = 0 and date >= (strftime('%s', 'now') - 86400) * 1000
          AND (
            :fileType = 'ALL'
            OR (:fileType = 'PDF' AND path LIKE '%.pdf')
            OR (:fileType = 'WORD' AND (path LIKE '%.doc' OR path LIKE '%.docx'))
            OR (:fileType = 'PPT' AND (path LIKE '%.ppt' OR path LIKE '%.pptx'))
            OR (:fileType = 'TXT' AND path LIKE '%.txt')
            OR (:fileType = 'EXCEL' AND (path LIKE '%.xls' OR path LIKE '%.xlsx' OR path LIKE '%.xlsm'))
          )
    """)
    fun getNumberOfTodayAddedFile(fileType: String): LiveData<Int>
    @Query("""
        SELECT COUNT(*) FROM file 
        WHERE 
            :fileType = 'ALL'
            OR (:fileType = 'PDF' AND path LIKE '%.pdf')
            OR (:fileType = 'TXT' AND path LIKE '%.txt')
            OR (:fileType = 'WORD' AND (path LIKE '%.doc' OR path LIKE '%.docx'))
            OR (:fileType = 'PPT' AND (path LIKE '%.ppt' OR path LIKE '%.pptx'))
            OR (:fileType = 'EXCEL' AND (path LIKE '%.xls' OR path LIKE '%.xlsx' OR path LIKE '%.xlsm'))
    """)
    fun getNumberOfTotalFile(fileType: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM file WHERE isRecent = 1")
    fun getTotalRecentFiles(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM file WHERE isFavorite = 1")
    fun getTotalFavoriteFiles(): LiveData<Int>
}