package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "diary.db"
        private const val TBL_NOTE = "tbl_note"
        private const val ID = "_id"
        private const val DATE = "date"
        private const val TEXT = "text"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createTblNote = ("CREATE TABLE " + TBL_NOTE + "("
                + ID + " INTEGER PRIMARY KEY, " + DATE + " TEXT,"
                + TEXT + " TEXT" + ")")
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

        val success = db.insert(TBL_NOTE, null, contentValues)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getNote(date: String): ArrayList<NoteModel> {
        val ntList: ArrayList<NoteModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTE WHERE $DATE " + "= \"$date\""
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
        var date: String
        var text: String

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(ID))
            date = cursor.getString(cursor.getColumnIndex(DATE))
            text = cursor.getString(cursor.getColumnIndex(TEXT))

            val nt = NoteModel(id = id, date = date, text = text)
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
        var date: String
        var text: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getString(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))

                val nt = NoteModel(id = id, date = date, text = text)
                ntList.add(nt)
            } while (cursor.moveToNext())
        }

        return ntList
    }

    fun updateNote(nt: NoteModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(DATE, nt.date)
        contentValues.put(TEXT, nt.text)
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
}