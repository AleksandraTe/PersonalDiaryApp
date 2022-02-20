package com.example.personaldiaryapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class DownloadNoteActivity : AppCompatActivity() {

    private lateinit var tvDatePicker: TextView
    private lateinit var openCalendar: Button
    private lateinit var btnDownload: Button
    private lateinit var btnDownloadFiles: Button
    private lateinit var sqliteHelper: SQLiteHelper
    private val STORAGE_CODE: Int = 100;
    private lateinit var bmpToDo: Bitmap
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0
    private lateinit var bmp: Bitmap


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_note)

        initView()
        sqliteHelper = SQLiteHelper(this)
        val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(
            Pair.create(
                MaterialDatePicker.todayInUtcMilliseconds(),
                MaterialDatePicker.todayInUtcMilliseconds()
            )
        ).build()

        btnDownload.setOnClickListener {
            generateNoteDialog()
        }

        openCalendar.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "Tag_picker")
            materialDatePicker.addOnPositiveButtonClickListener {
                tvDatePicker.text = materialDatePicker.headerText
                val range = materialDatePicker.headerText.split("–")
                Log.e("eee", range.toString())
                startDateMillis = getMilliseconds(range[0])
                endDateMillis = getMilliseconds(range[1])
            }
        }

        btnDownloadFiles.setOnClickListener {
            downloadAllFiles()
        }
    }

    private fun downloadAllFiles() {


        bmp = BitmapFactory.decodeResource(resources, R.drawable.emptytemplate)

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()

        for (note in sqliteHelper.getTimeRangeNotes(startDateMillis, endDateMillis)){
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color =  (Color.parseColor("#FFFFFF"))
            canvas.drawPaint(paint)

            val bmp = Bitmap.createScaledBitmap(bmp, bmp.width, bmp.height, true)
            canvas.drawBitmap(bmp, 0f, 0f, null)

            val c = Calendar.getInstance()
            c.timeInMillis = note.date
            val date = "${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}"
            val textPaint = Paint()
            textPaint.color = Color.BLACK
            textPaint.textSize = 160F
            canvas.drawText(date, 850F, 550F, textPaint)
            var textOffsetY = 0f
            if(note.hasImage){
                canvas.drawBitmap(note.image!!, (bmp.width - note.image.width)/2f, 800f, null)
                textOffsetY = note.image.height.toFloat()
            }
            canvas.drawText(note.text, 400F, 1000F + textOffsetY, textPaint)
            //TODO: RYSOWANIE CHECKBOXÓW
            pdfDocument.finishPage(page)
        }


        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName + ".pdf"

        pdfDocument.writeTo(FileOutputStream(mFilePath))
        pdfDocument.close()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMilliseconds(date: String): Long {
        val date = date.split(" ")
        val day = date[0].toInt()
        val month = when(date[1]){
            "sty" -> 0
            "lut" -> 1
            "mar" -> 2
            "kwi" -> 3
            "maj" -> 4
            "cze" -> 5
            "lip" -> 6
            "sie" -> 7
            "wrz" -> 8
            "paz" -> 9
            "lis" -> 10
            "gru" -> 11
            else -> -1
        }
        val year : Int
        if(date.size > 2) {
            year = date[2].toInt()
        } else {
            year = LocalDate.now().year
        }

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    private fun generateNoteDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.empty_note_dialog, null)
        val cbText = dialogLayout.findViewById<CheckBox>(R.id.cbText)
        val cbChecklist = dialogLayout.findViewById<CheckBox>(R.id.cbChecklist)

        with(builder){
            setTitle("Select a note template: ")
            setPositiveButton("Download pdf") { _, _ ->

                when {
                    cbText.isChecked && !cbChecklist.isChecked-> {
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.text)
                    }
                    cbChecklist.isChecked && !cbText.isChecked -> {
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.todo)
                    }
                    cbChecklist.isChecked && cbText.isChecked -> {
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.todotext)
                    }
                }
                downloadEmptyPdf()
            }

            setView(dialogLayout)
            show()
        }
    }

    private fun generatePdf() {

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bmpToDo.width, bmpToDo.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.color =  (Color.parseColor("#FFFFFF"))
        canvas.drawPaint(paint)

        val bmpToDo = Bitmap.createScaledBitmap(bmpToDo, bmpToDo.width, bmpToDo.height, true )
        canvas.drawBitmap(bmpToDo, 0f,0f,null)

        pdfDocument.finishPage(page)


        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName + ".pdf"

        pdfDocument.writeTo(FileOutputStream(mFilePath))
        pdfDocument.close()

    }

    private fun downloadEmptyPdf(){

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, STORAGE_CODE)
        } else {
            generatePdf()
        }
    }

    private fun initView() {
        tvDatePicker = findViewById(R.id.tvDatePicker)
        openCalendar = findViewById(R.id.openCalendar)
        btnDownload = findViewById(R.id.btnDownload)
        btnDownloadFiles = findViewById(R.id.btnDownloadFiles)
    }
}