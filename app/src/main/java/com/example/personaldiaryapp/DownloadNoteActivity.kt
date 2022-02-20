package com.example.personaldiaryapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DownloadNoteActivity : AppCompatActivity() {

    private lateinit var tvDatePicker: TextView
    private lateinit var openCalendar: Button
    private lateinit var btnDownload: Button
    private val STORAGE_CODE: Int = 100;
    private lateinit var bmpToDo: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_note)

        initView()

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalendar)
        }

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
            }
        }
    }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tvDatePicker.setText(sdf.format(myCalendar.time))
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
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.emptytemplate)
                    }
                    cbChecklist.isChecked && !cbText.isChecked -> {
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.todolist)
                    }
                    cbChecklist.isChecked && cbText.isChecked -> {
                        bmpToDo = BitmapFactory.decodeResource(resources, R.drawable.texttodo)
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
    }
}