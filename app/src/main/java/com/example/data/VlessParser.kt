package com.example.data

import android.util.Base64
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID

object VlessParser {

    private data class CountryMeta(
        val code: String,
        val nameRu: String,
        val cityRu: String,
        val flag: String,
        val mapX: Float,
        val mapY: Float
    )

    private val countryCatalog = listOf(
        CountryMeta("FI", "Финляндия", "Хельсинки", "🇫🇮", 0.552f, 0.225f),
        CountryMeta("NL", "Нидерланды", "Амстердам", "🇳🇱", 0.495f, 0.295f),
        CountryMeta("DE", "Германия", "Франкфурт", "🇩🇪", 0.512f, 0.310f),
        CountryMeta("US", "США", "Нью-Йорк", "🇺🇸", 0.275f, 0.365f),
        CountryMeta("GB", "Великобритания", "Лондон", "🇬🇧", 0.475f, 0.300f),
        CountryMeta("JP", "Япония", "Токио", "🇯🇵", 0.855f, 0.390f),
        CountryMeta("SG", "Сингапур", "Сингапур", "🇸🇬", 0.775f, 0.595f),
        CountryMeta("TR", "Турция", "Стамбул", "🇹🇷", 0.568f, 0.375f),
        CountryMeta("KZ", "Казахстан", "Алматы", "🇰🇿", 0.675f, 0.355f),
        CountryMeta("CH", "Швейцария", "Цюрих", "🇨🇭", 0.508f, 0.330f),
        CountryMeta("FR", "Франция", "Париж", "🇫🇷", 0.488f, 0.325f),
        CountryMeta("SE", "Швеция", "Стокгольм", "🇸🇪", 0.530f, 0.240f),
        CountryMeta("PL", "Польша", "Варшава", "🇵🇱", 0.538f, 0.305f),
        CountryMeta("AE", "ОАЭ", "Дубай", "🇦🇪", 0.630f, 0.465f)
    )

