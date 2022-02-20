package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var btnAdd: FloatingActionButton
    private lateinit var sqliteHelper:SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnCalendar: Button
    private lateinit var btnPrint: Button
    private var adapter:NoteAdapter? = null

    override fun onResume() {
        super.onResume()
        getNotes()
    }
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)
        getNotes()
        val sdf = SimpleDateFormat("dd/M/yyyy")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        btnAdd.setOnClickListener { 
            val intent = Intent(this, NewNoteActivity::class.java)

            if (sqliteHelper.getNote(calendar.timeInMillis).isEmpty()) {
                intent.putExtra("new", "true")
            }
            else {
                val todayNote = sqliteHelper.getNote(calendar.timeInMillis)[0]
                intent.putExtra("new", "false")
                intent.putExtra("ntId", todayNote.id)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = todayNote.date

                intent.putExtra("ntDate", sdf.format(calendar.time))
                intent.putExtra("ntText", todayNote.text)
                intent.putExtra("ntColor", todayNote.color)
                intent.putExtra("ntHasImage", todayNote.hasImage)
            }
            startActivity(intent)

        }

        adapter?.setOnClickItem {
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "false")
            intent.putExtra("ntId", it.id)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.date

            intent.putExtra("ntDate", sdf.format(calendar.time))
            intent.putExtra("ntText", it.text)
            intent.putExtra("ntColor", it.color)
            intent.putExtra("ntHasImage", it.hasImage)
            Log.e("EEEE", it.toString())
            startActivity(intent)
        }

        adapter?.setOnClickDeleteItem {
            deleteNote(it.id)
        }

        btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        btnPrint.setOnClickListener {
            val intent = Intent(this, DownloadNoteActivity::class.java)
            startActivity(intent)
        }

        searchView.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                getSearchedNotes(newText!!.lowercase())
                return true
            }
        })
    }

    private fun getSearchedNotes(newText: String?) {
        val ntList = sqliteHelper.getSearchedNote(newText!!)

        adapter?.addItems(ntList)
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
        btnPrint = findViewById(R.id.btnPrint)
    }
}