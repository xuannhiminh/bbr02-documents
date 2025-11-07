package com.ezstudio.pdftoolmodule.di

import com.ezstudio.pdftoolmodule.viewmodel.PdfToolViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val toolModule = module {
    single { PdfToolViewModel(androidApplication()) }
}