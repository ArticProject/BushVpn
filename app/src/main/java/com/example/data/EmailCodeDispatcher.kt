package com.example.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

data class SmtpConfig(
    val host: String = "smtp.gmail.com",
    val port: Int = 587,
    val senderEmail: String = "",
    val appPassword: String = ""
) {
    val isConfigured: Boolean
        get() = senderEmail.isNotBlank() && appPassword.isNotBlank() && host.isNotBlank()
}

data class EmailDispatchResult(
    val delivered: Boolean,
    val needsFormActivation: Boolean,
    val viaSmtp: Boolean,
    val statusMessageRu: String,
    val statusMessageEn: String
)

/**
 * Real Email Dispatcher for 6-digit verification codes:
 * 1. Does NOT generate any local Android phone notifications pretending to be an email.
 * 2. Sends real emails via RFC-5321 STARTTLS / SSL SMTP (e.g., smtp.gmail.com:587, smtp.mail.ru:587,
 *    smtp.yandex.ru:587) when SMTP credentials are configured.
 * 3. Sends real emails via FormSubmit Cloud Relay (https://formsubmit.co/ajax/<email>) with proper
 *    Origin and Referer headers when SMTP is not yet configured.
 */
object EmailCodeDispatcher {

    fun generateSixDigitCode(): String {
        val num = Random.nextInt(100000, 999999)
        return num.toString()
    }

    suspend fun dispatchVerificationEmail(
        recipientEmail: String,
        code: String,
        isEnglish: Boolean,
        smtpConfig: SmtpConfig = SmtpConfig()
    ): EmailDispatchResult = withContext(Dispatchers.IO) {
        val cleanRecipient = recipientEmail.trim()
        if (!cleanRecipient.contains("@") || !cleanRecipient.contains(".")) {
            return@withContext EmailDispatchResult(
                delivered = false,
                needsFormActivation = false,
                viaSmtp = false,
                statusMessageRu = "Укажите корректный адрес электронной почты",
                statusMessageEn = "Please enter a valid email address"
            )
        }

        // 1. If direct SMTP (Gmail / Mail.ru / Yandex) is configured, send directly via STARTTLS/SSL SMTP
        if (smtpConfig.isConfigured) {
            val smtpResult = runCatching {
                sendViaSmtp(
                    smtpConfig = smtpConfig,
                    recipientEmail = cleanRecipient,
                    code = code,
                    isEnglish = isEnglish
                )
            }
            if (smtpResult.isSuccess && smtpResult.getOrDefault(false)) {
                return@withContext EmailDispatchResult(
                    delivered = true,
                    needsFormActivation = false,
                    viaSmtp = true,
                    statusMessageRu = "Письмо с кодом отправлено через SMTP на $cleanRecipient",
                    statusMessageEn = "Verification email sent via SMTP to $cleanRecipient"
                )
            }
        }

        // 2. Send via FormSubmit Cloud Relay with required Origin & Referer headers
        val relayResult = runCatching {
            sendViaFormSubmit(
                recipientEmail = cleanRecipient,
                code = code,
                isEnglish = isEnglish
            )
        }.getOrElse { err ->
            EmailDispatchResult(
                delivered = false,
                needsFormActivation = false,
                viaSmtp = false,
                statusMessageRu = "Ошибка сети при отправке письма: ${err.localizedMessage ?: "проверьте интернет"}",
                statusMessageEn = "Network error sending email: ${err.localizedMessage ?: "check internet"}"
            )
        }

        relayResult
    }

