package com.example.personaldiaryapp

import android.graphics.Bitmap

data class NoteModel(
    val id: Int,
    val date: Long,
    val text: String,
    val color: String,
    val image: Bitmap

)