package cn.org.subit.dataClass

import kotlinx.serialization.Serializable
import kotlin.let
import kotlin.text.toInt
import kotlin.text.toIntOrNull

@Suppress("unused")
@JvmInline
@Serializable
value class UserId(val value: Int): Comparable<UserId>
{
    override fun compareTo(other: UserId): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
    companion object
    {
        fun String.toUserId() = UserId(toInt())
        fun String.toUserIdOrNull() = toIntOrNull()?.let(::UserId)
        fun Number.toUserId() = UserId(toInt())
    }
}