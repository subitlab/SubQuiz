package cn.org.subit.database

import cn.org.subit.dataClass.SectionType
import cn.org.subit.dataClass.SectionTypeId
import cn.org.subit.dataClass.SubjectId
import cn.org.subit.database.utils.singleOrNull
import kotlinx.serialization.Serializer
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*

class SectionTypes: SqlDao<SectionTypes.SectionTypeTable>(SectionTypeTable)
{
    object SectionTypeTable: IdTable<SectionTypeId>("sectionTypes")
    {
        override val id = sectionTypeId("id").autoIncrement().entityId()
        val subject = reference("subject", Subjects.SubjectTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
        val name = text("name").uniqueIndex()
        val description = text("description")
        override val primaryKey = PrimaryKey(id)
    }

    private fun deserialize(row: ResultRow) = SectionTypeTable.run()
    {
        SectionType(
            row[id].value,
            row[subject].value,
            row[name],
            row[description],
        )
    }

    suspend fun getSectionType(id: SectionTypeId) = query()
    {
        selectAll()
            .andWhere { table.id eq id }
            .singleOrNull()
            ?.let(::deserialize)
    }

    suspend fun newSectionType(subject: SubjectId, name: String, description: String) = query()
    {
        insertIgnoreAndGetId()
        {
            it[table.subject] = subject
            it[table.name] = name
            it[table.description] = description
        }?.value
    }
}