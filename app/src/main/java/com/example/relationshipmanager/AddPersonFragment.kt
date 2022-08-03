package com.example.relationshipmanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import com.example.relationshipmanager.databinding.DialogInsertEventBinding
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddPersonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalStdlibApi
class AddPersonFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_person, container, false)
        //get all EditText, Button and DatePicker objects
        val textFirstName = view.findViewById<TextView>(R.id.editTextFirstName)
        val textLastName = view.findViewById<TextView>(R.id.editTextLastName)
        val textMobile = view.findViewById<TextView>(R.id.editTextMobile)
        val textTelephone = view.findViewById<TextView>(R.id.editTextPhone)
        val textEmailAddress = view.findViewById<TextView>(R.id.editTextEmail)
        val textPostalAddress = view.findViewById<TextView>(R.id.editTextPostalAddress)
        val textNotes = view.findViewById<TextView>(R.id.editTextNotes)
        val dateBirthday = view.findViewById<DatePicker>(R.id.datePickerBirthday)
        val intervalPicker = view.findViewById<NumberPicker>(R.id.numberPickerInterval)
        val lastContacted = view.findViewById<DatePicker>(R.id.datePickerLastContacted)
        intervalPicker.maxValue = 60
        intervalPicker.minValue = 0 //zero means no regular contacting

        val button: Button = view.findViewById(R.id.button)

        //OnClick to add a person to the Excel file
        button.setOnClickListener{ view ->
            //Toast.makeText(activity, "called startDasboard", Toast.LENGTH_SHORT).show()

            //read all EditText fields, TODO empty fields might need to be reasked, at least essential field (name, number or address)
            val firstName = textFirstName.text.toString()
            val lastName = textLastName.text.toString()
            val birthday = Calendar.getInstance()
            birthday.set(dateBirthday.year, dateBirthday.month, dateBirthday.dayOfMonth)
            val mobileNumber = textMobile.text.toString()
            val telephoneNumber = textTelephone.text.toString()
            val emailAddress = textEmailAddress.text.toString()
            val postalAddress = textPostalAddress.text.toString()
            val notes = textNotes.text.toString()
            val contactInterval = intervalPicker.value.toString()
            val lastContactedDay = Calendar.getInstance()
            lastContactedDay.set(lastContacted.year, lastContacted.month, lastContacted.dayOfMonth)
            //Toast.makeText(activity, "$firstName $lastName.", Toast.LENGTH_LONG).show()

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
                //excelSheet?.let { it1 -> Toast.makeText(this, "row " + it1.lastRowNum, Toast.LENGTH_SHORT).show() }

                //Adding data to the sheet
                saveContactToRow(excelWb as XSSFWorkbook,
                    excelSheet as XSSFSheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday, contactInterval, lastContactedDay)
                //actually create Excel file, "contacts.xlsx"
                createExcel(excelWb as XSSFWorkbook)



            }  else {

                // Create excel workbook (code only)
                excelWb = XSSFWorkbook()
                excelSheet = (excelWb as XSSFWorkbook).createSheet("Sheet")

                //test how many rows were loaded
                Toast.makeText(activity, "new " + excelSheet.lastRowNum, Toast.LENGTH_SHORT).show()

                //Create Header Cell Style
                val cellStyle = getHeaderStyle(excelWb as XSSFWorkbook)
                //Create sheet header row
                createSheetHeader(cellStyle, excelSheet as XSSFSheet)
                //Adding data to the sheet
                saveContactToRow(excelWb as XSSFWorkbook,
                    excelSheet as XSSFSheet, firstName, lastName, mobileNumber, telephoneNumber, emailAddress, postalAddress, notes, birthday, contactInterval, lastContactedDay)
                //actually create Excel file, "contacts.xlsx"
                createExcel(excelWb as XSSFWorkbook)


            }

            val date = birthday?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            //addReminder(firstName, lastName, date, true)
            addReachOutReminder(firstName, lastName, date)
            Toast.makeText(activity, "Contact added.", Toast.LENGTH_LONG).show()
        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddPersonFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddPersonFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun saveContactToRow(workbook: XSSFWorkbook, sheet: Sheet, firstName: String, lastName: String, mobileNumber: String, telephoneNumber: String, emailAddress: String, postalAddress: String, notes: String, birthday: Calendar, contactInterval: String, lastContactedDay: Calendar) {
        val row = sheet.createRow(sheet.lastRowNum + 1)
        row.createCell(0).setCellValue(firstName)
        row.createCell(1).setCellValue(lastName)
        row.createCell(2).setCellValue(mobileNumber)
        row.createCell(3).setCellValue(telephoneNumber)
        row.createCell(4).setCellValue(emailAddress)
        row.createCell(5).setCellValue(postalAddress)
        row.createCell(6).setCellValue(notes)
        row.createCell(8).setCellValue(contactInterval)
        val cellBirthday = row.createCell(7)
        cellBirthday.setCellValue(birthday)
        val lastContacted = row.createCell(9)
        lastContacted.setCellValue(lastContactedDay)

        val cellStyleDate : XSSFCellStyle = workbook.createCellStyle()
        val df = workbook.createDataFormat().getFormat("dd.mm.yyyy")
        cellStyleDate.dataFormat = df

        cellBirthday.cellStyle = cellStyleDate
        lastContacted.cellStyle = cellStyleDate
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
        row.createCell(8).setCellValue("Contact Interval")
        row.createCell(9).setCellValue("Last contacted")
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

    private fun addReachOutReminder(nameValue: String?, surnameValue: String?, eventDateValue: LocalDate?) {
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
                type = EventCode.REACHOUT.name,
                originalDate = it,
                name = nameValue?.smartCapitalize(),
                surname = surnameValue?.smartCapitalize(),
                favorite = false,
                yearMatter = false,
                notes = "Reach out!",
                image = image,
            )
        }
        if (tuple != null) {
            mainViewModel.insert(tuple)
        }
    }


}