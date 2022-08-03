package com.example.relationshipmanager

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.RelativeLayout.LayoutParams
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.example.relationshipmanager.adapters.EventAdapter
import com.example.relationshipmanager.databinding.ActivityMainBinding
import com.example.relationshipmanager.databinding.DialogInsertEventBinding
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.model.EventResult
import com.example.relationshipmanager.utilities.*
import com.example.relationshipmanager.viewmodels.MainViewModel
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import androidx.lifecycle.*
import com.example.relationshipmanager.activities.WelcomeActivity
import com.example.relationshipmanager.backup.BirdayImporter
import java.time.ZoneId
import java.util.*


@ExperimentalStdlibApi
class MainActivity : AppCompatActivity(), View.OnClickListener {

    val WRITE_RQ = 101
    val READ_RQ = 102

    //lateinit var mainViewModel: MainViewModel
    public val mainViewModel: MainViewModel by viewModels()
    private lateinit var adapter: EventAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private var _dialogInsertEventBinding: DialogInsertEventBinding? = null
    private val dialogInsertEventBinding get() = _dialogInsertEventBinding!!
    private lateinit var resultLauncher: ActivityResultLauncher<String>
    private var imageChosen = false

    // Insert the necessary information in the upcoming event cardview (and confetti)
    private fun insertUpcomingEvents(events: List<EventResult>) {
        var personName = ""
        var nextDateText = ""
        var nextAge = ""
        val upcomingDate = events[0].nextDate
        val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

        // Manage multiple events in the same day considering first case, middle cases and last case if more than 3
        for (event in events) {
            if (event.nextDate!!.isEqual(upcomingDate)) {
                // Consider the case of null surname and the case of unknown age
                val formattedPersonName =
                    formatName(event, sharedPrefs.getBoolean("surname_first", false))
                val age = if (event.yearMatter!!) event.nextDate.year.minus(event.originalDate.year)
                    .toString()
                else getString(R.string.unknown_age)
                when (events.indexOf(event)) {
                    0 -> {
                        personName = formattedPersonName
                        nextAge = getString(R.string.next_age_years) + ": $age"
                    }
                    1, 2 -> {
                        personName += ", $formattedPersonName"
                        nextAge += ", $age"
                    }
                    3 -> {
                        personName += " " + getString(R.string.event_others)
                        nextAge += "..."
                    }
                }
            }
            if (ChronoUnit.DAYS.between(event.nextDate, upcomingDate) < 0) break
        }
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        //mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        adapter = EventAdapter(null)
        // Initialize the result launcher to pick the image
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Handle the returned Uri
                if (uri != null) {
                    imageChosen = true
                    ////setImage(uri)
                }
            }

        // Retrieve the shared preferences
        val theme = sharedPrefs.getString("theme_color", "system")
        val accent = sharedPrefs.getString("accent_color", "aqua")

