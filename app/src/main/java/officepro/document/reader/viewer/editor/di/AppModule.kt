package office.pdf.document.reader.viewer.editor.di

import office.pdf.document.reader.viewer.editor.database.AppDatabase
import office.pdf.document.reader.viewer.editor.database.repository.FileModelRepository
import office.pdf.document.reader.viewer.editor.database.repository.FileModelRepositoryImpl
import office.pdf.document.reader.viewer.editor.screen.main.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { FileModelRepositoryImpl(get()) as FileModelRepository}
    single { MainViewModel(androidApplication(), get()) }
}