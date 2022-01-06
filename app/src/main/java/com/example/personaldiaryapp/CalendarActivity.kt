package com.example.personaldiaryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var  recyclerView: RecyclerView
    private lateinit var sqliteHelper:SQLiteHelper

    private var adapter:NoteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)

        calendarView.setOnDateChangeListener { calendarView, i, i2, i3 ->
            val mon = i2 + 1
            val day: String
            if(i3 < 10){
                day = "0$i3"
            } else {
                day = i3.toString()
            }
            getOneNote("$day/$mon/$i")
        }

        adapter?.setOnClickItem {
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "false")
            intent.putExtra("ntId", it.id)
            intent.putExtra("ntDate", it.date)
            intent.putExtra("ntText", it.text)
            startActivity(intent)

        }

    }

    private fun getOneNote(date: String) {
        val ntList = sqliteHelper.getNote(date)
        Log.e("error", "${ntList.size}")

        adapter?.addItems(ntList)
    }

    private fun ShowExistingNotes() {

    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter()
        recyclerView.adapter = adapter
    }

    private fun initView() {
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerView)
    }
}