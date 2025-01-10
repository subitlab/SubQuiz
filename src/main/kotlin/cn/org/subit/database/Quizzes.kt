package cn.org.subit.database

import cn.org.subit.dataClass.*
import cn.org.subit.dataClass.Slice
import cn.org.subit.database.utils.asSlice
import cn.org.subit.database.utils.single
import cn.org.subit.database.utils.singleOrNull
import cn.org.subit.plugin.contentNegotiation.dataJson
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class Quizzes: SqlDao<Quizzes.QuizTable>(QuizTable)
{
    companion object QuizTable: IdTable<QuizId>("quizzes")
    {
        override val id = quizId("id").autoIncrement().entityId()
        val user = reference("user", Users.UsersTable).index()
        val time = timestamp("time").defaultExpression(CurrentTimestamp).index()
        val duration = long("duration").nullable().default(null)
        val sections = jsonb<List<Section<Int, Int?, String>>>("sections", dataJson)
        val finished = bool("finished").default(false)
        override val primaryKey = PrimaryKey(id)
    }

    private fun deserialize(row: ResultRow): Quiz<Int, Int?, String> =
        Quiz(
            id = row[Quizzes.id].value,
            user = row[Quizzes.user].value,
            time = row[Quizzes.time].toEpochMilliseconds(),
            sections = row[Quizzes.sections],
            duration = row[Quizzes.duration],
            finished = row[Quizzes.finished],
        )

    suspend fun getUnfinishQuiz(user: UserId): Quiz<Int, Int?, String>? = query()
    {
        selectAll()
            .where { finished eq false }
            .singleOrNull()
            ?.let(::deserialize)
    }

    suspend fun getQuiz(id: QuizId): Quiz<Int, Int?, String>? = query()
    {
        selectAll()
            .where { table.id eq id }
            .singleOrNull()
            ?.let(::deserialize)
    }

    suspend fun getQuizzes(user: UserId, begin: Long, count: Int): Slice<Quiz<Int, Int?, String>> = query()
    {
        selectAll()
            .where { table.user eq user }
            .asSlice(begin, count)
            .map(::deserialize)
    }

    suspend fun addQuiz(user: UserId, sections: List<Section<Int, Nothing?, String>>): Quiz<Int, Nothing?, String> = query()
    {
        val time = Clock.System.now()
        val id = insertAndGetId {
            it[table.user] = user
            it[table.time] = time
            it[table.sections] = sections
        }.value
        Quiz(
            id,
            user,
            time.toEpochMilliseconds(),
            null,
            sections,
            false
        )
    }

    suspend fun updateQuiz(id: QuizId, finished: Boolean, duration: Long?, sections: List<Section<Int, Int?, String>>) = query()
    {
        update({ table.id eq id})
        {
            it[table.sections] = sections
            it[table.finished] = finished
            it[table.duration] = duration
            it[table.sections] = sections
        }
    }
}