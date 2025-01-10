package cn.org.subit.database

import cn.org.subit.dataClass.Permission
import cn.org.subit.dataClass.SubjectId
import cn.org.subit.dataClass.UserId
import cn.org.subit.database.utils.singleOrNull
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.andWhere

class Permissions: SqlDao<Permissions.PermissionTable>(PermissionTable)
{
    object PermissionTable: IdTable<Long>("permissions")
    {
        override val id = long("id").autoIncrement().entityId()
        val user = reference("user", Users.UsersTable)
        val subject = reference("subject", Subjects.SubjectTable)
        val permission = enumeration<Permission>("permission").default(Permission.NORMAL)
        override val primaryKey = PrimaryKey(id)
    }

    suspend fun getPermission(user: UserId, subject: SubjectId) = query()
    {
        select(permission)
            .andWhere { table.user eq user }
            .andWhere { table.subject eq subject }
            .singleOrNull()
            ?.get(permission)
            ?: Permission.NORMAL
    }
}