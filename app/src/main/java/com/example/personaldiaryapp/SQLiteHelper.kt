package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 13
        private const val DATABASE_NAME = "diary.db"
        private const val TBL_NOTE = "tbl_note"
        private const val ID = "_id"
        private const val DATE = "date"
        private const val TEXT = "text"
        private const val COLOR = "color"
        private const val IMAGE = "image"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createTblNote = ("CREATE TABLE " + TBL_NOTE + "("
                + ID + " INTEGER PRIMARY KEY, " + DATE + " NUMERIC,"
                + TEXT + " TEXT," + COLOR + " TEXT," + IMAGE + " BLOB" + ")")
        db?.execSQL(createTblNote)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_NOTE")
        onCreate(db)
    }

    fun insertNote(nt: NoteModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(DATE, nt.date)
        contentValues.put(TEXT, nt.text)
        contentValues.put(COLOR, nt.color)
        contentValues.put(IMAGE, nt.image.toByteArray())

        val success = db.insert(TBL_NOTE, null, contentValues)
        db.close()
        return success
    }

    fun Bitmap.toByteArray():ByteArray{
        ByteArrayOutputStream().apply {
            compress(Bitmap.CompressFormat.JPEG,100,this)
            return toByteArray()
        }
    }

    fun ByteArray.toBitmap():Bitmap{
        return BitmapFactory.decodeByteArray(this,0,size)
    }

    @SuppressLint("Range")
    fun getNote(date: Long): ArrayList<NoteModel> {
        val ntList: ArrayList<NoteModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTE WHERE $DATE = $date"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var date: Long
        var text: String
        var color: String
        var image: Bitmap


        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(ID))
            date = cursor.getLong(cursor.getColumnIndex(DATE))
            text = cursor.getString(cursor.getColumnIndex(TEXT))
            color = cursor.getString(cursor.getColumnIndex(COLOR))
            image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()

            val nt = NoteModel(id = id, date = date, text = text, color = color, image = image)
            ntList.add(nt)
        }

        return ntList

    }


    @SuppressLint("Range")
    fun getAllNote(): ArrayList<NoteModel> {
        val ntList: ArrayList<NoteModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTE ORDER BY $DATE DESC"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var date: Long
        var text: String
        var color: String
        var image: Bitmap

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                color = cursor.getString(cursor.getColumnIndex(COLOR))
                image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()

                val nt = NoteModel(id = id, date = date, text = text, color = color, image = image)
                ntList.add(nt)
            } while (cursor.moveToNext())
        }

        return ntList
    }

    fun updateNote(nt: NoteModel): Int {
        val db = this.writableDatabase
        val stream = ByteArrayOutputStream()

        val contentValues = ContentValues()
        contentValues.put(DATE, nt.date)
        contentValues.put(TEXT, nt.text)
        contentValues.put(COLOR, nt.color)
        contentValues.put(IMAGE, nt.image.toByteArray())
        val success = db.update(TBL_NOTE, contentValues, "_id=" + nt.id, null)
        db.close()
        return success
    }

    fun deleteNote(id:Int): Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, id)

        val success = db.delete(TBL_NOTE, "_id=$id", null)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getSearchedNote(newText : String): ArrayList<NoteModel> {
        val ntList: ArrayList<NoteModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTE WHERE $TEXT LIKE \"%$newText%\" ORDER BY $DATE DESC"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var date: Long
        var text: String
        var color: String
        var image: Bitmap

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                color = cursor.getString(cursor.getColumnIndex(COLOR))
                image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()

                val nt = NoteModel(id = id, date = date, text = text, color = color, image = image)
                ntList.add(nt)
            } while (cursor.moveToNext())
        }

        return ntList

    }
}