package com.ezteam.baseproject.photopicker

/**
 * Created by donglua on 15/6/30.
 */
data class Photo(
    var id: Long = 0,
    var path: String = "",
    var bucketId: String = "",
    var bucketName: String = "",
    var isSelected: Boolean = false,
    var dateAdded: Long = 0
)