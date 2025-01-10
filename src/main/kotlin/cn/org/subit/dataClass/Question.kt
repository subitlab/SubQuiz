package cn.org.subit.dataClass

import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class Question<out Answer: Int?, out UserAnswer: Int?, out Analysis: String?>(
    val description: String,
    val options: List<String>,
    val answer: Answer,
    val userAnswer: UserAnswer,
    val analysis: Analysis,
)
{
    val correct get() = userAnswer != null && userAnswer == answer
    fun hideAnswer() = Question(description, options, null, userAnswer, null)
    fun checkFinished(): Question<Answer, Int, Analysis>? = if (userAnswer == null) null else Question(description, options, answer, userAnswer, analysis)
    fun withoutUserAnswer() = Question(description, options, answer, null, analysis)
    companion object
    {
        val example = Question(
            "the question description",
            listOf("option 1", "option 2", "option 3", "option 4"),
            1,
            2,
            "the answer is 1"
        )
    }
}