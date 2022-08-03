package com.example.relationshipmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.relationshipmanager.adapters.EventAdapter
import com.example.relationshipmanager.backup.BirdayImporter
import com.example.relationshipmanager.databinding.ActivityMainBinding
import com.example.relationshipmanager.databinding.DialogInsertEventBinding
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.utilities.bitmapToByteArray
import com.example.relationshipmanager.utilities.smartCapitalize
import com.example.relationshipmanager.viewmodels.MainViewModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.apache.poi.ss.usermodel.*
import java.io.*
import java.util.*
import org.apache.poi.ss.usermodel.Sheet as Sheet
import org.apache.poi.ss.usermodel.Cell

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.*
import java.time.LocalDate

@ExperimentalStdlibApi
class AddPerson : AppCompatActivity() {

    //val mainViewModel: MainViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private var _dialogInsertEventBinding: DialogInsertEventBinding? = null
    private val dialogInsertEventBinding get() = _dialogInsertEventBinding!!
    private var imageChosen = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_person)

        //get all EditText, Button and DatePicker objects
        val textFirstName = findViewById<TextView>(R.id.editTextFirstName)
        val textLastName = findViewById<TextView>(R.id.editTextLastName)
        val textMobile = findViewById<TextView>(R.id.editTextMobile)
        val textTelephone = findViewById<TextView>(R.id.editTextPhone)
        val textEmailAddress = findViewById<TextView>(R.id.editTextEmail)
        val textPostalAddress = findViewById<TextView>(R.id.editTextPostalAddress)
        val textNotes = findViewById<TextView>(R.id.editTextNotes)
        val buttonAddPerson = findViewById<Button>(R.id.button)
        val dateBirthday = findViewById<DatePicker>(R.id.datePickerBirthday)

        val button: Button = findViewById(R.id.button)

        //OnClick to add a person to the Excel file
        button.setOnClickListener{ view ->
            Toast.makeText(this@AddPerson, "called startDasboard",Toast.LENGTH_SHORT).show()

            //read all EditText fields, TODO empty fields might need to be reasked, at least essential field (name, number or address)
            val firstName = textFirstName.text.toString()
            val lastName = textLastName.text.toString()
            val birthday = Calendar.getInstance()
            val mobileNumber = textMobile.text.toString()
            val telephoneNumber = textTelephone.text.toString()
            val emailAddress = textEmailAddress.text.toString()
            val postalAddress = textPostalAddress.text.toString()
            val notes = textNotes.text.toString()
            birthday.set(dateBirthday.year, dateBirthday.month, dateBirthday.dayOfMonth)
            //Toast.makeText(this, "$firstName $lastName.", Toast.LENGTH_LONG).show()

            //val filepath = "./test.xlsx"
            //writeToExcelFile(filepath)
            //readFromExcelFile(filepath)


            //load and read Excel file, create workbook, extract first sheet, variables here
            var excelFile = getExcelFile()
            var excelWb = excelFile?.let { readExcelAsWorkbook(it) }
            var excelSheet: Sheet?

            //null check for loaded file, basically if else
            if (excelWb != null) {

                //create workbook and extract first sheet from file
                //excelWb = readExcelAsWorkbook(excelFile)
                excelSheet = excelWb?.let { it -> getSheet(it, 0) }

                //test how many rows were loaded
                excelSheet?.let { it1 -> Toast.makeText(this, "row " + it1.lastRowNum, Toast.LENGTH_SHORT).show() }

                //Adding data to the sheet
                saveContactToRow(excelWb as XSSFWorkbook,
                    excelSheet as XSSFSheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday)
                //actually create Excel file, "contacts.xlsx"
                createExcel(excelWb as XSSFWorkbook)



            }  else {

                // Create excel workbook (code only)
                excelWb = XSSFWorkbook()
                excelSheet = (excelWb as XSSFWorkbook).createSheet("Sheet")

                //test how many rows were loaded
                Toast.makeText(this, "new " + excelSheet.lastRowNum, Toast.LENGTH_SHORT).show()

                //Create Header Cell Style
                val cellStyle = getHeaderStyle(excelWb as XSSFWorkbook)
                //Create sheet header row
                createSheetHeader(cellStyle, excelSheet as XSSFSheet)
                //Adding data to the sheet
                saveContactToRow(excelWb as XSSFWorkbook,
                    excelSheet as XSSFSheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday)
                //actually create Excel file, "contacts.xlsx"
                createExcel(excelWb as XSSFWorkbook)


            }


            /*


            // Create excel workbook (code only)
            val workbook = XSSFWorkbook()

            //Create first sheet inside workbook (code only)
            //Constants.SHEET_NAME is a string value of sheet name
            val sheet = workbook.createSheet("Sheet")

            //Create Header Cell Style
            val cellStyle = getHeaderStyle(workbook)

            //Create sheet header row
            createSheetHeader(cellStyle, sheet)

            //Adding data to the sheet
            saveContactToRow(workbook, sheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday)

            //actually create Excel file, "contacts.xlsx"
            createExcel(workbook)


            Toast.makeText(this, "before wrote file", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, sheet.lastRowNum, Toast.LENGTH_SHORT).show()

            //createSaveExcel()

             */

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

    private fun saveContactToRow(workbook: XSSFWorkbook, sheet: Sheet, firstName: String, lastName: String, mobileNumber: String, telephoneNumber: String, emailAddress: String, postalAddress: String, notes: String, birthday: Calendar) {
        val row = sheet.createRow(sheet.lastRowNum + 1)
        row.createCell(0).setCellValue(firstName)
        row.createCell(1).setCellValue(lastName)
        row.createCell(2).setCellValue(mobileNumber)
        row.createCell(3).setCellValue(telephoneNumber)
        row.createCell(4).setCellValue(emailAddress)
        row.createCell(5).setCellValue(postalAddress)
        row.createCell(6).setCellValue(notes)
        val cellBirthday = row.createCell(7)
        cellBirthday.setCellValue(birthday)
        val cellStyleDate : XSSFCellStyle = workbook.createCellStyle()
        val df = workbook.createDataFormat().getFormat("dd.mm.yyyy")
        cellStyleDate.dataFormat = df
        cellBirthday.cellStyle = cellStyleDate
    }
    /*
    public fun createSaveExcel() {
        val xlWb = XSSFWorkbook()
        val xlWs = xlWb.createSheet()
        val xlRow = xlWs.createRow(0)
        val xlCel = xlRow.createCell(0)

        xlCel.setCellValue("haha")

        val filePath = this.getExternalFilesDir(null)
        val outputFile = File(filePath, "test.xlsx")

        try {
            //val folder = File(filePath, "avalakki")
            //folder.mkdirs()
            //println(folder.exists())



            val fileOutputStream = FileOutputStream(outputFile)
            xlWb.write(fileOutputStream)
            Toast.makeText(this, "wrote file", Toast.LENGTH_SHORT).show()

            if (fileOutputStream != null) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        } catch (e: IOException) {
                e.printStackTrace()
        }


    }
     */
    /*
    /**
     * Writes the value "TEST" to the cell at the first row and first column of worksheet.
     */
    private fun writeToExcelFile(filepath: String) {
        //Instantiate Excel workbook:
        val xlWb = XSSFWorkbook()
        //Instantiate Excel worksheet:
        val xlWs = xlWb.createSheet()

        //Row index specifies the row in the worksheet (starting at 0):
        val rowNumber = 0
        //Cell index specifies the column within the chosen row (starting at 0):
        val columnNumber = 0

        //Write text value to cell located at ROW_NUMBER / COLUMN_NUMBER:
        xlWs.createRow(rowNumber).createCell(columnNumber).setCellValue("TEST")

        //Write file:
        val outputStream = FileOutputStream(filepath)
        xlWb.write(outputStream)
        xlWb.close()
    }

    /**
     * Reads the value from the cell at the first row and first column of worksheet.
     */
    private fun readFromExcelFile(filepath: String) {
        val inputStream = FileInputStream(filepath)
        //Instantiate Excel workbook using existing file:
        var xlWb = WorkbookFactory.create(inputStream)

        //Row index specifies the row in the worksheet (starting at 0):
        val rowNumber = 0
        //Cell index specifies the column within the chosen row (starting at 0):
        val columnNumber = 0

        //Get reference to first sheet:
        val xlWs = xlWb.getSheetAt(0)
        println(xlWs.getRow(rowNumber).getCell(columnNumber))
    }*/


    private fun createWorkbook(): Workbook {
        // Creating excel workbook
        val workbook = XSSFWorkbook()

        //Creating first sheet inside workbook
        //Constants.SHEET_NAME is a string value of sheet name
        val sheet = workbook.createSheet("Sheet")

        //Create Header Cell Style
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        //Adding data to the sheet
        addData(0, sheet)

        return workbook
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Create sheet first row
        val row = sheet.createRow(0)

        //Header list
        val HEADER_LIST = listOf("column_1", "column_2", "column_3", "column_4", "column_5", "column_6", "column_7", "column_8")

        //Loop to populate each column of header row
        for ((index, value) in HEADER_LIST.withIndex()) {

            val columnWidth = (15 * 500)

            //index represents the column number
            sheet.setColumnWidth(index, columnWidth)

            //Create cell
            val cell = row.createCell(index)

            //value represents the header value from HEADER_LIST
            cell?.setCellValue(value)

            //Apply style to cell
            cell.cellStyle = cellStyle
        }

        //Add data to the header cells
        row.createCell(0).setCellValue("First name")
        row.createCell(1).setCellValue("Last name")
        row.createCell(2).setCellValue("Mobile Number")
        row.createCell(3).setCellValue("Phone Number")
        row.createCell(4).setCellValue("Email Address")
        row.createCell(5).setCellValue("Postal Address")
        row.createCell(6).setCellValue("Notes")
        row.createCell(7).setCellValue("Birthday")
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {

        //Cell style for header row
        val cellStyle: CellStyle = workbook.createCellStyle()

        //Apply cell color
        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        var color = XSSFColor(IndexedColors.RED, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        //Apply font style on cell text
        val whiteFont = workbook.createFont()
        color = XSSFColor(IndexedColors.WHITE, colorMap).indexed
        whiteFont.color = color
        whiteFont.bold = true
        cellStyle.setFont(whiteFont)


        return cellStyle
    }


    private fun addData(rowIndex: Int, sheet: Sheet) {

        //Create row based on row index
        val row = sheet.createRow(rowIndex)

        //Add data to each cell
        createCell(row, 0, "value 1") //Column 1
        createCell(row, 1, "value 2") //Column 2
        createCell(row, 2, "value 3") //Column 3
    }

    private fun createCell(row: Row, columnIndex: Int, value: String) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
    }

    private fun createExcel(workbook: Workbook) {

        //Get App Director, APP_DIRECTORY_NAME is a string
        val appDirectory = this.getExternalFilesDir(null)

        //Check App Directory whether it exists or not, create if not.
        if (appDirectory != null && !appDirectory.exists()) {
            appDirectory.mkdirs()
        }

        //Create excel file with extension .xlsx
        val excelFile = File(appDirectory,"contacts.xlsx")

        //Write workbook to file using FileOutputStream
        try {
            val fileOut = FileOutputStream(excelFile)
            workbook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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






}