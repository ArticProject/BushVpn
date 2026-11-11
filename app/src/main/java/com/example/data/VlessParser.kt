package com.example.data

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
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
        CountryMeta("AE", "ОАЭ", "Дубай", "🇦🇪", 0.630f, 0.465f),
        CountryMeta("LV", "Латвия", "Рига", "🇱🇻", 0.550f, 0.265f),
        CountryMeta("EE", "Эстония", "Таллин", "🇪🇪", 0.552f, 0.248f),
        CountryMeta("LT", "Литва", "Вильнюс", "🇱🇹", 0.550f, 0.282f),
        CountryMeta("AT", "Австрия", "Вена", "🇦🇹", 0.528f, 0.330f),
        CountryMeta("CZ", "Чехия", "Прага", "🇨🇿", 0.524f, 0.318f),
        CountryMeta("ES", "Испания", "Мадрид", "🇪🇸", 0.468f, 0.365f),
        CountryMeta("IT", "Италия", "Милан", "🇮🇹", 0.515f, 0.350f),
        CountryMeta("NO", "Норвегия", "Осло", "🇳🇴", 0.515f, 0.235f),
        CountryMeta("DK", "Дания", "Копенгаген", "🇩🇰", 0.515f, 0.275f),
        CountryMeta("BE", "Бельгия", "Брюссель", "🇧🇪", 0.492f, 0.305f),
        CountryMeta("IE", "Ирландия", "Дублин", "🇮🇪", 0.462f, 0.298f),
        CountryMeta("PT", "Португалия", "Лиссабон", "🇵🇹", 0.455f, 0.370f),
        CountryMeta("BG", "Болгария", "София", "🇧🇬", 0.552f, 0.358f),
        CountryMeta("RO", "Румыния", "Бухарест", "🇷🇴", 0.555f, 0.345f),
        CountryMeta("HU", "Венгрия", "Будапешт", "🇭🇺", 0.535f, 0.335f),
        CountryMeta("RS", "Сербия", "Белград", "🇷🇸", 0.540f, 0.350f),
        CountryMeta("MD", "Молдова", "Кишинев", "🇲🇩", 0.562f, 0.338f),
        CountryMeta("UA", "Украина", "Киев", "🇺🇦", 0.568f, 0.318f),
        CountryMeta("AM", "Армения", "Ереван", "🇦🇲", 0.605f, 0.372f),
        CountryMeta("GE", "Грузия", "Тбилиси", "🇬🇪", 0.602f, 0.362f),
        CountryMeta("IL", "Израиль", "Тель-Авив", "🇮🇱", 0.582f, 0.415f),
        CountryMeta("IN", "Индия", "Мумбаи", "🇮🇳", 0.710f, 0.490f),
        CountryMeta("HK", "Гонконг", "Гонконг", "🇭🇰", 0.805f, 0.470f),
        CountryMeta("KR", "Южная Корея", "Сеул", "🇰🇷", 0.830f, 0.385f),
        CountryMeta("CA", "Канада", "Торонто", "🇨🇦", 0.265f, 0.330f),
        CountryMeta("BR", "Бразилия", "Сан-Паулу", "🇧🇷", 0.345f, 0.680f)
    )

    private val countryByCode: Map<String, CountryMeta> =
        countryCatalog.associateBy { it.code.uppercase() }

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
                decodeUrlComponentSafe(withoutScheme.substring(fragmentIndex + 1)).trim()
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

            val uuid = decodeUrlComponentSafe(userHostPort.substring(0, atIndex)).trim()
            if (uuid.isEmpty()) {
                throw IllegalArgumentException("Некорректный UUID в ссылке vless://")
            }

            val hostPort = userHostPort.substring(atIndex + 1).trim()
            val (host, port) = parseHostPort(hostPort)
            if (host.isBlank()) {
                throw IllegalArgumentException("Не указан хост сервера")
            }

            val params = parseQueryParams(queryString)
            val security = params["security"]?.trim()?.lowercase()?.ifBlank { "none" } ?: "none"
            val encryption = params["encryption"]?.trim()?.ifBlank { "none" } ?: "none"
            val networkType = (params["type"] ?: params["net"])?.trim()?.lowercase()?.ifBlank { "tcp" } ?: "tcp"
            val flow = params["flow"]?.trim() ?: ""
            val sni = (params["sni"] ?: params["servername"] ?: params["host"])?.trim() ?: ""
            val fp = (params["fp"] ?: params["fingerprint"])?.trim()?.ifBlank { "chrome" } ?: "chrome"
            val pbk = (params["pbk"] ?: params["publicKey"] ?: params["publickey"])?.trim() ?: ""
            val sid = (params["sid"] ?: params["shortId"] ?: params["shortid"])?.trim() ?: ""
            val spx = (params["spx"] ?: params["spiderX"] ?: params["spiderx"])?.trim() ?: ""
            val path = params["path"]?.trim()?.ifBlank { "/" } ?: "/"
            val wsHost = (params["host"] ?: "").trim()
            val serviceName = (params["serviceName"] ?: params["servicename"] ?: "").trim()
            val alpn = (params["alpn"] ?: "").trim()

            val effectiveRemark = remarkRaw.ifBlank {
                (params["remarks"] ?: params["remark"] ?: params["ps"] ?: params["name"]).orEmpty().trim()
            }
            val detectedCountry = detectCountry(effectiveRemark, host)
            val displayName = effectiveRemark.ifBlank {
                if (detectedCountry.code == "VPN" || detectedCountry.code == "INFO") {
                    host
                } else {
                    "${detectedCountry.nameRu} (${host})"
                }
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
                pingMs = (24..58).random(),
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
                decodeUrlComponentSafe(withoutScheme.substring(fragmentIdx + 1)).trim()
            } else ""
            val main = if (fragmentIdx != -1) withoutScheme.substring(0, fragmentIdx) else withoutScheme
            val queryIdx = main.indexOf('?')
            val userAndHost = if (queryIdx != -1) main.substring(0, queryIdx) else main

            var method = "chacha20-ietf-poly1305"
            var password = generateRandomUuid()
            val host: String
            val port: Int

            val atIdx = userAndHost.lastIndexOf('@')
            if (atIdx != -1) {
                val userPart = userAndHost.substring(0, atIdx)
                val decodedUser = if (userPart.contains(':')) {
                    decodeUrlComponentSafe(userPart)
                } else {
                    decodeBase64Safe(userPart).ifBlank { decodeUrlComponentSafe(userPart) }
                }
                val colonIdx = decodedUser.indexOf(':')
                if (colonIdx > 0) {
                    method = decodedUser.substring(0, colonIdx).trim()
                    password = decodedUser.substring(colonIdx + 1).trim()
                }
                val hp = parseHostPort(userAndHost.substring(atIdx + 1))
                host = hp.first
                port = hp.second
            } else {
                val decoded = decodeBase64Safe(userAndHost)
                val decAt = decoded.lastIndexOf('@')
                if (decAt != -1) {
                    val userPart = decoded.substring(0, decAt)
                    val colonIdx = userPart.indexOf(':')
                    if (colonIdx > 0) {
                        method = userPart.substring(0, colonIdx).trim()
                        password = userPart.substring(colonIdx + 1).trim()
                    }
                    val hp = parseHostPort(decoded.substring(decAt + 1))
                    host = hp.first
                    port = hp.second
                } else {
                    host = "ss.bushvpn.net"
                    port = 443
                }
            }

            val detectedCountry = detectCountry(remarkRaw, host)
            val displayName = remarkRaw.ifBlank {
                detectedCountry.nameRu
            }
            VlessServerEntity(
                name = displayName,
                countryCode = detectedCountry.code,
                countryName = detectedCountry.nameRu,
                city = detectedCountry.cityRu,
                flagEmoji = detectedCountry.flag,
                host = host,
                port = port,
                uuid = password,
                encryption = method,
                security = "none",
                flow = "",
                networkType = "tcp",
                pingMs = (26..64).random(),
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
     * Parses a raw subscription body (JSON array of Xray configs, plain text, or Base64-encoded list of vless:// and ss:// links).
     */
    fun parseSubscriptionPayload(
        rawPayload: String,
        providerName: String = "SUBSCRIPTION",
        subscriptionUrl: String = ""
    ): List<VlessServerEntity> {
        val trimmed = rawPayload.trim()
        if (trimmed.isEmpty()) return emptyList()

        // 1. Check if payload is a JSON array or single JSON object of Xray configs (used by Happ / INCY / v2raytun subscriptions like KosmoVPN)
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            val jsonParsed = parseXrayJsonSubscription(trimmed, providerName, subscriptionUrl)
            if (jsonParsed.isNotEmpty()) {
                return jsonParsed
            }
        }

        val decodedCandidate = if (
            trimmed.contains("vless://", ignoreCase = true) ||
            trimmed.contains("ss://", ignoreCase = true)
        ) {
            trimmed
        } else {
            val decoded = decodeBase64Safe(trimmed).trim()
            if (decoded.startsWith("[") || decoded.startsWith("{")) {
                val jsonParsed = parseXrayJsonSubscription(decoded, providerName, subscriptionUrl)
                if (jsonParsed.isNotEmpty()) {
                    return jsonParsed
                }
            }
            if (decoded.contains("vless://", ignoreCase = true) || decoded.contains("ss://", ignoreCase = true)) {
                decoded
            } else {
                trimmed
            }
        }

        return decodedCandidate
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
            .filterNot { srv ->
                // Filter out dummy 0.0.0.0:1 unsupported-client warning placeholders if real servers exist
                srv.host == "0.0.0.0" && srv.port == 1
            }
            .toList()
    }

    private fun parseXrayJsonSubscription(
        jsonString: String,
        providerName: String,
        subscriptionUrl: String
    ): List<VlessServerEntity> {
        return runCatching {
            val items = if (jsonString.trim().startsWith("[")) {
                JSONArray(jsonString.trim())
            } else {
                JSONArray().put(JSONObject(jsonString.trim()))
            }

            val results = mutableListOf<VlessServerEntity>()
            for (i in 0 until items.length()) {
                val obj = items.optJSONObject(i) ?: continue
                val remark = obj.optString("remarks", obj.optString("remark", "")).trim()
                val outbounds = obj.optJSONArray("outbounds") ?: continue

                // Find all vless outbounds in this config object; prefer TCP reality if multiple exist (e.g. proxy + reserve)
                val vlessOutbounds = mutableListOf<JSONObject>()
                for (j in 0 until outbounds.length()) {
                    val ob = outbounds.optJSONObject(j) ?: continue
                    if (ob.optString("protocol").equals("vless", ignoreCase = true)) {
                        vlessOutbounds.add(ob)
                    }
                }
                val chosenOutbound = vlessOutbounds.firstOrNull { ob ->
                    ob.optJSONObject("streamSettings")?.optString("network")?.equals("tcp", ignoreCase = true) == true
                } ?: vlessOutbounds.firstOrNull() ?: continue

                val settingsObj = chosenOutbound.optJSONObject("settings") ?: continue
                val vnextArr = settingsObj.optJSONArray("vnext") ?: continue
                val vnext = vnextArr.optJSONObject(0) ?: continue
                val host = vnext.optString("address", "").trim()
                val port = vnext.optInt("port", 443)
                if (host.isBlank() || host == "0.0.0.0") continue

                val usersArr = vnext.optJSONArray("users")
                val userObj = usersArr?.optJSONObject(0)
                val uuid = userObj?.optString("id", "")?.trim().orEmpty()
                if (uuid.isBlank()) continue
                val encryption = userObj?.optString("encryption", "none")?.trim()?.ifBlank { "none" } ?: "none"
                val flow = userObj?.optString("flow", "")?.trim().orEmpty()

                val stream = chosenOutbound.optJSONObject("streamSettings")
                val netType = stream?.optString("network", "tcp")?.trim()?.lowercase()?.ifBlank { "tcp" } ?: "tcp"
                val secType = stream?.optString("security", "none")?.trim()?.lowercase()?.ifBlank { "none" } ?: "none"

                val realityObj = stream?.optJSONObject("realitySettings")
                val tlsObj = stream?.optJSONObject("tlsSettings")
                val sni = (realityObj?.optString("serverName") ?: tlsObj?.optString("serverName") ?: "").trim()
                val fp = (realityObj?.optString("fingerprint") ?: tlsObj?.optString("fingerprint") ?: "chrome").trim().ifBlank { "chrome" }
                val pbk = realityObj?.optString("publicKey", "")?.trim().orEmpty()
                val sid = realityObj?.optString("shortId", "")?.trim().orEmpty()
                val spx = realityObj?.optString("spiderX", "")?.trim().orEmpty()

                val wsObj = stream?.optJSONObject("wsSettings")
                val xhttpObj = stream?.optJSONObject("xhttpSettings")
                val grpcObj = stream?.optJSONObject("grpcSettings")
                val path = (xhttpObj?.optString("path") ?: wsObj?.optString("path") ?: "/").trim().ifBlank { "/" }
                val wsHost = (xhttpObj?.optString("host") ?: wsObj?.optJSONObject("headers")?.optString("Host") ?: "").trim()
                val grpcService = grpcObj?.optString("serviceName", "")?.trim().orEmpty()

                val detectedCountry = detectCountry(remark, host)
                val displayName = remark.ifBlank { "${detectedCountry.nameRu} ($host)" }

                results.add(
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
                        security = secType,
                        flow = flow,
                        networkType = netType,
                        sni = sni,
                        fingerprint = fp,
                        publicKey = pbk,
                        shortId = sid,
                        spiderX = spx,
                        wsPath = path,
                        wsHost = wsHost,
                        grpcServiceName = grpcService,
                        alpn = "",
                        pingMs = (28..58).random(),
                        loadPercent = (14..38).random(),
                        bandwidthGbps = 10,
                        isFavorite = true,
                        isCustom = true,
                        category = "Custom",
                        virtualIp = generateVirtualIp(host),
                        mapX = detectedCountry.mapX,
                        mapY = detectedCountry.mapY,
                        providerName = providerName.ifBlank { "SUBSCRIPTION" },
                        protocol = "VLESS",
                        subscriptionUrl = subscriptionUrl
                    )
                )
            }
            results
        }.getOrDefault(emptyList())
    }

    /**
     * Fetches a live Incy / Happ / v2raytun / Xray subscription URL over HTTP/HTTPS and parses all nodes.
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
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "v2raytun/3.10.28 (Android)")
                setRequestProperty("x-hwid", "a1b2c3d4e5f60718")
                setRequestProperty("x-device-os", "Android")
                setRequestProperty("x-ver-os", "14")
                setRequestProperty("x-device-model", "Pixel 8")
                setRequestProperty("Accept", "*/*")
            }

            try {
                val status = conn.responseCode
                val profileTitleHeader = conn.getHeaderField("profile-title")
                    ?: conn.getHeaderField("Profile-Title")
                val resolvedProvider = if (!profileTitleHeader.isNullOrBlank()) {
                    val trimmedTitle = profileTitleHeader.trim()
                    if (trimmedTitle.startsWith("base64:", ignoreCase = true)) {
                        decodeBase64Safe(trimmedTitle.substringAfter(":")).trim().ifBlank { defaultProvider }
                    } else {
                        decodeUrlComponentSafe(trimmedTitle).trim().ifBlank { defaultProvider }
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

            throw IllegalArgumentException("Не удалось загрузить серверы по ссылке подписки")
        }
    }

    private fun decodeUrlComponentSafe(value: String): String {
        return runCatching {
            val sanitized = value.replace(Regex("%(?![0-9a-fA-F]{2})"), "%25")
            URLDecoder.decode(sanitized, "UTF-8")
        }.getOrDefault(value)
    }

    private fun decodeBase64Safe(input: String): String {
        val cleaned = input.trim().replace("\n", "").replace("\r", "").replace(" ", "")
        if (cleaned.isEmpty()) return ""
        val padded = when (cleaned.length % 4) {
            2 -> "$cleaned=="
            3 -> "$cleaned="
            else -> cleaned
        }
        return runCatching {
            String(Base64.decode(padded, Base64.DEFAULT), Charsets.UTF_8)
        }.recoverCatching {
            String(Base64.decode(padded, Base64.URL_SAFE), Charsets.UTF_8)
        }.getOrDefault("")
    }

    fun toUri(server: VlessServerEntity): String {
        if (server.protocol.equals("Shadowsocks", ignoreCase = true)) {
            val rawCreds = "${server.encryption.ifBlank { "chacha20-ietf-poly1305" }}:${server.uuid}"
            val b64 = Base64.encodeToString(rawCreds.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
            val encodedRemark = URLEncoder.encode(server.name, "UTF-8").replace("+", "%20")
            return "ss://$b64@${server.host}:${server.port}#$encodedRemark"
        }

        val params = linkedMapOf<String, String>()
        params["encryption"] = server.encryption.ifBlank { "none" }
        params["security"] = server.security.ifBlank { "none" }
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
        if (server.networkType.equals("ws", ignoreCase = true) || server.networkType.equals("httpupgrade", ignoreCase = true) || server.networkType.equals("xhttp", ignoreCase = true)) {
            if (server.wsPath.isNotBlank()) params["path"] = server.wsPath
            if (server.wsHost.isNotBlank()) params["host"] = server.wsHost
        }
        if (server.networkType.equals("grpc", ignoreCase = true)) {
            if (server.grpcServiceName.isNotBlank()) params["serviceName"] = server.grpcServiceName
            params["mode"] = "gun"
        }
        if (server.alpn.isNotBlank()) {
            params["alpn"] = server.alpn
        }

        val query = params.entries.joinToString("&") { (k, v) ->
            "$k=${URLEncoder.encode(v, "UTF-8").replace("+", "%20")}"
        }
        val encodedRemark = URLEncoder.encode(server.name, "UTF-8").replace("+", "%20")
        return "vless://${server.uuid}@${server.host}:${server.port}?$query#$encodedRemark"
    }

    /**
     * Generates a complete, valid Xray-core v26 configuration JSON using org.json.JSONObject.
     * - When `includeTunInbound = true`, includes the native `"protocol": "tun"` inbound (`xray0`)
     *   for `coreController.startLoop(configJson, tunFd)`.
     * - Uses pure domain/regexp/CIDR routing rules so Xray starts with zero external .dat dependencies.
     */
    fun toXrayJsonConfig(
        server: VlessServerEntity,
        settings: VpnTunnelSettings,
        includeTunInbound: Boolean = true
    ): String {
        val root = JSONObject()

        // 1. Log
        root.put("log", JSONObject().apply {
            put("loglevel", "warning")
        })

        // 2. Stats & Policy for live traffic telemetry
        root.put("stats", JSONObject())
        root.put("policy", JSONObject().apply {
            put("levels", JSONObject().apply {
                put("8", JSONObject().apply {
                    put("handshake", 4)
                    put("connIdle", 300)
                    put("uplinkOnly", 1)
                    put("downlinkOnly", 1)
                })
            })
            put("system", JSONObject().apply {
                put("statsOutboundUplink", true)
                put("statsOutboundDownlink", true)
            })
        })

        // 3. DNS
        root.put("dns", JSONObject().apply {
            put("servers", JSONArray().apply {
                put(settings.primaryDns.ifBlank { "1.1.1.1" })
                put(settings.secondaryDns.ifBlank { "8.8.8.8" })
            })
        })

        // 4. Inbounds (Native TUN inbound + Local SOCKS5 inbound)
        val inbounds = JSONArray()
        if (includeTunInbound) {
            inbounds.put(JSONObject().apply {
                put("tag", "tun")
                put("port", 0)
                put("protocol", "tun")
                put("settings", JSONObject().apply {
                    put("name", "xray0")
                    put("MTU", settings.mtu.coerceIn(1280, 1500))
                    put("userLevel", 8)
                })
                put("sniffing", JSONObject().apply {
                    put("enabled", settings.enableSniffing)
                    put("destOverride", JSONArray().apply {
                        put("http")
                        put("tls")
                    })
                })
            })
        }
        inbounds.put(JSONObject().apply {
            put("tag", "socks")
            put("port", 10808)
            put("listen", "127.0.0.1")
            put("protocol", "socks")
            put("settings", JSONObject().apply {
                put("auth", "noauth")
                put("udp", true)
                put("userLevel", 8)
            })
            put("sniffing", JSONObject().apply {
                put("enabled", settings.enableSniffing)
                put("destOverride", JSONArray().apply {
                    put("http")
                    put("tls")
                })
            })
        })
        root.put("inbounds", inbounds)

        // 5. Outbounds
        val outbounds = JSONArray()
        val proxyOutbound = JSONObject().apply {
            put("tag", "proxy")
            if (server.protocol.equals("Shadowsocks", ignoreCase = true)) {
                put("protocol", "shadowsocks")
                put("settings", JSONObject().apply {
                    put("servers", JSONArray().put(JSONObject().apply {
                        put("address", server.host)
                        put("port", server.port)
                        put("method", server.encryption.ifBlank { "chacha20-ietf-poly1305" })
                        put("password", server.uuid)
                        put("level", 8)
                    }))
                })
            } else {
                put("protocol", "vless")
                put("settings", JSONObject().apply {
                    put("vnext", JSONArray().put(JSONObject().apply {
                        put("address", server.host)
                        put("port", server.port)
                        put("users", JSONArray().put(JSONObject().apply {
                            put("id", server.uuid)
                            put("encryption", server.encryption.ifBlank { "none" })
                            if (server.flow.isNotBlank() && !server.flow.equals("none", ignoreCase = true)) {
                                put("flow", server.flow)
                            }
                            put("level", 8)
                        }))
                    }))
                })

                val netType = server.networkType.lowercase().ifBlank { "tcp" }
                val secType = server.security.lowercase().ifBlank { "none" }
                put("streamSettings", JSONObject().apply {
                    put("network", netType)
                    put("security", secType)

                    when (secType) {
                        "reality" -> {
                            put("realitySettings", JSONObject().apply {
                                put("show", false)
                                put("fingerprint", server.fingerprint.ifBlank { settings.defaultFingerprint.ifBlank { "chrome" } })
                                put("serverName", server.sni)
                                put("publicKey", server.publicKey)
                                put("shortId", server.shortId)
                                put("spiderX", server.spiderX)
                            })
                        }
                        "tls" -> {
                            put("tlsSettings", JSONObject().apply {
                                put("allowInsecure", false)
                                if (server.sni.isNotBlank()) {
                                    put("serverName", server.sni)
                                }
                                put("fingerprint", server.fingerprint.ifBlank { settings.defaultFingerprint.ifBlank { "chrome" } })
                                if (server.alpn.isNotBlank()) {
                                    val alpnArray = JSONArray()
                                    server.alpn.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                                        alpnArray.put(it)
                                    }
                                    if (alpnArray.length() > 0) {
                                        put("alpn", alpnArray)
                                    }
                                }
                            })
                        }
                    }

                    when (netType) {
                        "ws" -> {
                            put("wsSettings", JSONObject().apply {
                                put("path", server.wsPath.ifBlank { "/" })
                                val hostHeader = server.wsHost.ifBlank { server.sni }
                                if (hostHeader.isNotBlank()) {
                                    put("headers", JSONObject().apply {
                                        put("Host", hostHeader)
                                    })
                                }
                            })
                        }
                        "grpc" -> {
                            put("grpcSettings", JSONObject().apply {
                                put("serviceName", server.grpcServiceName)
                                put("multiMode", false)
                            })
                        }
                        "httpupgrade" -> {
                            put("httpupgradeSettings", JSONObject().apply {
                                put("path", server.wsPath.ifBlank { "/" })
                                val hostHeader = server.wsHost.ifBlank { server.sni }
                                if (hostHeader.isNotBlank()) {
                                    put("host", hostHeader)
                                }
                            })
                        }
                        "xhttp", "splithttp" -> {
                            put("xhttpSettings", JSONObject().apply {
                                put("mode", "auto")
                                put("path", server.wsPath.ifBlank { "/" })
                                val hostHeader = server.wsHost.ifBlank { server.sni }
                                if (hostHeader.isNotBlank()) {
                                    put("host", hostHeader)
                                }
                            })
                        }
                    }
                })
            }

            if (settings.enableMux && (server.flow.isBlank() || server.flow == "none")) {
                put("mux", JSONObject().apply {
                    put("enabled", true)
                    put("concurrency", settings.muxConcurrency.coerceIn(1, 32))
                })
            }
        }
        outbounds.put(proxyOutbound)

        outbounds.put(JSONObject().apply {
            put("tag", "direct")
            put("protocol", "freedom")
            put("settings", JSONObject().apply {
                put("domainStrategy", "UseIP")
            })
        })
        outbounds.put(JSONObject().apply {
            put("tag", "block")
            put("protocol", "blackhole")
        })
        root.put("outbounds", outbounds)

        // 6. Routing (Pure domain/regexp/IP CIDR rules without external .dat file dependencies)
        val rules = JSONArray()
        if (settings.blockAdsAndTrackers) {
            rules.put(JSONObject().apply {
                put("type", "field")
                put("domain", JSONArray().apply {
                    put("domain:doubleclick.net")
                    put("domain:googlesyndication.com")
                    put("domain:googleadservices.com")
                    put("domain:app-measurement.com")
                })
                put("outboundTag", "block")
            })
        }
        if (settings.bypassRuDomains || settings.routingMode == RoutingMode.RU_BYPASS) {
            rules.put(JSONObject().apply {
                put("type", "field")
                put("domain", JSONArray().apply {
                    put("regexp:.*\\.ru$")
                    put("regexp:.*\\.рф$")
                    put("regexp:.*\\.su$")
                })
                put("outboundTag", "direct")
            })
        }
        if (settings.bypassLan) {
            rules.put(JSONObject().apply {
                put("type", "field")
                put("ip", JSONArray().apply {
                    put("127.0.0.0/8")
                    put("192.168.0.0/16")
                    put("172.16.0.0/12")
                })
                put("outboundTag", "direct")
            })
        }

        root.put("routing", JSONObject().apply {
            put("domainStrategy", "IPIfNonMatch")
            put("rules", rules)
        })

        return root.toString(2)
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
                val key = decodeUrlComponentSafe(pair.substring(0, idx)).trim()
                val value = decodeUrlComponentSafe(pair.substring(idx + 1)).trim()
                key to value
            } else null
        }.toMap()
    }

    /**
     * Extracts a 2-letter ISO country code and flag emoji directly from any Unicode Regional Indicator
     * flag pair (U+1F1E6..U+1F1FF) present in the text.
     */
    private fun extractUnicodeFlag(text: String): Pair<String, String>? {
        var i = 0
        while (i < text.length) {
            val cp1 = text.codePointAt(i)
            val len1 = Character.charCount(cp1)
            if (cp1 in 0x1F1E6..0x1F1FF && i + len1 < text.length) {
                val cp2 = text.codePointAt(i + len1)
                val len2 = Character.charCount(cp2)
                if (cp2 in 0x1F1E6..0x1F1FF) {
                    val c1 = ('A'.code + (cp1 - 0x1F1E6)).toChar()
                    val c2 = ('A'.code + (cp2 - 0x1F1E6)).toChar()
                    val iso = "$c1$c2"
                    val flagEmoji = text.substring(i, i + len1 + len2)
                    return iso to flagEmoji
                }
            }
            i += len1
        }
        return null
    }

    private fun detectCountry(remark: String, host: String): CountryMeta {
        // 1. Direct Unicode Flag Emoji extraction from remark (100% accurate for all flag emojis)
        val unicodeFlag = extractUnicodeFlag(remark)
        if (unicodeFlag != null) {
            val (iso, flagEmoji) = unicodeFlag
            val normalizedIso = if (iso == "UK") "GB" else iso
            val catalogMatch = countryByCode[normalizedIso]
            if (catalogMatch != null) {
                return catalogMatch
            }
            val cleanLabel = remark.replace(flagEmoji, "").trim().ifBlank { normalizedIso }
            return CountryMeta(
                code = normalizedIso,
                nameRu = cleanLabel,
                cityRu = cleanLabel,
                flag = flagEmoji,
                mapX = 0.52f,
                mapY = 0.30f
            )
        }

        val remarkLower = remark.lowercase()
        val hostLower = host.lowercase()
        val combined = "$remarkLower $hostLower"

        // 2. Check if this remark is a subscription status / info entry rather than a country
        if (
            remarkLower.contains("подписк") ||
            remarkLower.contains("трафик") ||
            remarkLower.contains("осталось") ||
            remarkLower.contains("истекает") ||
            remarkLower.contains("обновит") ||
            remarkLower.contains("t.me/") ||
            remarkLower.contains("support") ||
            remarkLower.contains("expire")
        ) {
            return CountryMeta(
                code = "INFO",
                nameRu = remark.ifBlank { "Инфо" },
                cityRu = "Инфо",
                flag = "🌐",
                mapX = 0.50f,
                mapY = 0.30f
            )
        }

        // 3. Explicit Russian & English country/city substring matching
        when {
            combined.contains("нидерл") || combined.contains("голланди") || combined.contains("амстердам") || combined.contains("nether") || combined.contains("holland") || combined.contains("amsterdam") -> return countryByCode.getValue("NL")
            combined.contains("герман") || combined.contains("франкфурт") || combined.contains("берлин") || combined.contains("мюнхен") || combined.contains("german") || combined.contains("frankfurt") || combined.contains("berlin") -> return countryByCode.getValue("DE")
            combined.contains("финлянд") || combined.contains("хельсинки") || combined.contains("finland") || combined.contains("helsinki") -> return countryByCode.getValue("FI")
            combined.contains("сша") || combined.contains("америк") || combined.contains("нью-йорк") || combined.contains("вашингтон") || combined.contains("майами") || combined.contains("лос-анджелес") || combined.contains("united states") || combined.contains("usa") || combined.contains("new york") || combined.contains("dallas") || combined.contains("miami") -> return countryByCode.getValue("US")
            combined.contains("великобрит") || combined.contains("англи") || combined.contains("лондон") || combined.contains("united kingdom") || combined.contains("britain") || combined.contains("england") || combined.contains("london") -> return countryByCode.getValue("GB")
            combined.contains("латви") || combined.contains("рига") || combined.contains("latvia") || combined.contains("riga") -> return countryByCode.getValue("LV")
            combined.contains("эстон") || combined.contains("таллин") || combined.contains("estonia") || combined.contains("tallinn") -> return countryByCode.getValue("EE")
            combined.contains("литв") || combined.contains("вильнюс") || combined.contains("lithuania") || combined.contains("vilnius") -> return countryByCode.getValue("LT")
            combined.contains("польш") || combined.contains("варшав") || combined.contains("poland") || combined.contains("warsaw") -> return countryByCode.getValue("PL")
            combined.contains("франц") || combined.contains("париж") || combined.contains("марсель") || combined.contains("france") || combined.contains("paris") -> return countryByCode.getValue("FR")
            combined.contains("швец") || combined.contains("стокгольм") || combined.contains("sweden") || combined.contains("stockholm") -> return countryByCode.getValue("SE")
            combined.contains("швейцар") || combined.contains("цюрих") || combined.contains("женев") || combined.contains("switzer") || combined.contains("zurich") || combined.contains("geneva") -> return countryByCode.getValue("CH")
            combined.contains("турц") || combined.contains("стамбул") || combined.contains("анкар") || combined.contains("turkey") || combined.contains("turkiye") || combined.contains("istanbul") -> return countryByCode.getValue("TR")
            combined.contains("казах") || combined.contains("алмат") || combined.contains("астан") || combined.contains("kazakh") || combined.contains("almaty") || combined.contains("astana") -> return countryByCode.getValue("KZ")
            combined.contains("австри") || combined.contains("вена") || combined.contains("austria") || combined.contains("vienna") -> return countryByCode.getValue("AT")
            combined.contains("чехи") || combined.contains("прага") || combined.contains("czech") || combined.contains("prague") -> return countryByCode.getValue("CZ")
            combined.contains("испан") || combined.contains("мадрид") || combined.contains("барселон") || combined.contains("spain") || combined.contains("madrid") -> return countryByCode.getValue("ES")
            combined.contains("итали") || combined.contains("милан") || combined.contains("рим") || combined.contains("italy") || combined.contains("milan") || combined.contains("rome") -> return countryByCode.getValue("IT")
            combined.contains("оаэ") || combined.contains("эмират") || combined.contains("дубай") || combined.contains("uae") || combined.contains("dubai") -> return countryByCode.getValue("AE")
            combined.contains("япон") || combined.contains("токио") || combined.contains("осак") || combined.contains("japan") || combined.contains("tokyo") -> return countryByCode.getValue("JP")
            combined.contains("сингап") || combined.contains("singapore") -> return countryByCode.getValue("SG")
            combined.contains("норвег") || combined.contains("осло") || combined.contains("norway") || combined.contains("oslo") -> return countryByCode.getValue("NO")
            combined.contains("дани") || combined.contains("копенгаген") || combined.contains("denmark") || combined.contains("copenhagen") -> return countryByCode.getValue("DK")
            combined.contains("бельги") || combined.contains("брюссел") || combined.contains("belgium") || combined.contains("brussels") -> return countryByCode.getValue("BE")
            combined.contains("ирланд") || combined.contains("дублин") || combined.contains("ireland") || combined.contains("dublin") -> return countryByCode.getValue("IE")
            combined.contains("португал") || combined.contains("лиссабон") || combined.contains("portugal") || combined.contains("lisbon") -> return countryByCode.getValue("PT")
            combined.contains("болгар") || combined.contains("софия") || combined.contains("bulgaria") || combined.contains("sofia") -> return countryByCode.getValue("BG")
            combined.contains("румын") || combined.contains("бухарест") || combined.contains("romania") || combined.contains("bucharest") -> return countryByCode.getValue("RO")
            combined.contains("венгр") || combined.contains("будапешт") || combined.contains("hungary") || combined.contains("budapest") -> return countryByCode.getValue("HU")
            combined.contains("серби") || combined.contains("белград") || combined.contains("serbia") || combined.contains("belgrade") -> return countryByCode.getValue("RS")
            combined.contains("молдов") || combined.contains("кишинев") || combined.contains("moldova") || combined.contains("chisinau") -> return countryByCode.getValue("MD")
            combined.contains("украин") || combined.contains("киев") || combined.contains("ukraine") || combined.contains("kyiv") -> return countryByCode.getValue("UA")
            combined.contains("армени") || combined.contains("ереван") || combined.contains("armenia") || combined.contains("yerevan") -> return countryByCode.getValue("AM")
            combined.contains("грузи") || combined.contains("тбилиси") || combined.contains("georgia") || combined.contains("tbilisi") -> return countryByCode.getValue("GE")
            combined.contains("израил") || combined.contains("тель-авив") || combined.contains("israel") || combined.contains("tel aviv") -> return countryByCode.getValue("IL")
            combined.contains("индия") || combined.contains("индии") || combined.contains("мумбаи") || combined.contains("india") || combined.contains("mumbai") -> return countryByCode.getValue("IN")
            combined.contains("гонконг") || combined.contains("hong kong") || combined.contains("hongkong") -> return countryByCode.getValue("HK")
            combined.contains("коре") || combined.contains("сеул") || combined.contains("korea") || combined.contains("seoul") -> return countryByCode.getValue("KR")
            combined.contains("канад") || combined.contains("торонто") || combined.contains("монреал") || combined.contains("canada") || combined.contains("toronto") -> return countryByCode.getValue("CA")
            combined.contains("бразил") || combined.contains("сан-паулу") || combined.contains("brazil") -> return countryByCode.getValue("BR")
        }

        // 4. Token-based matching on remark and host words (e.g. "nl-1", "de.kosmovpn.com", "lv1", "hel")
        val tokens = combined.split(Regex("[^a-zа-яё]+")).filter { it.isNotBlank() }
        for (token in tokens) {
            when (token) {
                "nl", "nld", "ams" -> return countryByCode.getValue("NL")
                "de", "deu", "ger", "fra" -> return countryByCode.getValue("DE")
                "fi", "fin", "hel" -> return countryByCode.getValue("FI")
                "us", "usa", "nyc", "lax", "mia", "ord" -> return countryByCode.getValue("US")
                "gb", "uk", "gbr", "lon" -> return countryByCode.getValue("GB")
                "lv", "lva", "rix" -> return countryByCode.getValue("LV")
                "ee", "est", "tll" -> return countryByCode.getValue("EE")
                "lt", "ltu", "vno" -> return countryByCode.getValue("LT")
                "pl", "pol", "waw" -> return countryByCode.getValue("PL")
                "fr", "par" -> return countryByCode.getValue("FR")
                "se", "swe", "sto", "arn" -> return countryByCode.getValue("SE")
                "ch", "che", "zrh", "gva" -> return countryByCode.getValue("CH")
                "tr", "tur", "ist" -> return countryByCode.getValue("TR")
                "kz", "kaz", "ala", "nqz" -> return countryByCode.getValue("KZ")
                "at", "aut", "vie" -> return countryByCode.getValue("AT")
                "cz", "cze", "prg" -> return countryByCode.getValue("CZ")
                "es", "esp", "mad", "bcn" -> return countryByCode.getValue("ES")
                "it", "ita", "mil", "rom" -> return countryByCode.getValue("IT")
                "ae", "are", "dxb" -> return countryByCode.getValue("AE")
                "jp", "jpn", "tyo", "nrt" -> return countryByCode.getValue("JP")
                "sg", "sgp", "sin" -> return countryByCode.getValue("SG")
                "no", "nor", "osl" -> return countryByCode.getValue("NO")
                "dk", "dnk", "cph" -> return countryByCode.getValue("DK")
                "be", "bel", "bru" -> return countryByCode.getValue("BE")
                "ie", "irl", "dub" -> return countryByCode.getValue("IE")
                "pt", "prt", "lis" -> return countryByCode.getValue("PT")
                "bg", "bgr", "sof" -> return countryByCode.getValue("BG")
                "ro", "rou", "otp" -> return countryByCode.getValue("RO")
                "hu", "hun", "bud" -> return countryByCode.getValue("HU")
                "rs", "srb", "beg" -> return countryByCode.getValue("RS")
                "md", "mda", "rmo" -> return countryByCode.getValue("MD")
                "ua", "ukr", "iev", "kbp" -> return countryByCode.getValue("UA")
                "am", "arm", "evn" -> return countryByCode.getValue("AM")
                "ge", "geo", "tbs" -> return countryByCode.getValue("GE")
                "il", "isr", "tlv" -> return countryByCode.getValue("IL")
                "in", "ind", "bom" -> return countryByCode.getValue("IN")
                "hk", "hkg" -> return countryByCode.getValue("HK")
                "kr", "kor", "icn" -> return countryByCode.getValue("KR")
                "ca", "can", "yyz" -> return countryByCode.getValue("CA")
                "br", "bra", "gru" -> return countryByCode.getValue("BR")
            }
        }

        // 5. Fallback for generic/unlabelled nodes: never mislabel as Finland!
        return CountryMeta(
            code = "VPN",
            nameRu = remark.ifBlank { host },
            cityRu = host,
            flag = "🌐",
            mapX = 0.50f,
            mapY = 0.30f
        )
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