        // Show the introduction for the first launch
        if (sharedPrefs.getBoolean("first", true)) {
            val editor = sharedPrefs.edit()
            editor.putBoolean("first", false)
            editor.apply()
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        // Set the task appearance in recent apps
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(
                ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    R.mipmap.ic_launcher,
                    ContextCompat.getColor(this, R.color.deepGray)
                )
            )
        } else setTaskDescription(
            ActivityManager.TaskDescription(
                getString(R.string.app_name),
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher)?.toBitmap(),
                ContextCompat.getColor(this, R.color.deepGray)
            )
        )
        // Create the notification channel and check the permission (note: appIntro 6.0 is still buggy, better avoid to use it for asking permissions)
        ////askContactsPermission()
        createNotificationChannel()


        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        val buttonCreatePerson = findViewById<Button>(R.id.button)

        buttonCreatePerson.setOnClickListener {
            val intent = Intent(this, AddPerson::class.java)
            startActivity(intent)
        }

        buttonTaps()

        //test function to load values into table
        fillTable()

        renewAllReminders()

        /*
        //create an Event
        var nameValue = "Dieter"
        var surnameValue = "Muller"
        var eventDateValue: LocalDate = LocalDate.of(2022, 4, 30)
        var countYearValue = true

        var image: ByteArray? = null
        if (imageChosen)
            image =
                bitmapToByteArray(dialogInsertEventBinding.imageEvent.drawable.toBitmap())
        // Use the data to create an event object and insert it in the db
        val tuple = Event(
            id = 0,
            originalDate = eventDateValue,
            name = nameValue.smartCapitalize(),
            surname = surnameValue.smartCapitalize(),
            yearMatter = countYearValue,
            image = image,
        )

        mainViewModel.insert(tuple)

         */

        /*
        nameValue = "Sandra"
        surnameValue = "Muller"
        eventDateValue = LocalDate.of(2022, 4, 30)
        countYearValue = true

        image = null
        if (imageChosen)
            image =
                bitmapToByteArray(dialogInsertEventBinding.imageEvent.drawable.toBitmap())
        // Use the data to create an event object and insert it in the db
        val tuple1 = Event(
            id = 0,
            type = "anniversary",
            originalDate = eventDateValue,
            name = nameValue.smartCapitalize(),
            surname = surnameValue.smartCapitalize(),
            yearMatter = countYearValue,
            image = image,
        )
        mainViewModel.insert(tuple1)

         */


        mainViewModel.scheduleNextCheck()



        /*
        // Insert using another thread
        val thread = Thread { mainViewModel.insert(tuple) }
        thread.start()
         */

        /* not sure what this func was meant to do
        mainViewModel.allEvents.observe(this, { events ->
            // Manage placeholders, search results and the main list
            events?.let { adapter.submitList(it) }
            if (events.isNotEmpty()) {
                insertUpcomingEvents(events)
            }
            //if (events.isEmpty())
            //if (events.isEmpty() && mainViewModel.searchString.value!!.isNotBlank())
        })
        */

    }


    //birday stuff


    override fun onSaveInstanceState(outState: Bundle) {
        // Manage refresh from settings, since there's a bug where the refresh doesn't work properly
        val refreshed = sharedPrefs.getBoolean("refreshed", false)
        if (refreshed) {
            sharedPrefs.edit().putBoolean("refreshed", false).apply()
            super.onSaveInstanceState(outState)
        } else {
            // Dirty, dirty fix to avoid TransactionTooBigException:
            // it will restore the home fragment when the theme is changed from system for example,
            // and the app is in recent apps. No issues for screen rotations, keyboard and so on
            super.onSaveInstanceState(Bundle())
        }
    }

    // Choose a backup registering a callback and following the latest guidelines
    val selectBackup =
        registerForActivityResult(ActivityResultContracts.GetContent()) { fileUri: Uri? ->
            try {
                val birdayImporter = BirdayImporter(this, null)
                if (fileUri != null) birdayImporter.importBirthdays(this, fileUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }



    // Create the NotificationChannel. This code does nothing when it already exists
    private fun createNotificationChannel() {
        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.birday_notification)
        val attributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val name = getString(R.string.events_notification_channel)
        val descriptionText = getString(R.string.events_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("events_channel", name, importance).apply {
            description = descriptionText
        }
        // Additional tuning over sound, vibration and notification light
        channel.setSound(soundUri, attributes)
        channel.enableLights(true)
        channel.lightColor = Color.GREEN
        channel.enableVibration(true)
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }




    private fun renewAllReminders() {
        //load data from excel file
        val excelFile = getExcelFile()
        val excelWb = excelFile?.let { readExcelAsWorkbook(it) }
        val excelSheet = excelWb?.let { getSheet(it, 0) }

        //set reminder for each row, TODO check if birthday is actually set, and later if birthday or other event
        for (i in 1..excelSheet!!.lastRowNum) { //row 0 is filled with header, end should be number of contacts
            val excelRow = excelSheet?.let { getRow(it, i) } //copy row from Excel

            val firstName = excelRow?.let { getCellString(it, 0) }
            val lastName = excelRow?.let { getCellString(it, 1) }
            val birthday = excelRow?.getCell(7)?.dateCellValue
            val date = birthday?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

            addReminder(firstName, lastName, date, true)
        }

        mainViewModel.scheduleNextCheck()
    }



    private fun addReminder(nameValue: String?, surnameValue: String?, eventDateValue: LocalDate?, countYearValue: Boolean) {
        //create an Event
        var image: ByteArray? = null
        if (imageChosen)
            image =
                bitmapToByteArray(dialogInsertEventBinding.imageEvent.drawable.toBitmap())
        // Use the data to create an event object and insert it in the db
        val tuple = eventDateValue?.let {
            Event(
                id = 0,
                originalDate = it,
                name = nameValue?.smartCapitalize(),
                surname = surnameValue?.smartCapitalize(),
                yearMatter = countYearValue,
                image = image,
            )
        }
        if (tuple != null) {
            mainViewModel.insert(tuple)
        }
    }






    //Excel stuff

    private fun fillTable() {
        //load data from excel file
        val excelFile = getExcelFile()
        val excelWb = excelFile?.let { readExcelAsWorkbook(it) }
        val excelSheet = excelWb?.let { getSheet(it, 0) }

        //border for TextView cells, needs to be set
        val border = ShapeDrawable(RectShape())
        border.paint.style = Paint.Style.STROKE
        border.paint.color = Color.BLACK

        //alternate border background for button, above didnt work
        val gd = GradientDrawable()
        gd.setColor(0xFF0FF00) // Changes this drawbale to use a single color instead of a gradient
        gd.cornerRadius = 5f
        gd.setStroke(1, -0xFF00000)

        //load data from excel file to table in activity
        val table = findViewById<TableLayout>(R.id.table_contacts)
        val lp = LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT) //layout for rows of Android table
        for (i in 1..excelSheet!!.lastRowNum) { //row 0 is filled with header, end should be number of contacts
            val row = TableRow(this) //create row in Android table
            row.layoutParams = lp //assign layout to row

            val excelRow = excelSheet?.let { getRow(it, i) } //copy row from Excel

            for (j in 0..3) {
                val textView = TextView(this) //create Android TextView
                //config various properties of TextView, color is really important, border good for debugging, padding for reading, gravity necessary?, Layout for spacing
                textView.textSize = 14f
                textView.setTextColor(Color.BLACK)
                textView.background = border
                textView.setPadding(10,10,10,10)
                textView.gravity = Gravity.CENTER_HORIZONTAL;
                textView.layoutParams = TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, 80, 4.0f) //height to 80 to fit to button
                when (j) {
                    0 -> {
                        val button = Button(this)
                        button.textSize = 14f
                        button.setTextColor(Color.BLACK)
                        button.background = gd;
                        button.setPadding(10,10,10,10)
                        button.gravity = Gravity.CENTER_HORIZONTAL
                        button.layoutParams = TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, 80, 1.0f) //height to 80 because automatic height is too high (WRAP Content)
                        button.text = excelRow?.let { getCellString(it, 1) }
                        button.minimumWidth = 0 //apparently to reduce in-button padding, which would cut off text
                        button.minimumHeight = 0
                        button.tag = i

                        //general OnClick function described in override at bottom
                        button.setOnClickListener(this) //can uncomment it when class wide onclicklistener is uncommented

                        row.addView(button)
                    }
                    1 -> {
                        textView.text = excelRow?.let { getCellString(it, 0) }
                        row.addView(textView)
                    }
                    2 -> {
                        textView.text = excelRow?.let { getCellString(it, 7) }
                        row.addView(textView)
                    }
                    3 -> {
                        textView.text = excelRow?.let { getCellString(it, 2) }
                        row.addView(textView)
                    }
                }
                //row.addView(textView)
            }
            table.addView(row, i)
        }


        /*
        val tableLayout = TableLayout(this)
        val tr1 = TableRow(this)
        val tr2 = TableRow(this)
        val tr3 = TableRow(this)
        val b1 = Button(this)
        val b2 = Button(this)
        val b3 = Button(this)
        val b4 = Button(this)
        val b5 = Button(this)
        val b6 = Button(this)
        val tv1 = TextView(this)
        val tv2 = TextView(this)
        val tv3 = TextView(this)

        tv1.background = border
        tv2.background = border
        tv3.background = border

        val lp = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
        tableLayout.layoutParams = lp
        b1.setText("Col1")
        b2.setText("Col2")
        b3.setText("Col3")
        b4.setText("Col4")
        b5.setText("Col5")
        b6.setText("Col6")
        tr1.addView(b1)
        tr1.addView(b2)
        tr1.addView(b3)
        tr1.addView(b4)
        tr2.addView(b5)
        tr2.addView(b6)
        tr3.addView(tv1)
        tr3.addView(tv2)
        tr3.addView(tv3)
        tableLayout.isStretchAllColumns = true
        tableLayout.addView(tr1,0)
        tableLayout.addView(tr2,0)
        findViewById<RelativeLayout>(R.id.relative_layout).addView(tableLayout)

         */





