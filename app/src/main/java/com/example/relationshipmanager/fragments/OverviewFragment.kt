package com.example.relationshipmanager.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.relationshipmanager.MainActivity
import com.example.relationshipmanager.databinding.FragmentOverviewBinding
import com.example.relationshipmanager.viewmodels.MainViewModel


@ExperimentalStdlibApi
class OverviewFragment : Fragment() {
    private lateinit var act: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sharedPrefs: SharedPreferences
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = activity as MainActivity
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset the binding to null to follow the best practice
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        val v = binding.root
        mainViewModel = ViewModelProvider(act)[MainViewModel::class.java]
        return v
    }
}