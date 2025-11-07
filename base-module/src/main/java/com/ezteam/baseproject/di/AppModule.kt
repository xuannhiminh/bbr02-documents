package com.ezteam.baseproject.di

import com.ezteam.baseproject.photopicker.PickerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val baseModule = module {
    single { PickerViewModel(androidApplication()) }
}