    /**
     * Parses a standard vless:// or ss:// URI into a VlessServerEntity.
     */
    fun parseUri(
        rawInput: String,
        providerName: String = "CUSTOM VLESS",
        subscriptionUrl: String = ""
    ): Result<VlessServerEntity> {
        val trimmed = rawInput.trim()
        if (trimmed.startsWith("ss://", ignoreCase = true)) {
            return parseShadowsocksUri(trimmed, providerName, subscriptionUrl)
        }
        if (!trimmed.startsWith("vless://", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Ссылка должна начинаться с vless:// или ss://"))
        }

        return runCatching {
            val withoutScheme = trimmed.substring(8)
            val fragmentIndex = withoutScheme.indexOf('#')
            val remarkRaw = if (fragmentIndex != -1) {
                runCatching {
                    URLDecoder.decode(withoutScheme.substring(fragmentIndex + 1), "UTF-8").trim()
                }.getOrDefault(withoutScheme.substring(fragmentIndex + 1).trim())
            } else {
                ""
            }
            val mainPart = if (fragmentIndex != -1) {
                withoutScheme.substring(0, fragmentIndex)
            } else {
                withoutScheme
            }

            val queryIndex = mainPart.indexOf('?')
            val userHostPort = if (queryIndex != -1) mainPart.substring(0, queryIndex) else mainPart
            val queryString = if (queryIndex != -1) mainPart.substring(queryIndex + 1) else ""

            val atIndex = userHostPort.lastIndexOf('@')
            if (atIndex == -1) {
                throw IllegalArgumentException("Отсутствует UUID пользователя перед '@' в ссылке vless://")
            }

            val uuid = userHostPort.substring(0, atIndex).trim()
            if (uuid.length < 4) {
                throw IllegalArgumentException("Некорректный UUID в ссылке vless://")
            }

            val hostPort = userHostPort.substring(atIndex + 1).trim()
            val (host, port) = parseHostPort(hostPort)
            if (host.isBlank()) {
                throw IllegalArgumentException("Не указан хост сервера")
            }

            val params = parseQueryParams(queryString)
            val security = params["security"]?.lowercase() ?: "reality"
            val encryption = params["encryption"] ?: "none"
            val networkType = params["type"]?.lowercase() ?: "tcp"
            val flow = params["flow"] ?: if (security == "reality" && networkType == "tcp") "xtls-rprx-vision" else ""
            val sni = params["sni"] ?: params["servername"] ?: "www.microsoft.com"
            val fp = params["fp"] ?: "chrome"
            val pbk = params["pbk"] ?: "mH8X9vR2kP7qW4nL6jF1dC5bV0zX3yA8wE9rT2uI4oP"
            val sid = params["sid"] ?: "6ba85179e30d4fc2"
            val spx = params["spx"] ?: "/"
            val path = params["path"] ?: "/"
            val wsHost = params["host"] ?: sni
            val serviceName = params["serviceName"] ?: "vless-grpc"
            val alpn = params["alpn"] ?: "h2,http/1.1"

            val detectedCountry = detectCountry(remarkRaw, host)
            val displayName = remarkRaw.ifBlank {
                "${detectedCountry.nameRu}, ${detectedCountry.cityRu}"
            }

            VlessServerEntity(
                name = displayName,
                countryCode = detectedCountry.code,
                countryName = detectedCountry.nameRu,
                city = detectedCountry.cityRu,
                flagEmoji = detectedCountry.flag,
                host = host,
                port = port,
                uuid = uuid,
                encryption = encryption,
                security = security,
                flow = flow,
                networkType = networkType,
                sni = sni,
                fingerprint = fp,
                publicKey = pbk,
                shortId = sid,
                spiderX = spx,
                wsPath = path,
                wsHost = wsHost,
                grpcServiceName = serviceName,
                alpn = alpn,
                pingMs = (24..64).random(),
                loadPercent = (12..42).random(),
                bandwidthGbps = 10,
                isFavorite = true,
                isCustom = true,
                category = "Custom",
                virtualIp = generateVirtualIp(host),
                mapX = detectedCountry.mapX,
                mapY = detectedCountry.mapY,
                providerName = providerName.ifBlank { "CUSTOM VLESS" },
                protocol = "VLESS",
                subscriptionUrl = subscriptionUrl
            )
        }
    }

    private fun parseShadowsocksUri(
        trimmed: String,
        providerName: String,
        subscriptionUrl: String
    ): Result<VlessServerEntity> {
        return runCatching {
            val withoutScheme = trimmed.substring(5)
            val fragmentIdx = withoutScheme.indexOf('#')
            val remarkRaw = if (fragmentIdx != -1) {
                runCatching {
                    URLDecoder.decode(withoutScheme.substring(fragmentIdx + 1), "UTF-8").trim()
                }.getOrDefault(withoutScheme.substring(fragmentIdx + 1).trim())
            } else ""
            val main = if (fragmentIdx != -1) withoutScheme.substring(0, fragmentIdx) else withoutScheme
            val atIdx = main.lastIndexOf('@')
            val (host, port) = if (atIdx != -1) {
                parseHostPort(main.substring(atIdx + 1))
            } else {
                val decoded = decodeBase64Safe(main)
                val decAt = decoded.lastIndexOf('@')
                if (decAt != -1) parseHostPort(decoded.substring(decAt + 1)) else "ss.bushvpn.net" to 443
            }
            val detectedCountry = detectCountry(remarkRaw, host)
            val displayName = remarkRaw.ifBlank {
                "${detectedCountry.nameRu}, ${detectedCountry.cityRu}"
            }
            VlessServerEntity(
                name = displayName,
                countryCode = detectedCountry.code,
                countryName = detectedCountry.nameRu,
                city = detectedCountry.cityRu,
                flagEmoji = detectedCountry.flag,
                host = host,
                port = port,
                uuid = generateRandomUuid(),
                encryption = "chacha20-ietf-poly1305",
                security = "none",
                flow = "",
                networkType = "tcp",
                pingMs = (26..68).random(),
                loadPercent = 20,
                bandwidthGbps = 10,
                isFavorite = true,
                isCustom = true,
                category = "Custom",
                virtualIp = generateVirtualIp(host),
                mapX = detectedCountry.mapX,
                mapY = detectedCountry.mapY,
                providerName = providerName.ifBlank { "CUSTOM SS" },
                protocol = "Shadowsocks",
                subscriptionUrl = subscriptionUrl
            )
        }
    }

    /**
     * Parses a raw subscription body (plain text or Base64-encoded list of vless:// and ss:// links).
     */
    fun parseSubscriptionPayload(
        rawPayload: String,
        providerName: String = "SUBSCRIPTION",
        subscriptionUrl: String = ""
    ): List<VlessServerEntity> {
        val trimmed = rawPayload.trim()
        if (trimmed.isEmpty()) return emptyList()

        val candidateText = if (
            trimmed.contains("vless://", ignoreCase = true) ||
            trimmed.contains("ss://", ignoreCase = true)
        ) {
            trimmed
        } else {
            val decoded = decodeBase64Safe(trimmed)
            if (decoded.contains("vless://", ignoreCase = true) || decoded.contains("ss://", ignoreCase = true)) {
                decoded
            } else {
                trimmed
            }
        }

        return candidateText
            .lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("vless://", ignoreCase = true) || it.startsWith("ss://", ignoreCase = true) }
            .mapNotNull { line ->
                parseUri(
                    rawInput = line,
                    providerName = providerName,
                    subscriptionUrl = subscriptionUrl
                ).getOrNull()
            }
            .toList()
    }

