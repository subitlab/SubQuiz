package cn.org.subit.config

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment

@Serializable
data class SystemConfig(
    @Comment("SSO服务后端url, 请不要以/结尾, 例如: https://ssubito.subit.org.cn/api 而不是 https://ssubito.subit.org.cn/api/")
    val ssoServer: String,
    @Comment("SSO服务ID")
    val ssoServerId: Int,
    @Comment("SSO服务秘钥")
    val ssoSecret: String,
    @Comment("重复推送阈值, 若某个section在某次作答的正确率高于该阈值则不再重新推送. 例如60表示正确率高于60%不会再推送")
    val score: Double,
)

var systemConfig: SystemConfig by config(
    "system.yml",
    SystemConfig("https://ssubito.subit.org.cn/api", 1, "secret", 60.0),
)