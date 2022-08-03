package com.example.relationshipmanager.model

import java.io.Serializable
import java.time.LocalDate

data class EventResult (
    val id: Int,
    val type: String? = "birthday",
    var name: String,
    var surname: String? = "",
    var favorite: Boolean? = false,
    val yearMatter: Boolean? = true,
    var originalDate: LocalDate,
    val nextDate: LocalDate? = null,
    val notes: String? = "",
    val image: ByteArray? = null,
): Serializable {

    // Check if an event is a birthday
    fun isBirthday(event: EventResult): Boolean =
        event.type == EventCode.BIRTHDAY.name

    // Check if an event is a anniversary
    fun isAnniversary(event: EventResult): Boolean =
        event.type == EventCode.ANNIVERSARY.name

    // Check if an event is a death anniversary
    fun isDeathAnniversary(event: EventResult): Boolean =
        event.type == EventCode.DEATH.name

    // Check if an event is a name day
    fun isNameDay(event: EventResult): Boolean =
        event.type == EventCode.NAME_DAY.name

    // Check if an event is a name day
    fun isReachOut(event: EventResult): Boolean =
        event.type == EventCode.REACHOUT.name

    // Check if an event is "other"
    fun isOther(event: EventResult): Boolean =
        event.type == EventCode.OTHER.name





    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventResult

        if (id != other.id) return false
        if (type != other.type) return false
        if (name != other.name) return false
        if (surname != other.surname) return false
        if (yearMatter != other.yearMatter) return false
        if (originalDate != other.originalDate) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (surname?.hashCode() ?: 0)
        result = 31 * result + (favorite?.hashCode() ?: 0)
        result = 31 * result + (yearMatter?.hashCode() ?: 0)
        result = 31 * result + originalDate.hashCode()
        result = 31 * result + (nextDate?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}