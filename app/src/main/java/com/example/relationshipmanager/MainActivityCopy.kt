package com.example.relationshipmanager

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.widget.RelativeLayout.LayoutParams
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import org.apache.poi.ss.usermodel.*
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetColorVal.BLACK
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


@ExperimentalStdlibApi
class MainActivityCopy : AppCompatActivity() {

    val WRITE_RQ = 101
    val READ_RQ = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonCreatePerson = findViewById<Button>(R.id.button)

        buttonCreatePerson.setOnClickListener {
            val intent = Intent(this, AddPerson::class.java)
            startActivity(intent)
        }

        buttonTaps()

        fillTable()
    }

    private fun fillTable() {
        //load data from excel file
        val excelFile = getExcelFile()
        val excelWb = excelFile?.let { readExcelAsWorkbook(it) }
        val excelSheet = excelWb?.let { getSheet(it, 0) }
        val excelRow = excelSheet?.let { getRow(it, 1) }
        //fill cells with data
        /*
        findViewById<TextView>(R.id.cell_2_1).text = excelRow?.let { getCellString(it, 1) }
        findViewById<TextView>(R.id.cell_2_2).text = excelRow?.let { getCellString(it, 0) }
        findViewById<TextView>(R.id.cell_2_3).text = excelRow?.let { getCellString(it, 7) }
        findViewById<TextView>(R.id.cell_2_4).text = excelRow?.let { getCellString(it, 2) }

         */

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
        tr2.addView(b4)
        tr2.addView(b5)
        tr2.addView(b6)
        tr3.addView(tv1)
        tr3.addView(tv2)
        tr3.addView(tv3)
        tableLayout.isStretchAllColumns = true
        tableLayout.addView(tr1)
        tableLayout.addView(tr2)
        findViewById<RelativeLayout>(R.id.relative_layout).addView(tableLayout)

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
                ActivityCompat.requestPermissions(this@MainActivityCopy, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}