package com.example.relationshipmanager.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.example.relationshipmanager.R
import com.example.relationshipmanager.MainActivity
import com.example.relationshipmanager.adapters.FavoritesAdapter
import com.example.relationshipmanager.databinding.DialogNotesBinding
import com.example.relationshipmanager.databinding.DialogStatsBinding
import com.example.relationshipmanager.databinding.FragmentFavoritesBinding
import com.example.relationshipmanager.listeners.OnItemClickListener
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.model.EventResult
import com.example.relationshipmanager.utilities.StatsGenerator
import com.example.relationshipmanager.utilities.applyLoopingAnimatedVectorDrawable
import com.example.relationshipmanager.viewmodels.MainViewModel
import java.time.LocalDate
import kotlin.math.min


@ExperimentalStdlibApi
class FavoritesFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: FavoritesAdapter
    private lateinit var fullStats: SpannableStringBuilder
    private lateinit var act: MainActivity
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private var _dialogStatsBinding: DialogStatsBinding? = null
    private val dialogStatsBinding get() = _dialogStatsBinding!!
    private var _dialogNotesBinding: DialogNotesBinding? = null
    private val dialogNotesBinding get() = _dialogNotesBinding!!
    private var totalEvents = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = FavoritesAdapter()
        act = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val v = binding.root
        val statsImage = binding.statsImage
        val shimmer = binding.favoritesCardShimmer
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val shimmerEnabled = sharedPrefs.getBoolean("shimmer", false)
        val favoriteMotionLayout = binding.favoritesMain
        val favoritesCard = binding.favoritesCard
        val favoritesMiniFab = binding.favoritesMiniFab
        val overviewButton = binding.overviewButton
        if (shimmerEnabled) shimmer.startShimmer()
        statsImage.applyLoopingAnimatedVectorDrawable(R.drawable.animated_candle)

        // Set motion layout state, since it's saved
        favoriteMotionLayout.progress = sharedPrefs.getFloat("favorite_motion_state", 0.0F)

        // Vibration on the mini fab (with manual managing of the transition)
        favoritesMiniFab.setOnClickListener {
            ////act.vibrate()
            when (favoriteMotionLayout.progress) {
                0.0F -> {
                    favoriteMotionLayout.transitionToEnd()
                    sharedPrefs.edit().putFloat("favorite_motion_state", 1.0F).apply()
                }
                1.0F -> {
                    favoriteMotionLayout.transitionToStart()
                    sharedPrefs.edit().putFloat("favorite_motion_state", 0.0F).apply()
                }
            }
        }

        // Show full stats on long press too
        favoritesMiniFab.setOnLongClickListener {
            if (favoriteMotionLayout.progress == 1.0F) showStatsSheet()
            true
        }

        // Show full stats in a bottom sheet
        favoritesCard.setOnClickListener {
            showStatsSheet()
        }
        rootView = v

        // Setup the recycler view
        initializeRecyclerView()
        setUpAdapter()
        mainViewModel = ViewModelProvider(act)[MainViewModel::class.java]
        with(mainViewModel) {
            getFavorites().observe(viewLifecycleOwner, { events ->
                // Update the cached copy in the adapter
                if (events != null && events.isNotEmpty()) {
                    removePlaceholder()
                    adapter.submitList(events)
                }
            })
            // AllEvents contains everything, since the query string is reset when the fragment changes
            allEvents.observe(viewLifecycleOwner, { eventList ->
                // Under a minimum size, no stats will be shown (at least 5 events containing a year)
                if (eventList.filter { it.yearMatter == true }.size > 4) generateStat(eventList)
                else fullStats = SpannableStringBuilder(
                    requireActivity().applicationContext.getString(
                        R.string.no_stats_description
                    )
                )
                totalEvents = eventList.size
            })
        }

        // Set the overview button
        overviewButton.setOnClickListener {
            // Vibrate and navigate to the overview screen
            ////act.vibrate()
            requireView().findNavController()
                .navigate(R.id.action_navigationFavorites_to_overviewFragment)
        }

        // Set the overview dots with the next events
        with(binding) {
            mainViewModel.allEventsUnfiltered.observe(viewLifecycleOwner, { events ->
                // Grey for no events, .3 for 1 event, .6 for 2 events, 1 for 3+ events
                if (events != null) {
                    val today = LocalDate.now()
                    /*
                    val accent = act.getThemeColor(R.attr.colorAccent)
                    if (events.any { eventResult ->
                            eventResult.nextDate!!.isBefore(
                                LocalDate.now().plusDays(10)
                            )
                        }) {
                        overviewDot1.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText1.text = today.dayOfMonth.toString()
                        overviewDot1.alpha = .1F
                        overviewDot2.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText2.text = today.dayOfMonth.toString()
                        overviewDot2.alpha = .2F
                        overviewDot3.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText3.text = today.dayOfMonth.toString()
                        overviewDot3.alpha = .3F
                        overviewDot4.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText4.text = today.dayOfMonth.toString()
                        overviewDot4.alpha = .4F
                        overviewDot5.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText5.text = today.dayOfMonth.toString()
                        overviewDot5.alpha = .5F
                        overviewDot6.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText6.text = today.dayOfMonth.toString()
                        overviewDot6.alpha = .6F
                        overviewDot7.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText7.text = today.dayOfMonth.toString()
                        overviewDot7.alpha = .7F
                        overviewDot8.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText8.text = today.dayOfMonth.toString()
                        overviewDot8.alpha = .8F
                        overviewDot9.setColorFilter(accent, android.graphics.PorterDuff.Mode.SRC_IN)
                        overviewText9.text = today.dayOfMonth.toString()
                        overviewDot9.alpha = .9F
                        overviewDot10.setColorFilter(
                            accent,
                            android.graphics.PorterDuff.Mode.SRC_IN
                        )
                        overviewText10.text = today.dayOfMonth.toString()
                        overviewDot10.alpha = 1F
                    }

                     */
                }
            })

        }

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset each binding to null to follow the best practice
        _binding = null
        _dialogStatsBinding = null
        _dialogNotesBinding = null
    }

    // Initialize the necessary parts of the recycler view
    private fun initializeRecyclerView() {
        recyclerView = binding.favoritesRecycler
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }

    // Manage the onclick actions, or the long click (unused atm)
    private fun setUpAdapter() {
        adapter.setOnItemClickListener(onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View?) {
                ////act.vibrate()
                _dialogNotesBinding = DialogNotesBinding.inflate(LayoutInflater.from(context))
                val event = adapter.getItem(position)
                val title = getString(R.string.notes) + " - " + event.name
                MaterialDialog(act).show {
                    title(text = title)
                    icon(R.drawable.ic_note_24dp)
                    cornerRadius(res = R.dimen.rounded_corners)
                    customView(view = dialogNotesBinding.root)
                    val noteTextField = dialogNotesBinding.favoritesNotes
                    noteTextField.setText(event.notes)
                    negativeButton(R.string.cancel) {
                        dismiss()
                    }
                    positiveButton {
                        val note = noteTextField.text.toString().trim()
                        val tuple = Event(
                            id = event.id,
                            originalDate = event.originalDate,
                            name = event.name,
                            yearMatter = event.yearMatter,
                            surname = event.surname,
                            favorite = event.favorite,
                            notes = note,
                            image = event.image
                        )
                        mainViewModel.update(tuple)
                        dismiss()
                    }
                }
            }

            // TODO reassign an action to the long press
            override fun onItemLongClick(position: Int, view: View?): Boolean {
                return true
            }

        })
    }

    // Remove the placeholder or return if the placeholder was already removed before
    private fun removePlaceholder() {
        val placeholder = binding.noFavorites
        placeholder.visibility = View.GONE
    }

    // Show a bottom sheet containing the stats
    private fun showStatsSheet() {
        ////act.vibrate()
        _dialogStatsBinding = DialogStatsBinding.inflate(LayoutInflater.from(context))
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            cornerRadius(res = R.dimen.rounded_corners)
            title(R.string.stats_summary)
            icon(R.drawable.ic_stats_24dp)
            // Don't use scrollable here, instead use a nestedScrollView in the layout
            customView(view = dialogStatsBinding.root)
        }
        dialogStatsBinding.fullStats.text = fullStats
        // Prepare the toast
        var toast: Toast? = null
        // Display the total number of birthdays, start the animated drawable
        dialogStatsBinding.eventCounter.text = totalEvents.toString()
        val backgroundDrawable = dialogStatsBinding.eventCounterBackground
        // Link the opacity of the background to the number of events (min = 0.05 / max = 100)
        backgroundDrawable.alpha = min(0.01F * totalEvents + 0.05F, 1.0F)
        backgroundDrawable.applyLoopingAnimatedVectorDrawable(R.drawable.animated_counter_background)
        // Show an explanation for the counter, even if it's quite obvious
        backgroundDrawable.setOnClickListener {
            ////act.vibrate()
            toast?.cancel()
            @SuppressLint("ShowToast") // The toast is shown, stupid lint
            toast = Toast.makeText(
                context, resources.getQuantityString(
                    R.plurals.stats_total,
                    totalEvents,
                    totalEvents
                ), Toast.LENGTH_LONG
            )
            toast!!.show()
        }
    }

    // Use the generator to generate a random stat and display it
    private fun generateStat(events: List<EventResult>) {
        val cardSubtitle: TextView = binding.statsSubtitle
        val cardDescription: TextView = binding.statsDescription
        val generator = StatsGenerator(events, context)
        cardSubtitle.text = generator.generateRandomStat()
        fullStats = generator.generateFullStats()
        val summary = resources.getQuantityString(R.plurals.stats_total, events.size, events.size)
        cardDescription.text = summary
    }
}
