package com.example.personaldiaryapp

import android.graphics.Bitmap

data class NoteModel(
    var id: Int,
    var date: String,
    var text: String,
    var color: String,
    var image: Bitmap

)