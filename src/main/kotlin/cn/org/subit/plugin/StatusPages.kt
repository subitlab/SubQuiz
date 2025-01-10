@file:Suppress("PackageDirectoryMismatch")

package cn.org.subit.plugin.statusPages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import cn.org.subit.logger.SubQuizLogger
import cn.org.subit.plugin.rateLimit.RateLimit
import cn.org.subit.route.utils.CallFinish
import cn.org.subit.utils.HttpStatus
import cn.org.subit.utils.respond
import kotlin.time.Duration.Companion.seconds

private fun ApplicationCall.hasResponseBody() =
    (this.response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0) > 0

/**
 * 对于不同的状态码返回不同的页面
 */
fun Application.installStatusPages() = install(StatusPages)
{
    val logger = SubQuizLogger.getLogger()

    exception<CallFinish> { call, finish ->
        logger.finest("CallFinish in ${call.request.path()}", finish)
        finish.block(call)
    }
    exception<BadRequestException> { call, _ -> call.respond(HttpStatus.BadRequest) }
    exception<Throwable>
    { call, throwable ->
        logger.warning("出现位置错误, 访问接口: ${call.request.path()}", throwable)
        call.respond(HttpStatus.InternalServerError)
    }

    status(HttpStatusCode.NotFound) { _ -> if (!call.hasResponseBody()) call.respond(HttpStatus.NotFound) }
    status(HttpStatusCode.Forbidden) { _ -> if (!call.hasResponseBody()) call.respond(HttpStatus.Forbidden) }
    status(HttpStatusCode.BadRequest) { _ -> if (!call.hasResponseBody()) call.respond(HttpStatus.BadRequest) }
    status(HttpStatusCode.UnsupportedMediaType) { _ -> if (!call.hasResponseBody()) call.respond(HttpStatus.UnsupportedMediaType) }

    /** 针对请求过于频繁的处理, 详见[RateLimit] */
    status(HttpStatusCode.TooManyRequests)
    { _ ->
        val time = call.response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()?.seconds
        val typeName = call.response.headers["X-RateLimit-Type"]
        val type = RateLimit.list.find { it.rawRateLimitName == typeName }
        logger.config("TooManyRequests with type: $type($typeName), retryAfter: $time")
        if (time == null)
            return@status call.respond(HttpStatus.TooManyRequests)
        if (type == null)
            return@status call.respond(HttpStatus.TooManyRequests.copy(message = "请求过于频繁, 请${time}后再试"))
        type.customResponse(call, time)
    }
}