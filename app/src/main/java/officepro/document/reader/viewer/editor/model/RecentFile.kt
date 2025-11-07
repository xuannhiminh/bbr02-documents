package officepro.document.reader.viewer.editor.model

import androidx.annotation.Keep

@Keep
data class RecentFile(
    val type: FileType,
    val title: String
)

enum class FileType {
    PDF, DOC, XLS, PPT
}
