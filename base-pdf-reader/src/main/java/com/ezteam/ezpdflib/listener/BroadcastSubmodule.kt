package com.ezteam.ezpdflib.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

class BroadcastSubmodule : BroadcastReceiver() {

    companion object {
        const val ACTION_FAVORITE = "action favorite"
        const val ACTION_DELETE = "action delete"
        const val ACTION_RECENT = "action recent"
        const val IS_FAVORITE = "is favorite"
        const val ACTION_READING_STATUS = "current reading status"
        const val PATH = "path"
        const val IS_READ_DONE = "is_read_done"
        const val CURRENT_PAGE = "current_page"
        const val ALLOW_EDIT = "ALLOW_EDIT"
    }

    var favoriteListener: ((isFavorite: Boolean, path: String) -> Unit)? = null
    var deleteListener: ((path: String) -> Unit)? = null
    var recentListener: ((path: String) -> Unit)? = null
    var readingStatusListener: ((path: String,  isReadDone: Boolean, currentPage: Int) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val path = it.getStringExtra(PATH)
            path?.let { path ->
                when (it.action) {
                    ACTION_FAVORITE -> {
                        val isFavorite = it.getBooleanExtra(IS_FAVORITE, false)
                        favoriteListener?.invoke(isFavorite, path)
                    }
                    ACTION_DELETE -> {
                        deleteListener?.invoke(path)
                    }
                    ACTION_RECENT-> {
                        recentListener?.invoke(path)
                    }
                    ACTION_READING_STATUS -> {
                        val isReadDone = it.getBooleanExtra(IS_READ_DONE, false)
                        val currentPage = it.getIntExtra(CURRENT_PAGE, -1)
                        readingStatusListener?.invoke(path, isReadDone, currentPage)
                    }
                    else -> {

                    }
                }
            }

        }
    }

}