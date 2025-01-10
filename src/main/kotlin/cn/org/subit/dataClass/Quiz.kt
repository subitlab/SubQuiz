package cn.org.subit.dataClass

import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Quiz<out Answer: Int?, out UserAnswer: Int?, out Analysis: String?>(
    val id: QuizId,
    val user: UserId,
    val time: Long,
    val duration: Long?,
    val sections: List<Section<Answer, UserAnswer, Analysis>>,
    val finished: Boolean,
)
{
    fun hideAnswer() = Quiz(id, user, time, duration, sections.map { it.hideAnswer() }, finished)
    fun checkFinished(): Quiz<Answer, Int, Analysis>? =
        if (!finished) null else Quiz(id, user, time, duration, sections.map { it.checkFinished() ?: return null }, true)
    fun withoutUserAnswer() =
        Quiz(id, user, time, duration, sections.map { it.withoutUserAnswer() }, finished)
    companion object
    {
        val example = Quiz(
            QuizId(1),
            UserId(0),
            System.currentTimeMillis(),
            30.minutes.inWholeMilliseconds,
            listOf(Section.example),
            false
        )
    }
}