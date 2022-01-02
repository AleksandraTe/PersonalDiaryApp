package com.example.personaldiaryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible



class NewNoteActivity : AppCompatActivity() {
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var edDate: EditText
    private lateinit var edText: EditText
    private lateinit var sqliteHelper:SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        initView()
        sqliteHelper = SQLiteHelper(this)

        val isNew = intent.getStringExtra("new")

        if(isNew == "true"){
            loadNewNoteView()
        }
        else{
            loadEditView()
        }
        btnSave.setOnClickListener {
            saveNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadNewNoteView() {
        btnUpdate.isVisible = false
        btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun loadEditView() {
        btnSave.isVisible = false
        val date = intent.getStringExtra("ntDate")
        val text = intent.getStringExtra("ntText")

        btnUpdate.setOnClickListener {
            updateNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        edDate.setText(date)
        edText.setText(text)
    }

    private fun updateNote() {
        val date = edDate.text.toString()
        val text = edText.text.toString()
        val id = intent.getIntExtra("ntId", 0)
        val nt = NoteModel(id = id, date = date, text = text)


        if(nt == null) return

        val status = sqliteHelper.updateNote(nt)
        if (status > -1) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Edit failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        val date = edDate.text.toString()
        val text = edText.text.toString()

        if(date.isEmpty() || text.isEmpty()) {
            Toast.makeText(this, "Please enter requried field", Toast.LENGTH_SHORT).show()
        } else {
            val nt = NoteModel(id = 0, date = date, text = text)
            val status = sqliteHelper.insertNote(nt)
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

    private fun clearEditText() {
        edDate.setText("")
        edText.setText("")
        edDate.requestFocus()
    }

    private fun initView() {
        edDate = findViewById(R.id.edDate)
        edText = findViewById(R.id.edText)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
    }
}