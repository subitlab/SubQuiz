package cn.org.subit.database

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

class Preferences: SqlDao<Preferences.PreferenceTable>(PreferenceTable)
{
    object PreferenceTable: IdTable<Long>("preferences")
    {
        override val id = long("id").autoIncrement().entityId()
        val user = reference("user", Users.UsersTable)
        val type = reference("type", SectionTypes.SectionTypeTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
        val value = long("value")
        override val primaryKey = PrimaryKey(id)
    }
}