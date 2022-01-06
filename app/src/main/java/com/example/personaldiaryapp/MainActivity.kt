package com.example.personaldiaryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.service.autofill.FillEventHistory
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var btnAdd: Button
    private lateinit var sqliteHelper:SQLiteHelper
    private lateinit var  recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnCalendar: Button

    private var adapter:NoteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)
        getNotes()
        btnAdd.setOnClickListener { 
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "true")
            startActivity(intent)

        }

        adapter?.setOnClickItem {
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "false")
            intent.putExtra("ntId", it.id)
            intent.putExtra("ntDate", it.date)
            intent.putExtra("ntText", it.text)
            startActivity(intent)

        }

        adapter?.setOnClickDeleteItem {
            deleteNote(it.id)
        }

        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)

        }
    }

    private fun deleteNote(id:Int) {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete note?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialog, _ ->
            sqliteHelper.deleteNote(id)
            getNotes()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    private fun getNotes() {
        val ntList = sqliteHelper.getAllNote()
        Log.e("error", "${ntList.size}")

        adapter?.addItems(ntList)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter()
        recyclerView.adapter = adapter
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        btnCalendar = findViewById(R.id.btnCalendar)
    }
}