    private fun sendViaFormSubmit(
        recipientEmail: String,
        code: String,
        isEnglish: Boolean
    ): EmailDispatchResult {
        val endpoint = URL("https://formsubmit.co/ajax/$recipientEmail")
        val conn = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 8000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Origin", "https://bushvpn.app")
            setRequestProperty("Referer", "https://bushvpn.app/")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            )
        }

        val body = JSONObject().apply {
            put(
                "_subject",
                if (isEnglish) "BushVPN Verification Code: $code" else "BushVPN • Код подтверждения: $code"
            )
            put("_template", "box")
            put("_captcha", "false")
            put("service", "BushVPN Authentication")
            put("verification_code", code)
            put(
                "message",
                if (isEnglish) {
                    "Your BushVPN 6-digit verification code is: $code"
                } else {
                    "Ваш 6-значный код подтверждения для входа в BushVPN: $code"
                }
            )
        }.toString()

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
            writer.flush()
        }

        val status = conn.responseCode
        val responseStream = if (status in 200..299) conn.inputStream else conn.errorStream
        val responseText = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        conn.disconnect()

        val json = runCatching { JSONObject(responseText) }.getOrNull()
        val successStr = json?.optString("success", "false")?.lowercase(Locale.US) ?: "false"
        val messageStr = json?.optString("message", "").orEmpty()

        return when {
            successStr == "true" -> EmailDispatchResult(
                delivered = true,
                needsFormActivation = false,
                viaSmtp = false,
                statusMessageRu = "Письмо с 6-значным кодом отправлено на $recipientEmail (проверьте Входящие и Спам)",
                statusMessageEn = "6-digit code sent to $recipientEmail (check Inbox & Spam)"
            )
            messageStr.contains("Activation", ignoreCase = true) ||
                messageStr.contains("Activate", ignoreCase = true) -> EmailDispatchResult(
                delivered = false,
                needsFormActivation = true,
                viaSmtp = false,
                statusMessageRu = "На $recipientEmail отправлено письмо активации от FormSubmit. Откройте почту (или Спам), нажмите «Activate Form» и нажмите «Отправить повторно», либо настройте прямую отправку через SMTP ниже.",
                statusMessageEn = "FormSubmit sent a one-time activation email to $recipientEmail. Click 'Activate Form' in your inbox (or Spam) and tap Resend, or configure direct SMTP below."
            )
            else -> EmailDispatchResult(
                delivered = false,
                needsFormActivation = false,
                viaSmtp = false,
                statusMessageRu = "Почтовый шлюз вернул ответ: ${messageStr.ifBlank { "настройте прямую SMTP-отправку ниже" }}",
                statusMessageEn = "Mail relay response: ${messageStr.ifBlank { "configure direct SMTP below" }}"
            )
        }
    }

    /**
     * Real RFC-5321 SMTP Client supporting STARTTLS (port 587) and implicit SSL/TLS (port 465).
     * Works directly with smtp.gmail.com, smtp.mail.ru, smtp.yandex.ru, etc.
     */
    private fun sendViaSmtp(
        smtpConfig: SmtpConfig,
        recipientEmail: String,
        code: String,
        isEnglish: Boolean
    ): Boolean {
        val host = smtpConfig.host.trim()
        val port = smtpConfig.port
        val sender = smtpConfig.senderEmail.trim()
        val cleanPassword = smtpConfig.appPassword.replace(" ", "").trim()

        val rawSocket = Socket()
        rawSocket.connect(InetSocketAddress(host, port), 8000)
        rawSocket.soTimeout = 8000

        val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        var activeSocket: Socket = if (port == 465) {
            (sslFactory.createSocket(rawSocket, host, port, true) as SSLSocket).apply {
                startHandshake()
            }
        } else {
            rawSocket
        }

        try {
            var reader = BufferedReader(InputStreamReader(activeSocket.getInputStream(), Charsets.UTF_8))
            var writer = PrintWriter(OutputStreamWriter(activeSocket.getOutputStream(), Charsets.UTF_8), false)

            fun readResponse(): Int {
                var codeNum = -1
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.length >= 3) {
                        codeNum = line.substring(0, 3).toIntOrNull() ?: -1
                    }
                    if (line.length < 4 || line[3] != '-') break
                }
                return codeNum
            }

            fun sendCommand(cmd: String): Int {
                writer.print("$cmd\r\n")
                writer.flush()
                return readResponse()
            }

            // Read 220 greeting
            if (readResponse() !in 200..299) return false

            // EHLO
            if (sendCommand("EHLO bushvpn.client") !in 200..299) return false

            // Upgrade to TLS on port 587 via STARTTLS
            if (port != 465) {
                if (sendCommand("STARTTLS") !in 200..299) return false
                val sslSocket = sslFactory.createSocket(activeSocket, host, port, true) as SSLSocket
                sslSocket.startHandshake()
                activeSocket = sslSocket
                reader = BufferedReader(InputStreamReader(activeSocket.getInputStream(), Charsets.UTF_8))
                writer = PrintWriter(OutputStreamWriter(activeSocket.getOutputStream(), Charsets.UTF_8), false)
                if (sendCommand("EHLO bushvpn.client") !in 200..299) return false
            }

            // AUTH LOGIN
            if (sendCommand("AUTH LOGIN") != 334) return false
            val userB64 = Base64.encodeToString(sender.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            if (sendCommand(userB64) != 334) return false
            val passB64 = Base64.encodeToString(cleanPassword.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            if (sendCommand(passB64) != 235) return false

            if (sendCommand("MAIL FROM:<$sender>") !in 200..299) return false
            if (sendCommand("RCPT TO:<$recipientEmail>") !in 200..299) return false
            if (sendCommand("DATA") != 354) return false

            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
            val dateStr = dateFormat.format(Date())
            val subjectText = if (isEnglish) {
                "BushVPN Verification Code: $code"
            } else {
                "BushVPN - Код подтверждения: $code"
            }
            val subjectB64 = Base64.encodeToString(subjectText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            val htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="background-color:#0B0C0E;color:#F4F5F8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;padding:32px;text-align:center;">
                  <div style="max-width:420px;margin:0 auto;background-color:#13151A;border:1px solid #23262F;border-radius:20px;padding:28px;">
                    <h2 style="margin:0 0 8px 0;color:#F4F5F8;font-size:22px;">BushVPN</h2>
                    <p style="color:#9CA3AF;font-size:14px;margin:0 0 24px 0;">
                      ${if (isEnglish) "Your 6-digit account verification code:" else "Ваш 6-значный код подтверждения аккаунта:"}
                    </p>
                    <div style="background-color:#0B0C0E;border:1px solid #2E323E;border-radius:14px;padding:16px;font-size:30px;font-weight:bold;letter-spacing:8px;color:#F4F5F8;font-family:monospace;">
                      $code
                    </div>
                    <p style="color:#6B7280;font-size:12px;margin:20px 0 0 0;">
                      ${if (isEnglish) "Enter this code in the BushVPN app to continue." else "Введите этот код в приложении BushVPN для продолжения регистрации."}
                    </p>
                  </div>
                </body>
                </html>
            """.trimIndent()

            val msgBuilder = StringBuilder()
            msgBuilder.append("Date: $dateStr\r\n")
            msgBuilder.append("From: \"BushVPN\" <$sender>\r\n")
            msgBuilder.append("To: <$recipientEmail>\r\n")
            msgBuilder.append("Message-ID: <${UUID.randomUUID()}@bushvpn.app>\r\n")
            msgBuilder.append("Subject: =?UTF-8?B?$subjectB64?=\r\n")
            msgBuilder.append("MIME-Version: 1.0\r\n")
            msgBuilder.append("Content-Type: text/html; charset=UTF-8\r\n")
            msgBuilder.append("Content-Transfer-Encoding: 8bit\r\n")
            msgBuilder.append("\r\n")
            msgBuilder.append(htmlBody)
            msgBuilder.append("\r\n.")

            val dataResp = sendCommand(msgBuilder.toString())
            sendCommand("QUIT")
            return dataResp in 200..299
        } finally {
            runCatching { activeSocket.close() }
        }
    }
}
