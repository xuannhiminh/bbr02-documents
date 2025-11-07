package com.ezteam.baseproject.utils


object UrlObf {
    private val TT = intArrayOf(
        24, 8, 106, 104, 119, 119, 119, 25, 8, 27, 9, 18
    )
    private const val KEY = 0x5A
    private val O = intArrayOf(
        50,46,46,42,41,96,117,117,41,51,46,63,41,116,61,53,53,61,54,63,116,57,53,55,
        117,44,51,63,45,117,56,40,106,107
    )


    fun get(): String {
        val b = ByteArray(O.size)
        for (i in O.indices) b[i] = (O[i] xor KEY).toByte()
        return String(b, Charsets.UTF_8)
    }

    fun getTAG(): String {
        val b = ByteArray(TT.size)
        for (i in TT.indices) b[i] = (TT[i] xor KEY).toByte()
        return String(b, Charsets.UTF_8)
    }


}

