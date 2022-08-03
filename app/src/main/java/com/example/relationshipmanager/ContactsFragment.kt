package com.example.relationshipmanager

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalStdlibApi
class ContactsFragment : Fragment(), View.OnClickListener {
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

        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        //load all contacts into a table
        fillTable(view)

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
         * @return A new instance of fragment ContactsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //Excel stuff

    private fun fillTable(view: View) {
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
        val table = view.findViewById<TableLayout>(R.id.table_contacts)
        val lp = RelativeLayout.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        ) //layout for rows of Android table
        for (i in 1..excelSheet!!.lastRowNum) { //row 0 is filled with header, end should be number of contacts
            val row = TableRow(activity) //create row in Android table
            row.layoutParams = lp //assign layout to row

            val excelRow = excelSheet?.let { getRow(it, i) } //copy row from Excel

            for (j in 0..3) {
                val textView = TextView(activity) //create Android TextView
                //config various properties of TextView, color is really important, border good for debugging, padding for reading, gravity necessary?, Layout for spacing
                textView.textSize = 14f
                textView.setTextColor(Color.BLACK)
                textView.background = border
                textView.setPadding(10,10,10,10)
                textView.gravity = Gravity.CENTER_HORIZONTAL;
                textView.layoutParams = TableRow.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 80, 4.0f) //height to 80 to fit to button
                when (j) {
                    0 -> {
                        val button = Button(activity)
                        button.textSize = 14f
                        button.setTextColor(Color.BLACK)
                        button.background = gd;
                        button.setPadding(10,10,10,10)
                        button.gravity = Gravity.CENTER_HORIZONTAL
                        button.layoutParams = TableRow.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 80, 1.0f) //height to 80 because automatic height is too high (WRAP Content)
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


    override fun onClick(buttonView: View?) {
        //Toast.makeText(activity, "heeeeheee", Toast.LENGTH_SHORT).show()
        var bundle = Bundle()
        bundle.putInt("rowNr", buttonView?.tag as Int)
        var fragment = EditPersonFragment()
        fragment.arguments = bundle
        parentFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

}