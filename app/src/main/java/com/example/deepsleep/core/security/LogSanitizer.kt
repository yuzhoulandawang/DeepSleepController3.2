package com.example.deepsleep.core.security

/**
 * 日志脱敏处理器
 * 用于移除日志中的敏感信息
 */
object LogSanitizer {
    
    /**
     * 脱敏手机号
     */
    private val phoneRegex = Regex("\\d{11}")
    
    /**
     * 脱敏邮箱
     */
    private val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    
    /**
     * 脱敏身份证号
     */
    private val idCardRegex = Regex("\\d{15}|\\d{17}[\\dXx]")
    
    /**
     * 脱敏银行卡号
     */
    private val bankCardRegex = Regex("\\d{16,19}")
    
    /**
     * 脱敏 IP 地址
     */
    private val ipRegex = Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
    
    /**
     * 脱敏 Token/密钥
     */
    private val tokenRegex = Regex("[A-Za-z0-9]{32,}")
    
    /**
     * 脱敏日志消息
     */
    fun sanitize(message: String): String {
        var result = message
        
        // 脱敏手机号 (保留前3位和后4位)
        result = result.replace(phoneRegex) { match ->
            val phone = match.value
            "${phone.take(3)}****${phone.takeLast(4)}"
        }
        
        // 脱敏邮箱 (只显示首字母和域名)
        result = result.replace(emailRegex) { match ->
            val email = match.value
            val atIndex = email.indexOf('@')
            if (atIndex > 0) {
                val name = email.substring(0, atIndex)
                val domain = email.substring(atIndex)
                "${name.first()}***${name.last()}$domain"
            } else {
                "***@***.com"
            }
        }
        
        // 脱敏身份证号 (保留前6位和后4位)
        result = result.replace(idCardRegex) { match ->
            val id = match.value
            "${id.take(6)}********${id.takeLast(4)}"
        }
        
        // 脱敏银行卡号 (保留前4位和后4位)
        result = result.replace(bankCardRegex) { match ->
            val card = match.value
            "${card.take(4)}************${card.takeLast(4)}"
        }
        
        // 脱敏 IP 地址 (只显示前2段)
        result = result.replace(ipRegex) { match ->
            val ip = match.value
            val parts = ip.split(".")
            "${parts[0]}.${parts[1]}.*.*"
        }
        
        // 脱敏 Token/密钥 (只显示前8位和后8位)
        result = result.replace(tokenRegex) { match ->
            val token = match.value
            if (token.length > 16) {
                "${token.take(8)}...${token.takeLast(8)}"
            } else {
                "***"
            }
        }
        
        return result
    }
    
    /**
     * 批量脱敏日志列表
     */
    fun sanitizeList(messages: List<String>): List<String> {
        return messages.map { sanitize(it) }
    }
    
    /**
     * 添加敏感词过滤
     */
    private val sensitiveWords = listOf(
        "password", "passwd", "secret", "token", "key", 
        "credential", "auth", "login", "passwd"
    )
    
    /**
     * 检查消息是否包含敏感词
     */
    fun containsSensitiveWords(message: String): Boolean {
        val lowerMessage = message.lowercase()
        return sensitiveWords.any { it in lowerMessage }
    }
    
    /**
     * 移除敏感词
     */
    fun removeSensitiveWords(message: String): String {
        var result = message
        sensitiveWords.forEach { word ->
            result = result.replace(Regex(word, RegexOption.IGNORE_CASE), "***")
        }
        return result
    }
}
