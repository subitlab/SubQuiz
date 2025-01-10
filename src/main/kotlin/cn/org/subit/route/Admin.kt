@file:Suppress("PackageDirectoryMismatch")

package cn.org.subit.route.adimin

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.server.routing.*

fun Route.admin() = route("/admin", {
    tags("admin")
})
{

}