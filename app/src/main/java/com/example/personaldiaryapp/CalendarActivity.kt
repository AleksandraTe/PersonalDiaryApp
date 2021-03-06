package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var sqliteHelper:SQLiteHelper
    private lateinit var btnAdd: Button
    private lateinit var selectedDate: String


    private var adapter:NoteAdapter? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        selectedDate = currentDate

        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        btnAdd.isVisible = getOneNote(calendar.timeInMillis).isEmpty()

        calendarView.setOnDateChangeListener { _, i, i2, i3 ->
            calendar.set(i, i2, i3, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            btnAdd.isVisible = getOneNote(calendar.timeInMillis).isEmpty()
            selectedDate = "$i3/${i2+1}/$i"
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "true")
            intent.putExtra("ntDate", selectedDate)
            startActivity(intent)
        }

        adapter?.setOnClickItem {
            val intent = Intent(this, NewNoteActivity::class.java)
            intent.putExtra("new", "false")
            intent.putExtra("ntId", it.id)
            intent.putExtra("ntDate", sdf.format(it.date))
            intent.putExtra("ntText", it.text)
            intent.putExtra("ntColor", it.color)
            intent.putExtra("ntHasImage", it.hasImage)
            startActivity(intent)
        }

        adapter?.setOnClickDeleteItem {
            deleteNote(it.id)
        }
    }

    private fun deleteNote(id:Int) {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete note?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialog, _ ->
            sqliteHelper.deleteNote(id)
            dialog.dismiss()
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    private fun getOneNote(date: Long): ArrayList<NoteModel> {
        val ntList = sqliteHelper.getNote(date)

        adapter?.addItems(ntList)

        return ntList
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter()
        recyclerView.adapter = adapter
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initView() {
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.recyclerView)
        btnAdd = findViewById(R.id.btnAdd)

    }
}