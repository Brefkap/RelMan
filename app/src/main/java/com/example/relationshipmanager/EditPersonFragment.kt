package com.example.relationshipmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.model.EventCode
import com.example.relationshipmanager.utilities.bitmapToByteArray
import com.example.relationshipmanager.utilities.smartCapitalize
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import java.io.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

var excelWb: Workbook? = null
var excelSheet: Sheet? = null
var rowNr: Int = 0

/**
 * A simple [Fragment] subclass.
 * Use the [EditPersonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalStdlibApi
class EditPersonFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_person, container, false)

        //get intent parameters
        //val b = intent.extras
        //rowNr = b?.getString("tag")?.toInt()!!
        //Toast.makeText(activity, "rowNr $rowNr", Toast.LENGTH_LONG).show()
        val bundle = arguments
        rowNr = bundle?.getInt("rowNr") ?: 2


        val textFirstName = view.findViewById<TextView>(R.id.editTextFirstName)
        val textLastName = view.findViewById<TextView>(R.id.editTextLastName)
        val textMobile = view.findViewById<TextView>(R.id.editTextMobile)
        val textTelephone = view.findViewById<TextView>(R.id.editTextPhone)
        val textEmailAddress = view.findViewById<TextView>(R.id.editTextEmail)
        val textPostalAddress = view.findViewById<TextView>(R.id.editTextPostalAddress)
        val textNotes = view.findViewById<TextView>(R.id.editTextNotes)
        val button = view.findViewById<Button>(R.id.button)
        val dateBirthday = view.findViewById<DatePicker>(R.id.datePickerBirthday)

        var excelFile = getExcelFile()
        //var excelWb: Workbook?
        //var excelSheet: Sheet?
        var excelRow: Row?

        //null check for loaded file, basically if else
        if (excelFile != null) {

            //create workbook and extract first sheet from file
            excelWb = readExcelAsWorkbook(excelFile)
            excelSheet = excelWb?.let { it -> getSheet(it, 0) }

            //test how many rows were loaded
            //excelSheet?.let { it1 -> Toast.makeText(activity, "row " + it1.lastRowNum, Toast.LENGTH_SHORT).show() }

            excelRow = getRow(excelSheet, rowNr)

            textFirstName.text = excelRow?.let { getCellString(it, 0) }
            textLastName.text = excelRow?.let { getCellString(it, 1) }
            textMobile.text = excelRow?.let { getCellString(it, 2) }
            textTelephone.text = excelRow?.let { getCellString(it, 3) }
            textPostalAddress.text = excelRow?.let { getCellString(it, 4) }
            textEmailAddress.text = excelRow?.let { getCellString(it, 5) }
            textNotes.text = excelRow?.let { getCellString(it, 6) }
            val birthday = excelRow?.getCell(7)?.dateCellValue?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            birthday?.year?.let { dateBirthday.updateDate(it, birthday.monthValue - 1, birthday.dayOfMonth) }

            //Adding data to the sheet
            //saveContactToRow(excelWb as XSSFWorkbook, excelSheet as XSSFSheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday)
            //actually create Excel file, "contacts.xlsx"
            //createExcel(excelWb as XSSFWorkbook)

        }  else {
            Toast.makeText(activity, "EditPerson Fileload problem", Toast.LENGTH_SHORT).show()
        }

        button.setOnClickListener {
            val firstName = textFirstName.text.toString()
            val lastName = textLastName.text.toString()
            val birthday = Calendar.getInstance()
            val mobileNumber = textMobile.text.toString()
            val telephoneNumber = textTelephone.text.toString()
            val emailAddress = textEmailAddress.text.toString()
            val postalAddress = textPostalAddress.text.toString()
            val notes = textNotes.text.toString()
            birthday.set(dateBirthday.year, dateBirthday.month, dateBirthday.dayOfMonth)
            //Toast.makeText(this, "$firstName $lastName $birthdayDay.$birthdayMonth.$birthdayYear.", Toast.LENGTH_LONG).show()

            saveContactToRow(
                excelWb as XSSFWorkbook,
                excelSheet as XSSFSheet,
                rowNr,
                firstName,
                lastName,
                mobileNumber,
                telephoneNumber,
                emailAddress,
                postalAddress,
                notes,
                birthday
            )

            //load and read Excel file, create workbook, extract first sheet, variables here


            //actually create Excel file, "contacts.xlsx"
            createExcel(excelWb as XSSFWorkbook)

            //Toast.makeText(activity, "before wrote file", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ContactsFragment()).commit()

            //createSaveExcel()


            //val intent = Intent(this, MainActivity::class.java)
            //startActivity(intent)
            val date = birthday?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            addReminder(firstName, lastName, date, true)

            Toast.makeText(activity, "Changes saved.", Toast.LENGTH_LONG).show()
        }


        // Inflate the layout for this fragment
        return view
    }

        private fun saveContactToRow(
            workbook: XSSFWorkbook,
            sheet: Sheet,
            rowNr: Int,
            firstName: String,
            lastName: String,
            mobileNumber: String,
            telephoneNumber: String,
            emailAddress: String,
            postalAddress: String,
            notes: String,
            birthday: Calendar
        ) {
            val row = sheet.getRow(rowNr)
            row.createCell(0).setCellValue(firstName)
            row.createCell(1).setCellValue(lastName)
            row.createCell(2).setCellValue(mobileNumber)
            row.createCell(3).setCellValue(telephoneNumber)
            row.createCell(4).setCellValue(emailAddress)
            row.createCell(5).setCellValue(postalAddress)
            row.createCell(6).setCellValue(notes)
            val cellBirthday = row.createCell(7)
            cellBirthday.setCellValue(birthday)
            val cellStyleDate: XSSFCellStyle = workbook.createCellStyle()
            val df = workbook.createDataFormat().getFormat("dd.mm.yyyy")
            cellStyleDate.dataFormat = df
            cellBirthday.cellStyle = cellStyleDate
        }


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
            val HEADER_LIST = listOf(
                "column_1",
                "column_2",
                "column_3",
                "column_4",
                "column_5",
                "column_6",
                "column_7",
                "column_8"
            )

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
            val appDirectory = activity?.getExternalFilesDir(null)

            //Check App Directory whether it exists or not, create if not.
            if (appDirectory != null && !appDirectory.exists()) {
                appDirectory.mkdirs()
            }

            //Create excel file with extension .xlsx
            val excelFile = File(appDirectory, "contacts.xlsx")

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
            val appDirectory = activity?.getExternalFilesDir(null)

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

        private fun getRow(sheet: Sheet?, rowNum: Int?): Row? {
            //get sheet
            sheet?.let { sheet ->

                //To find total number of rows
                val totalRows = sheet.physicalNumberOfRows

                //Total number of cells of a row
                val totalColumns = sheet.getRow(0).physicalNumberOfCells

                return rowNum?.let { sheet.getRow(it) }
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

    @ExperimentalStdlibApi
    private fun addReminder(nameValue: String?, surnameValue: String?, eventDateValue: LocalDate?, countYearValue: Boolean) {
        //create an Event
        var image: ByteArray? = null

        val mainActivity : MainActivityBottomNav = activity as MainActivityBottomNav
        val mainViewModel = mainActivity.mainViewModel
        val imageChosen = mainActivity.imageChosen
        //val dialogInsertEventBinding : DialogInsertEventBinding = mainActivity.dialogInsertEventBinding

        if (imageChosen)
            image =
                bitmapToByteArray(mainActivity.dialogInsertEventBinding.imageEvent.drawable.toBitmap())
        // Use the data to create an event object and insert it in the db
        val tuple = eventDateValue?.let {
            Event(
                id = 0,
                originalDate = it,
                type = EventCode.BIRTHDAY.name,
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



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditPersonFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditPersonFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}