/*
        val tableLayout = TableLayout(this).apply {
            val params = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.BELOW, R.id.table_contacts)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.YELLOW)
            params.topMargin = 10
            layoutParams = params
        }

        //val table = findViewById<TableLayout>(R.id.table_contacts)
        val row1 = TableRow(this).apply {
            setBackgroundColor(Color.GRAY)
            layoutParams = TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val row2 = TableRow(this).apply {
            setBackgroundColor(Color.MAGENTA)
            layoutParams = TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        row1.addView(TextView(this).apply {
            setText("haha")
            setBackgroundColor(Color.RED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(10,10,10,10)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4F)
            //TableRow.LayoutParams.WRAP_CONTENT
        })
        row1.addView(TextView(this).apply {
            setText("haha")
            setBackgroundColor(Color.BLUE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(10,10,10,10)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4F)
        })

        row2.addView(TextView(this).apply {
            text = "hheeheeee"
            textSize = 14F
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(10,10,10,10)
            layoutParams = TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 4F)
        })

        tableLayout.addView(row1)
        tableLayout.addView(row2)
        findViewById<RelativeLayout>(R.id.relative_layout).addView(tableLayout)

 */
    }


    //
    //reading Excel file part
    //
    private fun getExcelFile(): File? {

        //Get App Director, APP_DIRECTORY_NAME is a string
        val appDirectory = this.getExternalFilesDir(null)

        //Check App Directory whether it exists or not
        appDirectory?.let {

            //Check if file exists or not
            if (it.exists()) {

                //return excel file
                return File(appDirectory, "contacts.xlsx")
            }
        }

        return null
    }

    private fun readExcelAsWorkbook(file: File): Workbook? {

        //Reading excel file
        file.let {
            try {

                //Reading excel file as stream
                val inputStream = FileInputStream(it)

                //Return workbook
                return WorkbookFactory.create(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e("Relman Error","readExcelData: FileNotFoundException. " + e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Relman Error", "readExcelData: Error reading inputstream. " + e.message)
            }

        }

        return null
    }

    private fun getSheet(workbook: Workbook, index: Int): Sheet? {

        //Checking that sheet exist
        //This function will also tell you total number of sheets
        if (workbook != null) {
            if (workbook.numberOfSheets > 0) {

                //Return indexed sheet of excel Workbook
                return workbook.getSheetAt(index)
            }
        }

        return null
    }

    private fun getRow(sheet: Sheet, index: Int): Row? {

        //To find total number of rows
        val totalRows = sheet.physicalNumberOfRows

        if (totalRows > index) {
            //Get indexed row
            if (sheet != null) {
                return sheet.getRow(index)
            }
        }

        return null
    }

    private fun getCellString(row: Row, index: Int): String? {
        //Total number of cells of a row
        val totalColumns = row.physicalNumberOfCells

        //to format data to String or whatever
        val df = DataFormatter()

        val cell: Cell
        if (totalColumns > index) {
            //Get value of first cell from row
            if (row != null) {
                cell = row.getCell(index)
                //return cell String
                return df.formatCellValue(cell)
            }
        }

        return null
    }















    //
    //Permission Stuff
    //
    private fun buttonTaps() {
        val btnWritePerm = findViewById<Button>(R.id.buttonWritePerm)
        val btnReadPerm = findViewById<Button>(R.id.buttonReadPerm)

        btnWritePerm.setOnClickListener() {
            checkForPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, "write", WRITE_RQ)
        }
        btnReadPerm.setOnClickListener() {
            checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, "read", READ_RQ)
        }
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granetd", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when (requestCode) {
            WRITE_RQ -> innerCheck("write")
            READ_RQ -> innerCheck("read")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onClick(p0: View?) {
        val intent = Intent(this, EditPerson::class.java)
        intent.putExtra("tag", p0?.tag.toString())
        startActivity(intent)
    }


}