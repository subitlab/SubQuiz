@file:Suppress("PackageDirectoryMismatch")

package cn.org.subit.plugin.authentication

import cn.org.subit.config.apiDocsConfig
import cn.org.subit.logger.SubQuizLogger
import cn.org.subit.route.utils.finishCall
import cn.org.subit.utils.HttpStatus
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import cn.org.subit.utils.SSO

/**
 * 安装登陆验证服务
 */
fun Application.installAuthentication() = install(Authentication)
{
    val logger = SubQuizLogger.getLogger()
    // 此登陆仅用于api文档的访问, 见ApiDocs插件
    basic("auth-api-docs")
    {
        realm = "Access to the Swagger UI"
        validate()
        {
            if (it.name == apiDocsConfig.name && it.password == apiDocsConfig.password)
                UserIdPrincipal(it.name)
            else null
        }
    }

    bearer("auth")
    {
        authHeader()
        {
            val token = it.request.header(HttpHeaders.Authorization) ?: run {
                val t = parseHeaderValue(it.request.header(HttpHeaders.SecWebSocketProtocol))
                val index = t.indexOfFirst { headerValue -> headerValue.value == "Bearer" }
                if (index == -1) return@run null
                it.response.header(HttpHeaders.SecWebSocketProtocol, "Bearer")
                t.getOrNull(index + 1)?.value?.let { token -> "Bearer $token" }
            }
            logger.config("request with token: $token")
            runCatching { token?.let(::parseAuthorizationHeader) }.getOrNull()
        }
        authenticate()
        {
            val token = it.token
            val status = SSO.getStatus(token)
            if (status == null) finishCall(HttpStatus.Unauthorized)
            else if (status != SSO.AuthorizationStatus.AUTHORIZED) finishCall(HttpStatus.LoginSuccessButNotAuthorized)
            else SSO.getUserFull(token)
        }
    }
}