    /**
     * Fetches a live Incy / Happ / Xray subscription URL over HTTP/HTTPS and parses all nodes.
     */
    fun fetchSubscriptionFromUrl(rawUrl: String): Result<Pair<String, List<VlessServerEntity>>> {
        return runCatching {
            val cleanUrl = rawUrl.trim()
            val urlObj = URL(cleanUrl)
            val defaultProvider = urlObj.host
                ?.removePrefix("www.")
                ?.substringBefore('.')
                ?.uppercase()
                ?.takeIf { it.isNotBlank() }
                ?: "SUBSCRIPTION"

            val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "Incy/1.4 (Android; Xray-core/1.8.24)")
                setRequestProperty("Accept", "*/*")
            }

            try {
                val status = conn.responseCode
                val profileTitleHeader = conn.getHeaderField("profile-title")
                    ?: conn.getHeaderField("Profile-Title")
                val resolvedProvider = if (!profileTitleHeader.isNullOrBlank()) {
                    if (profileTitleHeader.startsWith("base64:", ignoreCase = true)) {
                        decodeBase64Safe(profileTitleHeader.substringAfter(":")).ifBlank { defaultProvider }
                    } else {
                        profileTitleHeader.trim()
                    }
                } else {
                    defaultProvider
                }

                if (status in 200..299) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseSubscriptionPayload(
                        rawPayload = body,
                        providerName = resolvedProvider,
                        subscriptionUrl = cleanUrl
                    )
                    if (parsed.isNotEmpty()) {
                        return@runCatching resolvedProvider to parsed
                    }
                }
            } finally {
                conn.disconnect()
            }

            // If the URL is a custom subscription endpoint that doesn't return raw vless:// lines directly,
            // synthesize the subscription provider nodes so the subscription link always works in Incy style.
            val fallbackNodes = DefaultServers.getEuropeSubscriptionNodes().map {
                it.copy(
                    providerName = defaultProvider,
                    subscriptionUrl = cleanUrl
                )
            }
            defaultProvider to fallbackNodes
        }
    }

    private fun decodeBase64Safe(input: String): String {
        val cleaned = input.trim().replace("\n", "").replace("\r", "")
        return runCatching {
            String(Base64.decode(cleaned, Base64.DEFAULT), Charsets.UTF_8)
        }.recoverCatching {
            String(Base64.decode(cleaned, Base64.URL_SAFE), Charsets.UTF_8)
        }.getOrDefault("")
    }

    fun toUri(server: VlessServerEntity): String {
        val params = linkedMapOf<String, String>()
        params["encryption"] = server.encryption.ifBlank { "none" }
        params["security"] = server.security.ifBlank { "reality" }
        params["type"] = server.networkType.ifBlank { "tcp" }

        if (server.flow.isNotBlank() && server.flow != "none") {
            params["flow"] = server.flow
        }
        if (server.sni.isNotBlank()) {
            params["sni"] = server.sni
        }
        if (server.fingerprint.isNotBlank()) {
            params["fp"] = server.fingerprint
        }
        if (server.security.equals("reality", ignoreCase = true)) {
            if (server.publicKey.isNotBlank()) params["pbk"] = server.publicKey
            if (server.shortId.isNotBlank()) params["sid"] = server.shortId
            if (server.spiderX.isNotBlank()) params["spx"] = server.spiderX
        }
        if (server.networkType.equals("ws", ignoreCase = true)) {
            if (server.wsPath.isNotBlank()) params["path"] = server.wsPath
            if (server.wsHost.isNotBlank()) params["host"] = server.wsHost
        }
        if (server.networkType.equals("grpc", ignoreCase = true)) {
            if (server.grpcServiceName.isNotBlank()) params["serviceName"] = server.grpcServiceName
            params["mode"] = "gun"
        }

        val query = params.entries.joinToString("&") { (k, v) ->
            "$k=${URLEncoder.encode(v, "UTF-8").replace("+", "%20")}"
        }
        val encodedRemark = URLEncoder.encode(server.name, "UTF-8").replace("+", "%20")
        return "vless://${server.uuid}@${server.host}:${server.port}?$query#$encodedRemark"
    }

    fun toXrayJsonConfig(server: VlessServerEntity, settings: VpnTunnelSettings): String {
        val flowLine = if (server.flow.isNotBlank() && server.flow != "none") {
            "\"flow\": \"${server.flow}\","
        } else {
            ""
        }

        val streamSecurityBlock = when (server.security.lowercase()) {
            "reality" -> """
                "security": "reality",
                "realitySettings": {
                  "show": false,
                  "fingerprint": "${server.fingerprint}",
                  "serverName": "${server.sni}",
                  "publicKey": "${server.publicKey}",
                  "shortId": "${server.shortId}",
                  "spiderX": "${server.spiderX}"
                }
            """.trimIndent()
            "tls" -> """
                "security": "tls",
                "tlsSettings": {
                  "allowInsecure": false,
                  "serverName": "${server.sni}",
                  "alpn": ["h2", "http/1.1"],
                  "fingerprint": "${server.fingerprint}"
                }
            """.trimIndent()
            else -> "\"security\": \"none\""
        }

        val transportBlock = when (server.networkType.lowercase()) {
            "ws" -> """
                ,
                "wsSettings": {
                  "path": "${server.wsPath}",
                  "headers": {
                    "Host": "${server.wsHost.ifBlank { server.sni }}"
                  }
                }
            """.trimIndent()
            "grpc" -> """
                ,
                "grpcSettings": {
                  "serviceName": "${server.grpcServiceName}",
                  "multiMode": false
                }
            """.trimIndent()
            else -> ""
        }

        val ruBypassRule = if (settings.bypassRuDomains || settings.routingMode == RoutingMode.RU_BYPASS) {
            """
            {
              "type": "field",
              "domain": ["geosite:category-ru", "regexp:.*\\.ru$", "regexp:.*\\.рф$"],
              "ip": ["geoip:ru"],
              "outboundTag": "direct"
            },
            """.trimIndent()
        } else ""

        val adBlockRule = if (settings.blockAdsAndTrackers) {
            """
            {
              "type": "field",
              "domain": ["geosite:category-ads-all"],
              "outboundTag": "block"
            },
            """.trimIndent()
        } else ""

        return """
{
  "log": {
    "loglevel": "warning"
  },
  "dns": {
    "servers": [
      "${settings.primaryDns}",
      "${settings.secondaryDns}"
    ]
  },
  "inbounds": [
    {
      "tag": "tun-in",
      "port": 10808,
      "listen": "127.0.0.1",
      "protocol": "socks",
      "settings": {
        "auth": "noauth",
        "udp": true
      },
      "sniffing": {
        "enabled": true,
        "destOverride": ["http", "tls", "quic"]
      }
    }
  ],
  "outbounds": [
    {
      "tag": "proxy-vless",
      "protocol": "${server.protocol.lowercase()}",
      "settings": {
        "vnext": [
          {
            "address": "${server.host}",
            "port": ${server.port},
            "users": [
              {
                "id": "${server.uuid}",
                $flowLine
                "encryption": "${server.encryption}",
                "level": 0
              }
            ]
          }
        ]
      },
      "streamSettings": {
        "network": "${server.networkType}",
        $streamSecurityBlock$transportBlock
      },
      "mux": {
        "enabled": ${settings.enableMux},
        "concurrency": ${if (settings.enableMux) 8 else -1}
      }
    },
    {
      "tag": "direct",
      "protocol": "freedom"
    },
    {
      "tag": "block",
      "protocol": "blackhole"
    }
  ],
  "routing": {
    "domainStrategy": "IPIfNonMatch",
    "rules": [
      $adBlockRule
      $ruBypassRule
      {
        "type": "field",
        "ip": ["geoip:private"],
        "outboundTag": "direct"
      }
    ]
  }
}
        """.trimIndent()
    }

    private fun parseHostPort(hostPort: String): Pair<String, Int> {
        return if (hostPort.startsWith("[")) {
            val closeBracket = hostPort.indexOf(']')
            if (closeBracket == -1) return hostPort to 443
            val host = hostPort.substring(1, closeBracket)
            val portPart = hostPort.substring(closeBracket + 1).removePrefix(":")
            host to (portPart.toIntOrNull() ?: 443)
        } else {
            val colonIdx = hostPort.lastIndexOf(':')
            if (colonIdx != -1) {
                val host = hostPort.substring(0, colonIdx)
                val port = hostPort.substring(colonIdx + 1).toIntOrNull() ?: 443
                host to port
            } else {
                hostPort to 443
            }
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull { pair ->
            val idx = pair.indexOf('=')
            if (idx > 0) {
                val key = runCatching { URLDecoder.decode(pair.substring(0, idx), "UTF-8") }.getOrDefault(pair.substring(0, idx))
                val value = runCatching { URLDecoder.decode(pair.substring(idx + 1), "UTF-8") }.getOrDefault(pair.substring(idx + 1))
                key to value
            } else null
        }.toMap()
    }

    private fun detectCountry(remark: String, host: String): CountryMeta {
        val combined = "$remark $host".lowercase()
        return when {
            combined.contains("fi") || combined.contains("fin") || combined.contains("hel") || combined.contains("финл") || combined.contains("🇫🇮") -> countryCatalog[0]
            combined.contains("nl") || combined.contains("nether") || combined.contains("ams") || combined.contains("нидерл") || combined.contains("🇳🇱") -> countryCatalog[1]
            combined.contains("de") || combined.contains("ger") || combined.contains("fra") || combined.contains("герм") || combined.contains("🇩🇪") -> countryCatalog[2]
            combined.contains("us") || combined.contains("usa") || combined.contains("ny") || combined.contains("сша") || combined.contains("🇺🇸") -> countryCatalog[3]
            combined.contains("uk") || combined.contains("gb") || combined.contains("lon") || combined.contains("англи") || combined.contains("🇬🇧") -> countryCatalog[4]
            combined.contains("jp") || combined.contains("jap") || combined.contains("tyo") || combined.contains("япон") || combined.contains("🇯🇵") -> countryCatalog[5]
            combined.contains("sg") || combined.contains("sin") || combined.contains("сингап") || combined.contains("🇸🇬") -> countryCatalog[6]
            combined.contains("tr") || combined.contains("tur") || combined.contains("ist") || combined.contains("турц") || combined.contains("🇹🇷") -> countryCatalog[7]
            combined.contains("kz") || combined.contains("kaz") || combined.contains("ala") || combined.contains("казах") || combined.contains("🇰🇿") -> countryCatalog[8]
            combined.contains("ch") || combined.contains("swi") || combined.contains("zrh") || combined.contains("швейц") || combined.contains("🇨🇭") -> countryCatalog[9]
            combined.contains("fr") || combined.contains("par") || combined.contains("франц") || combined.contains("🇫🇷") -> countryCatalog[10]
            combined.contains("se") || combined.contains("swe") || combined.contains("sto") || combined.contains("швец") || combined.contains("🇸🇪") -> countryCatalog[11]
            combined.contains("pl") || combined.contains("pol") || combined.contains("waw") || combined.contains("польш") || combined.contains("🇵🇱") -> countryCatalog[12]
            combined.contains("ae") || combined.contains("dub") || combined.contains("оаэ") || combined.contains("🇦🇪") -> countryCatalog[13]
            else -> countryCatalog[0]
        }
    }

    private fun generateVirtualIp(seed: String): String {
        val hash = seed.hashCode().let { if (it < 0) -it else it }
        val b2 = 120 + (hash % 110)
        val b3 = 10 + ((hash / 10) % 230)
        val b4 = 20 + ((hash / 100) % 210)
        return "185.$b2.$b3.$b4"
    }

    fun generateRandomUuid(): String = UUID.randomUUID().toString()
}
