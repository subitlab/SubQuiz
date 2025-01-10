package cn.org.subit.database

import cn.org.subit.config.systemConfig
import cn.org.subit.dataClass.*
import cn.org.subit.dataClass.Slice

import org.jetbrains.exposed.sql.SqlExpressionBuilder.coalesce
import org.jetbrains.exposed.sql.SqlExpressionBuilder.div
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import cn.org.subit.database.utils.asSlice
import cn.org.subit.database.utils.singleOrNull
import cn.org.subit.plugin.contentNegotiation.dataJson
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.jsonb
import org.koin.core.component.get

class Sections: SqlDao<Sections.SectionTable>(SectionTable)
{
    object SectionTable: IdTable<SectionId>("sections")
    {
        override val id = sectionId("id").autoIncrement().entityId()
        val subject = reference("subject", Subjects.SubjectTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
        val type = reference("type", SectionTypes.SectionTypeTable)
        val description = text("description")
        val questions = jsonb<List<Question<Int, Nothing?, String>>>("questions", dataJson)
        override val primaryKey = PrimaryKey(id)
    }

    private fun deserialize(row: ResultRow): Section<Int, Nothing?, String> = SectionTable.run()
    {
        Section(
            id = row[id].value,
            subject = row[subject].value,
            type = row[type].value,
            description = row[description],
            questions = row[questions]
        )
    }

    suspend fun getSection(id: SectionId): Section<Int, Nothing?, String>? = query()
    {
        selectAll().where { SectionTable.id eq id }.singleOrNull()?.let(::deserialize)
    }

    suspend fun recommendSections(user: UserId, subject: SubjectId?, count: Int): Slice<Section<Int, Nothing?, String>> = query()
    {
        val preferencesTable = get<Preferences>().table
        val historyTable = get<Histories>().table
        table
            .join(preferencesTable, JoinType.LEFT, table.type, preferencesTable.type)
            .join(historyTable, JoinType.LEFT, table.id, historyTable.section)
            .select(table.columns)
            .andWhere { preferencesTable.user eq user }
            .apply { subject?.let { andWhere { table.subject eq it } } }
            .andWhere { historyTable.user eq user }
            .andWhere { historyTable.score less systemConfig.score }
            .groupBy(*table.columns.toTypedArray())
            .groupBy(*preferencesTable.columns.toTypedArray())
            .andHaving { historyTable.id.count() eq 0 }
            .orderBy(
                coalesce(preferencesTable.value, longParam(1000)) * CustomFunction("RANDOM", LongColumnType()),
                SortOrder.DESC
            )
            .asSlice(0, count)
            .map(::deserialize)
    }

    suspend fun newSection(subject: SubjectId, type: SectionTypeId, description: String, questions: List<Question<Int, Nothing?, String>>) = query()
    {
        insertAndGetId()
        {
            it[table.subject] = subject
            it[table.type] = type
            it[table.description] = description
            it[table.questions] = questions
        }.value
    }
}