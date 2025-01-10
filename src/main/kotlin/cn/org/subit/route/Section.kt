@file:Suppress("PackageDirectoryMismatch")

package cn.org.subit.route.section

import cn.org.subit.dataClass.*
import cn.org.subit.database.Permissions
import cn.org.subit.database.SectionTypes
import cn.org.subit.database.Sections
import cn.org.subit.route.utils.*
import cn.org.subit.utils.HttpStatus
import cn.org.subit.utils.statuses
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.resources.post
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.core.component.get

fun Route.section() = route("/section", {
    tags("section")
})
{
    sectionType()

    post("/{type}", {
        summary = "创建题目"
        description = "创建一个Section"
        request()
        {
            body<SectionInfo<Int, Nothing?, String>>()
            {
                required = true
                description = "section信息"
                example("example", SectionInfo.example1)
            }
        }
        response()
        {
            statuses<SectionId>(HttpStatus.OK)
        }
    }, Context::newSection)
}

@Serializable
data class SectionInfo<out Answer: Int?, out UserAnswer: Int?, out Analysis: String?>(
    val subject: SubjectId,
    val type: SectionTypeId,
    val description: String,
    val questions: List<Question<Answer, UserAnswer, Analysis>>
)
{
    companion object
    {
        val example0 = SectionInfo(
            SubjectId(1),
            SectionTypeId(1),
            "this is a section",
            questions = listOf(Question.example)
        )
        val example1 = SectionInfo(
            SubjectId(1),
            SectionTypeId(1),
            "this is a section",
            questions = listOf(Question.example.withoutUserAnswer())
        )
    }
}

private fun Route.sectionType() = route("/type", {})
{
}

private suspend fun Context.newSection(section: SectionInfo<Int, Nothing?, String>): Nothing
{
    val loginUser = getLoginUser() ?: finishCall(HttpStatus.Unauthorized)
    if (loginUser.permission < Permission.ADMIN && get<Permissions>().getPermission(loginUser.id, section.subject) < Permission.ADMIN)
        finishCall(HttpStatus.Forbidden)
    val sections = get<Sections>()
    sections.newSection(section.subject, section.type, section.description, section.questions)
    finishCall(HttpStatus.OK)
}


private suspend fun Context.newSectionType(): Nothing
{
    TODO()
}