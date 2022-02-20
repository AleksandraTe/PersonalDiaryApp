package com.example.personaldiaryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_new_note.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class NewNoteActivity : AppCompatActivity() {
    private val STORAGE_CODE: Int = 100;
    private lateinit var btnSave: FloatingActionButton
    private lateinit var btnUpdate: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPreviousDays: Button
    private lateinit var btnNextDate: Button
    private lateinit var tvDate: TextView
    private lateinit var edText: EditText
    private lateinit var sqliteHelper: SQLiteHelper
    private lateinit var rlNewNote: RelativeLayout
    private lateinit var selectedColor: String
    private lateinit var imgNote: ImageView
    private var adapter:CheckboxAdapter? = null
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var hasImage = false
    private lateinit var bmp: Bitmap

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)
        initView()
        initRecyclerView()
        sqliteHelper = SQLiteHelper(this)
        sqliteHelper.deleteNotesCheckboxes(0)
        getCheckboxes()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val isNew = intent.getStringExtra("new")
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        tvDate.text = currentDate

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

        adapter?.setOnClickDeleteItem {
            sqliteHelper.deleteCheckbox(it.id)
            getCheckboxes()
        }

        edText.setTextColor(Color.BLACK)
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
        val dateSplit = nextDateString.split('/')
        val calendar = Calendar.getInstance()
        calendar.set(dateSplit[2].toInt(), dateSplit[1].toInt() - 1, dateSplit[0].toInt(), 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (sqliteHelper.getNote(calendar.timeInMillis).isEmpty()) {
            intent.putExtra("new", "true")
            intent.putExtra("ntDate", nextDateString)
        }
        else {
            val todayNote = sqliteHelper.getNote(calendar.timeInMillis)[0]
            intent.putExtra("new", "false")
            intent.putExtra("ntId", todayNote.id)
            intent.putExtra("ntDate", nextDateString)
            intent.putExtra("ntText", todayNote.text)
            intent.putExtra("ntColor", todayNote.color)
            intent.putExtra("ntHasImgae", todayNote.hasImage)
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
        hasImage = intent.getBooleanExtra("ntHasImage", false)
        val dateSplit = date!!.split('/')
        val calendar = Calendar.getInstance()
        calendar.set(dateSplit[2].toInt(), dateSplit[1].toInt() - 1, dateSplit[0].toInt(), 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        Log.e("EEEE", (calendar.timeInMillis).toString())
        Log.e("EEEE", date)

        selectedColor = color!!

        btnUpdate.setOnClickListener {
            updateNote()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        tvDate.text = date
        edText.setText(text)
        val hasImage = intent.getBooleanExtra("ntHasImage", false)
        if(hasImage) {
            val image = sqliteHelper.getNote(calendar.timeInMillis)[0].image
            imgNote.setImageBitmap(image)
            imgNote.visibility = View.VISIBLE
        }
        rlNewNote.setBackgroundColor(Color.parseColor(color))

    }

    private fun updateNote() {
        val date = tvDate.text.toString()
        val text = edText.text.toString()
        val color = selectedColor
        var image: Bitmap? = null
        Log.e("EEEE", hasImage.toString())
        if(hasImage) {
            image = imgNote.drawable.toBitmap()
            val imageMulti : Float = 1000 / image.width.toFloat()
            image = Bitmap.createScaledBitmap(image, 1000, (image.height * imageMulti).toInt(), true)
        }
        val id = intent.getIntExtra("ntId", 0)

        val dateSplit = date.split('/')
        val calendar = Calendar.getInstance()
        calendar.set(dateSplit[2].toInt(), dateSplit[1].toInt() - 1, dateSplit[0].toInt(), 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val nt = NoteModel(id = id, date = calendar.timeInMillis, text = text, color = color, image = image, hasImage)

        val status = sqliteHelper.updateNote(nt)

        saveCheckboxesNoteId(sqliteHelper.getNote(calendar.timeInMillis)[0].id)
        val checkboxes = adapter!!.getAllCheckboxes()
        Log.e("EEEE", checkboxes.toString())
        var i = 0
        for(cb in checkboxes){
            val item = recyclerView.getChildAt(i)
            cb.text = item.findViewById<EditText>(R.id.edCheckbox).text.toString()
            cb.value = item.findViewById<CheckBox>(R.id.cbValue).isChecked
            Log.e("EEEE", cb.toString())
            sqliteHelper.updateCheckbox(cb)
            i++
        }

        if (status > -1) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Edit failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        Log.e("EEEE", "START")
        val date = tvDate.text.toString()
        val text = edText.text.toString()
        var color = selectedColor
        var image: Bitmap? = null
        Log.e("EEEE", hasImage.toString())
        if(hasImage) {
            image = imgNote.drawable.toBitmap()
            val imageMulti : Float = 1000 / image.width.toFloat()
            image = Bitmap.createScaledBitmap(image, 1000, (image.height * imageMulti).toInt(), true)
        }
        if(text.isEmpty()) {
            Toast.makeText(this, "Please enter requried field", Toast.LENGTH_SHORT).show()
        } else {
            val dateSplit = date.split('/')
            Log.e("EEEE", dateSplit.toString())
            val calendar = Calendar.getInstance()
            calendar.set(dateSplit[2].toInt(), dateSplit[1].toInt() - 1, dateSplit[0].toInt(), 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val nt = NoteModel(id = 0, date = calendar.timeInMillis, text = text, color = color, image = image, hasImage)
            val status = sqliteHelper.insertNote(nt)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            saveCheckboxesNoteId(sqliteHelper.getNote(calendar.timeInMillis)[0].id)
            val checkboxes = adapter!!.getAllCheckboxes()
            Log.e("EEEE", checkboxes.toString())
            var i = 0
            for(cb in checkboxes){
                val item = recyclerView.getChildAt(i)
                cb.text = item.findViewById<EditText>(R.id.edCheckbox).text.toString()
                cb.value = item.findViewById<CheckBox>(R.id.cbValue).isChecked
                sqliteHelper.updateCheckbox(cb)
                i++
            }

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

    private fun saveCheckboxesNoteId(id: Int) {
        sqliteHelper.setCheckboxesNoteId(id)
    }


    private val BroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {

            var action = intent!!.getStringExtra("action")

            when (action!!) {

                "Gray" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    rlNewNote.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Blue" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    rlNewNote.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Red" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    rlNewNote.setBackgroundColor(Color.parseColor(selectedColor))

                }
                "Image" -> {

                    readStorageTask()

                }
                "Checkbox" -> {

                    addCheckbox()

                }
                "Download" -> {

                    downloadPdf()

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BroadcastReceiver)
    }

    companion object {
        private fun clearEditText(newNoteActivity: NewNoteActivity) {
            newNoteActivity.tvDate.setText("")
            newNoteActivity.edText.setText("")
            newNoteActivity.tvDate.requestFocus()
        }
    }

    private fun hasReadStoragePerm():Boolean{
        return EasyPermissions.hasPermissions(this,Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun readStorageTask(){
        if (hasReadStoragePerm()){

            Toast.makeText(this,"permission granted", Toast.LENGTH_SHORT).show()
            pickImageFromGallery()

        }else{
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your storage",
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageFromGallery(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent,REQUEST_CODE_IMAGE)

        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath:String? = null
        var cursor = this.contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){
            if ( data!= null){
                var selectedImageUrl = data.data

                if (selectedImageUrl != null){
                    try {
                        var inputStream = this.contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                        hasImage = true
                        Log.e("EEEE", hasImage.toString())
                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    }catch (e:Exception){
                        Log.e("eee347", e.message!!)
                    }
                }
            }
        }
    }

    private fun viewToBitmap():Bitmap{

        var dateBitmap = tvDate.drawToBitmap()
        var imgBitmap = imgNote.drawable.toBitmap()
        var textBitmap = edText.drawToBitmap()



        imgBitmap = Bitmap.createScaledBitmap(imgBitmap, imgBitmap.width * 3 / 2, imgBitmap.height * 3 / 2 , true)
        dateBitmap = Bitmap.createScaledBitmap(dateBitmap, dateBitmap.width * 5 / 2, dateBitmap.height * 5 / 2, true)
        textBitmap = Bitmap.createScaledBitmap(textBitmap, textBitmap.width * 5 / 2,  textBitmap.height * 5 / 2, true)

        val bmOverlay = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)

        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(imgBitmap, (bmp.width - imgBitmap.width)/2f, 800f, null)
        canvas.drawBitmap(textBitmap, 400F , imgBitmap.height + 1100F, null)
        canvas.drawBitmap(dateBitmap, 400F, 350F, null)
        var i = 0
        for(item in recyclerView.children){
            var itemBitmap = item.drawToBitmap()
            itemBitmap = Bitmap.createScaledBitmap(itemBitmap, item.width * 2, item.height * 2, true)
            canvas.drawBitmap(itemBitmap, 300F, imgBitmap.height + textBitmap.height + 1900F + 300F * i, null)
            i++
        }

        return bmOverlay

    }

    private fun downloadPdf(){

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        ) {
            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, STORAGE_CODE)
        } else {
            savePdf()
        }
    }

    private fun savePdf() {

        bmp = BitmapFactory.decodeResource(resources, R.drawable.emptytemplate)

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.color =  (Color.parseColor("#FFFFFF"))
        canvas.drawPaint(paint)

        val bmp = Bitmap.createScaledBitmap(bmp, bmp.width, bmp.height, true)
        canvas.drawBitmap(bmp, 0f, 0f, null)
        canvas.drawBitmap(viewToBitmap(), 0F, 0F, null)

        pdfDocument.finishPage(page)

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName + ".pdf"

        pdfDocument.writeTo(FileOutputStream(mFilePath))
        pdfDocument.close()

    }

    private fun addCheckbox() {
        val emptyCheckbox = CheckboxModel(0, intent.getIntExtra("ntId", 0), false, "")
        sqliteHelper.insertCheckbox(emptyCheckbox)
        getCheckboxes()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CheckboxAdapter()
        recyclerView.adapter = adapter
    }

    private fun getCheckboxes() {
        val cbList = sqliteHelper.getAllCheckbox(intent.getIntExtra("ntId", 0))

        adapter?.addItems(cbList)
    }

    private fun initView() {
        tvDate = findViewById(R.id.tvDate)
        edText = findViewById(R.id.edText)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnPreviousDays = findViewById(R.id.btnPreviousDay)
        btnNextDate = findViewById(R.id.btnNextDay)
        rlNewNote = findViewById(R.id.rlNewNote)
        recyclerView = findViewById(R.id.recyclerView)
        imgNote = findViewById(R.id.imgNote)
    }
}