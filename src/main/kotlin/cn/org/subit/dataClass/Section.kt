package cn.org.subit.dataClass

import kotlinx.serialization.Serializable

@Serializable
data class Section<out Answer: Int?, out UserAnswer: Int?, out Analysis: String?>(
    val id: SectionId,
    val subject: SubjectId,
    val type: SectionTypeId,
    val description: String,
    val questions: List<Question<Answer, UserAnswer, Analysis>>
)
{
    fun hideAnswer() = Section(id, subject, type, description, questions.map { it.hideAnswer() })
    fun checkFinished(): Section<Answer, Int, Analysis>? =
        Section(id, subject, type, description, questions.map { it.checkFinished() ?: return null })
    fun withoutUserAnswer() =
        Section(id, subject, type, description, questions.map { it.withoutUserAnswer() })
    companion object
    {
        val example = Section(
            SectionId(1),
            SubjectId(1),
            SectionTypeId(1),
            "the section description",
            listOf(Question.example)
        )

        /**
         * 统计一个section的得分, 返回的pair的first是得分, second是总分
         */
        fun Section<Int, Int, *>.score(): Pair<Int, Int> =
            questions.fold(0)
            { acc, q ->
                if (q.correct) acc + 1
                else acc
            } to questions.size
    }
}