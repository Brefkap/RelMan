package com.example.relationshipmanager.utilities

import android.content.Context
import com.example.relationshipmanager.R
import com.example.relationshipmanager.model.Event
import com.example.relationshipmanager.model.EventCode
import com.example.relationshipmanager.model.EventResult
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*


// Transform an event result in a simple event
fun resultToEvent(eventResult: EventResult) = Event(
    id = eventResult.id,
    name = eventResult.name,
    surname = eventResult.surname,
    favorite = eventResult.favorite,
    originalDate = eventResult.originalDate,
    yearMatter = eventResult.yearMatter,
    notes = eventResult.notes,
    image = eventResult.image
)

// Properly format the next date for widget and next event card
fun nextDateFormatted(event: EventResult, formatter: DateTimeFormatter, context: Context): String {
    val daysRemaining = getRemainingDays(event.nextDate!!)
    return event.nextDate.format(formatter) + ". " + formatDaysRemaining(daysRemaining, context)
}

// Return the remaining days or a string
fun formatDaysRemaining(daysRemaining: Int, context: Context): String {
    return when (daysRemaining) {
        // The -1 case should never happen
        -1 -> context.getString(R.string.yesterday)
        0 -> context.getString(R.string.today)
        1 -> context.getString(R.string.tomorrow)
        else -> context.resources.getQuantityString(
            R.plurals.days_left,
            daysRemaining,
            daysRemaining
        )
    }
}

// Format the name considering the preference and the surname (which could be empty)
fun formatName(event: EventResult, surnameFirst: Boolean): String {
    return if (event.surname.isNullOrBlank()) event.name
    else {
        if (!surnameFirst) event.name + " " + event.surname
        else event.surname + " " + event.name
    }
}

// Get the reduced date for an event, i.e. the month and day date, unsupported natively
fun getReducedDate(date: LocalDate) =
    date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
            ", " + date.dayOfMonth.toString()

// Get the age also considering the possible corner cases
fun getAge(eventResult: EventResult): Int {
    var age = -2
    if (eventResult.yearMatter!!) age = eventResult.nextDate!!.year - eventResult.originalDate.year - 1
    return if (age == -1) 0 else age
}

// Get the months of the age. Useful for babies
fun getAgeMonths(date: LocalDate) = Period.between(date, LocalDate.now()).months

// Get the next age also considering the possible corner cases
fun getNextAge(eventResult: EventResult) = getAge(eventResult) + 1

// Get the decade of birth
fun getDecade(originalDate: LocalDate) = ((originalDate.year.toDouble() / 10).toInt() * 10).toString()

// Get the age range, in decades
fun getAgeRange(originalDate: LocalDate) = (((LocalDate.now().year - originalDate.year).toDouble() / 10).toInt() * 10).toString()

// Get the days remaining before an event from today
fun getRemainingDays(nextDate: LocalDate) = ChronoUnit.DAYS.between(LocalDate.now(), nextDate).toInt()

// Given a series of events, format them considering the yearMatters parameter and the number
fun formatEventList(
    events: List<EventResult>,
    surnameFirst: Boolean,
    context: Context,
    showSurnames: Boolean = true
): String {
    var formattedEventList = ""
    if (events.isEmpty()) formattedEventList = context.getString(R.string.no_next_event)
    else events.forEach {
        // Years. They're not used in the string if the year doesn't matter
        val years = it.nextDate?.year?.minus(it.originalDate.year)!!
        // Only the data of the first 3 events are displayed
        if (events.indexOf(it) in 0..10) {
            // If the event is not the first, add an extra comma
            if (events.indexOf(it) != 0)
                formattedEventList += ", "

            // Show the last name, if any, if there's only one event
            formattedEventList +=
                if (events.size == 1 && showSurnames)
                    formatName(it, surnameFirst)
                else it.name

            // Show event type if different from birthday
            if (it.type != EventCode.BIRTHDAY.name)
                formattedEventList += " (${
                    getStringForTypeCodename(
                        context,
                        it.type!!
                    )
                })"
            // If the year is considered, display it. Else only display the name
            if (it.yearMatter!!) formattedEventList += ", " +
                    context.resources.getQuantityString(
                        R.plurals.years,
                        years,
                        years
                    )
        }
        // If more than 3 events, just let the user know other events are in the list
        if (events.indexOf(it) == 3) ", ${context.getString(R.string.event_others)}"
    }
    return formattedEventList
}

// Given a string, returns the corresponding translated event type, if any
fun getStringForTypeCodename(context: Context, codename: String): String {
    return try {
        when (EventCode.valueOf(codename.uppercase())) {
            EventCode.BIRTHDAY -> context.getString(R.string.birthday)
            EventCode.ANNIVERSARY -> context.getString(R.string.anniversary)
            EventCode.DEATH -> context.getString(R.string.death_anniversary)
            EventCode.NAME_DAY -> context.getString(R.string.name_day)
            EventCode.REACHOUT -> context.getString(R.string.reach_out)
            EventCode.OTHER -> context.getString(R.string.other)
        }
    } catch (e: Exception) {
        context.getString(R.string.unknown)
    }
}