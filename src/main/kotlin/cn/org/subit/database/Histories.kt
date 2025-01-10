package cn.org.subit.database

import cn.org.subit.dataClass.Section
import cn.org.subit.dataClass.Section.Companion.score
import cn.org.subit.dataClass.UserId
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.batchInsert

class Histories: SqlDao<Histories.HistoryTable>(HistoryTable)
{
    object HistoryTable: IdTable<Long>("histories")
    {
        override val id = long("id").autoIncrement().entityId()
        val user = reference("user", Users.UsersTable).index()
        val section = reference("section", Sections.SectionTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE).index()
        val score = double("score").index()
        override val primaryKey = PrimaryKey(id)
    }

    suspend fun addHistories(user: UserId, sections: List<Section<Int, Int, *>>): Unit = query()
    {
        batchInsert(sections)
        {
            this[HistoryTable.user] = user
            this[HistoryTable.section] = it.id
            this[HistoryTable.score] = it.score().let { s -> s.first * 100.0 / s.second }
        }
    }
}