package cn.org.subit.route

import cn.org.subit.dataClass.Permission
import cn.org.subit.route.adimin.admin
import cn.org.subit.route.oauth.oauth
import cn.org.subit.route.quiz.quiz
import cn.org.subit.route.subject.subject
import cn.org.subit.route.terminal.terminal
import cn.org.subit.route.utils.Context
import cn.org.subit.route.utils.finishCall
import cn.org.subit.route.utils.getLoginUser
import cn.org.subit.utils.HttpStatus
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.router() = routing()
{
    val rootPath = this.application.rootPath

    get("/", { hidden = true })
    {
        call.respondRedirect("$rootPath/api-docs")
    }

    authenticate("auth-api-docs")
    {
        route("/api-docs")
        {
            route("/api.json")
            {
                openApiSpec()
            }
            swaggerUI("$rootPath/api-docs/api.json")
        }
    }

    oauth()

    authenticate("auth", optional = true)
    {
        install(createRouteScopedPlugin("ProhibitPlugin", { })
        {
            onCall {
                val permission = it.getLoginUser()?.permission ?: return@onCall
                if (permission < Permission.NORMAL) finishCall(HttpStatus.Prohibit)
            }
        })

        admin()
        quiz()
        subject()
        terminal()
    }
}