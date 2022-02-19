package com.example.personaldiaryapp

import android.graphics.Bitmap

data class NoteModel(
    var id: Int,
    var date: Long,
    var text: String,
    var color: String,
    var image: Bitmap

)