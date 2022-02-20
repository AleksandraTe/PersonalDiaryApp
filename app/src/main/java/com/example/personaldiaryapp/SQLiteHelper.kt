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

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 28
        private const val DATABASE_NAME = "diary.db"
        private const val TBL_NOTE = "tbl_note"
        private const val TBL_CHECKBOX = "tbl_checkbox"
        private const val ID = "_id"
        private const val DATE = "date"
        private const val TEXT = "text"
        private const val COLOR = "color"
        private const val IMAGE = "image"
        private const val VALUE = "value"
        private const val NOTE_ID = "note_id"
        private const val HAS_IMAGE = "has_image"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblNote = ("CREATE TABLE " + TBL_NOTE + "("
                + ID + " INTEGER PRIMARY KEY, "
                + DATE + " TEXT,"
                + TEXT + " TEXT,"
                + COLOR + " TEXT,"
                + HAS_IMAGE + " INTEGER,"
                + IMAGE + " BLOB" + ")")
        db?.execSQL(createTblNote)

        val createTblCheckbox = ("CREATE TABLE " + TBL_CHECKBOX + "("
                + ID + " INTEGER PRIMARY KEY, "
                + VALUE + " INTEGER,"
                + TEXT + " TEXT,"
                + NOTE_ID + " INTEGER,"
                + " FOREIGN KEY(" + NOTE_ID + ") REFERENCES " + TBL_NOTE + "(" + ID + "))")
        db?.execSQL(createTblCheckbox)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_NOTE")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CHECKBOX")
        onCreate(db)
    }

    fun insertNote(nt: NoteModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(DATE, nt.date)
        contentValues.put(TEXT, nt.text)
        contentValues.put(COLOR, nt.color)
        contentValues.put(HAS_IMAGE, if(nt.hasImage) 1 else 0)
        if(nt.hasImage){
            contentValues.put(IMAGE, nt.image?.toByteArray())
        }
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
        var date: Long
        var text: String
        var color: String
        var image: Bitmap? = null
        var hasImage: Boolean


        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(ID))
            date = cursor.getLong(cursor.getColumnIndex(DATE))
            text = cursor.getString(cursor.getColumnIndex(TEXT))
            color = cursor.getString(cursor.getColumnIndex(COLOR))
            hasImage = cursor.getInt(cursor.getColumnIndex(HAS_IMAGE)) == 1
            if(hasImage){
                image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()
            }

            val nt = NoteModel(id = id, date = date, text = text, color = color, image = image, hasImage)
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
        var image: Bitmap? = null
        var hasImage: Boolean

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                color = cursor.getString(cursor.getColumnIndex(COLOR))
                hasImage = cursor.getInt(cursor.getColumnIndex(HAS_IMAGE)) == 1
                if(hasImage) {
                    image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()
                }
                val nt = NoteModel(id = id, date = date, text = text, color = color, image = image, hasImage)
                ntList.add(nt)
            } while (cursor.moveToNext())
        }

        return ntList
    }

    @SuppressLint("Range")
    fun getTimeRangeNotes(startDate: Long, endDate:Long): ArrayList<NoteModel> {
        val ntList: ArrayList<NoteModel> = ArrayList()
        val selectQuery =
            "SELECT * FROM $TBL_NOTE WHERE $DATE > \"$startDate\" AND $DATE < \"$endDate\""
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
        var image: Bitmap? = null
        var hasImage: Boolean


        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                color = cursor.getString(cursor.getColumnIndex(COLOR))
                hasImage = cursor.getInt(cursor.getColumnIndex(HAS_IMAGE)) == 1
                if(hasImage) {
                    image = cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap()
                }
                val nt = NoteModel(id = id, date = date, text = text, color = color, image = image, hasImage)
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
        contentValues.put(COLOR, nt.color)
        contentValues.put(HAS_IMAGE, nt.hasImage)
        if(nt.hasImage){
            contentValues.put(IMAGE, nt.image?.toByteArray())
        }

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

        deleteNotesCheckboxes(id)

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
        var image: Bitmap?
        var hasImage: Boolean

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                date = cursor.getLong(cursor.getColumnIndex(DATE))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                color = cursor.getString(cursor.getColumnIndex(COLOR))
                hasImage = cursor.getInt(cursor.getColumnIndex(HAS_IMAGE)) == 1
                image = if(hasImage) cursor.getBlob(cursor.getColumnIndex(IMAGE)).toBitmap() else null

                val nt = NoteModel(id = id, date = date, text = text, color = color, image = image, hasImage)
                ntList.add(nt)
            } while (cursor.moveToNext())
        }

        return ntList

    }

    fun insertCheckbox(cb: CheckboxModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(VALUE, cb.value)
        contentValues.put(TEXT, cb.text)
        contentValues.put(NOTE_ID, cb.note_id)

        val success = db.insert(TBL_CHECKBOX, null, contentValues)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getAllCheckbox(note_id: Int): ArrayList<CheckboxModel> {

        val cbList: ArrayList<CheckboxModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_CHECKBOX WHERE $NOTE_ID = $note_id"
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
        var note_id: Int
        var value: Int
        var text: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(ID))
                note_id = cursor.getInt(cursor.getColumnIndex(NOTE_ID))
                text = cursor.getString(cursor.getColumnIndex(TEXT))
                value = cursor.getInt(cursor.getColumnIndex(VALUE))

                val cb = CheckboxModel(id = id, note_id = note_id, text = text, value = value == 1)
                cbList.add(cb)
            } while (cursor.moveToNext())
        }

        return cbList
    }

    fun setCheckboxesNoteId(id: Int): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NOTE_ID, id)
        val success = db.update(TBL_CHECKBOX, contentValues, "$NOTE_ID = 0", null)
        db.close()
        return success
    }

    fun updateCheckbox(cb: CheckboxModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(VALUE, cb.value)
        contentValues.put(TEXT, cb.text)
        val success = db.update(TBL_CHECKBOX, contentValues, "$ID = ${cb.id}", null)
        db.close()
        return success

    }

    fun deleteCheckbox(id: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, id)

        val success = db.delete(TBL_CHECKBOX, "_id=$id", null)
        db.close()
        return success
    }

    fun deleteNotesCheckboxes(id: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, id)

        val success = db.delete(TBL_CHECKBOX, "$NOTE_ID=$id", null)
        db.close()
        return success
    }

}