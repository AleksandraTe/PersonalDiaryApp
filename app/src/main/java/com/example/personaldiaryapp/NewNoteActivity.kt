package com.example.personaldiaryapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_new_note.*
import java.text.SimpleDateFormat
import java.util.*


class NewNoteActivity : AppCompatActivity() {
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var tvDate: TextView
    private lateinit var edText: EditText
    private lateinit var sqliteHelper:SQLiteHelper
    private lateinit var rlNewNote: RelativeLayout
    private lateinit var selectedColor: String

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

    }

    private fun loadNewNoteView() {

        val date = intent.getStringExtra("ntDate")
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
        val color = "#2b436e"

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
                clearEditText()
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


    private fun clearEditText() {
        tvDate.setText("")
        edText.setText("")
        rlNewNote.setBackgroundColor(Color.parseColor("#2e2e2e"))
        tvDate.requestFocus()
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
        rlNewNote = findViewById(R.id.rlNewNote)
    }
}