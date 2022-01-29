package com.example.personaldiaryapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_new_note.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class NewNoteActivity : AppCompatActivity() {
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnPreviousDays: Button
    private lateinit var btnNextDate: Button
    private lateinit var tvDate: TextView
    private lateinit var edText: EditText
    private lateinit var sqliteHelper:SQLiteHelper
    private lateinit var rlNewNote: RelativeLayout
    private lateinit var selectedColor: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)
        initView()
        sqliteHelper = SQLiteHelper(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val isNew = intent.getStringExtra("new")
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        tvDate.setText(currentDate)

        if(isNew == "true"){
            loadNewNoteView()
        }
        else{
            loadEditView()
        }

        edText.requestFocus()
        
        btnSave.setOnClickListener {
            saveNote()
        }

        imgMore.setOnClickListener {

            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance()
            noteBottomSheetFragment.show(supportFragmentManager, "Note Bottom Sheet Fragment")
        }

        btnNextDate.setOnClickListener() {
            openAnotherNote(true)
        }

        btnPreviousDays.setOnClickListener() {

            openAnotherNote(false)

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openAnotherNote(next: Boolean){

        val intent = Intent(this, NewNoteActivity::class.java)
        val dtf = DateTimeFormatter.ofPattern("dd/M/yyyy")
        val currentDate = LocalDate.parse(tvDate.text, dtf)
        val nextDateString: String

        if(next){
            nextDateString = dtf.format(currentDate.plusDays(1)).toString()
        } else {
            nextDateString = dtf.format(currentDate.minusDays(1)).toString()
        }

        if (sqliteHelper.getNote(nextDateString).isEmpty()) {
            intent.putExtra("new", "true")
            intent.putExtra("ntDate", nextDateString)
        }
        else {
            val todayNote = sqliteHelper.getNote(nextDateString).get(0)
            intent.putExtra("new", "false")
            intent.putExtra("ntId", todayNote.id)
            intent.putExtra("ntDate", todayNote.date)
            intent.putExtra("ntText", todayNote.text)
            intent.putExtra("ntColor", todayNote.color)
        }
        startActivity(intent)
        finish()
    }
    private fun loadNewNoteView() {

        val date = intent.getStringExtra("ntDate")
        selectedColor = "#B3B7C0"
        btnUpdate.isVisible = false
        btnSave.setOnClickListener {
            saveNote()
        }
        if (!date.isNullOrEmpty()){
            tvDate.setText(date)
        }
    }

    private fun loadEditView() {
        btnSave.isVisible = false
        val date = intent.getStringExtra("ntDate")
        val text = intent.getStringExtra("ntText")
        val color = intent.getStringExtra("ntColor")
        selectedColor = color!!

        btnUpdate.setOnClickListener {
            updateNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        tvDate.setText(date)
        edText.setText(text)
        rlNewNote.setBackgroundColor(Color.parseColor(color))

    }

    private fun updateNote() {
        val date = tvDate.text.toString()
        val text = edText.text.toString()
        val color = selectedColor
        val id = intent.getIntExtra("ntId", 0)
        val nt = NoteModel(id = id, date = date, text = text, color = color)

        val status = sqliteHelper.updateNote(nt)
        if (status > -1) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Edit failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {

        val date = tvDate.text.toString()
        val text = edText.text.toString()
        var color = selectedColor

        if(text.isEmpty()) {
            Toast.makeText(this, "Please enter requried field", Toast.LENGTH_SHORT).show()
        } else {
            val nt = NoteModel(id = 0, date = date, text = text, color = color)
            val status = sqliteHelper.insertNote(nt)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            //Check insert success or not success
            if (status > -1) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                Companion.clearEditText(this)
                finish()
            } else {
                Toast.makeText(this, "Record not saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val BroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            selectedColor = intent!!.getStringExtra("selectedColor")!!
            Log.e("ELO", selectedColor!!)

            rlNewNote.setBackgroundColor(Color.parseColor(selectedColor))

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BroadcastReceiver)
    }

    private fun initView() {
        tvDate = findViewById(R.id.tvDate)
        edText = findViewById(R.id.edText)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnPreviousDays = findViewById(R.id.btnPreviousDay)
        btnNextDate = findViewById(R.id.btnNextDay)
        rlNewNote = findViewById(R.id.rlNewNote)
    }

    companion object {
        private fun clearEditText(newNoteActivity: NewNoteActivity) {
            newNoteActivity.tvDate.setText("")
            newNoteActivity.edText.setText("")
            //rlNewNote.setBackgroundColor(Color.parseColor("#B3B7C0"))
            newNoteActivity.tvDate.requestFocus()
        }
    }
}