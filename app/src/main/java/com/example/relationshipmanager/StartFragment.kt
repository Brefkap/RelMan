package com.example.relationshipmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.utilities.bitmapToByteArray
import com.example.relationshipmanager.utilities.smartCapitalize
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalStdlibApi
class StartFragment : Fragment() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun addReachOutReminder(nameValue: String?, surnameValue: String?, eventDateValue: LocalDate?, countYearValue: Boolean) {
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
                type = "reach-out",
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