package cn.org.subit.dataClass

import kotlinx.serialization.Serializable

@Suppress("unused")
@JvmInline
@Serializable
value class SectionId(val value: Int): Comparable<SectionId>
{
    override fun compareTo(other: SectionId): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    companion object
    {
        fun String.toSectionId() = SectionId(toInt())
        fun String.toSectionIdOrNull() = toIntOrNull()?.let(::SectionId)
        fun Number.toSectionId() = SectionId(toInt())
    }
}