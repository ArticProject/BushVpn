package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R

@Entity(tableName = "vless_servers")
data class VlessServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val countryCode: String,
    val countryName: String,
    val city: String,
    val flagEmoji: String,
    val host: String,
    val port: Int = 443,
    val uuid: String,
    val encryption: String = "none",
    val security: String = "reality", // reality, tls, none
    val flow: String = "",
    val networkType: String = "tcp", // tcp, ws, grpc, httpupgrade, xhttp
    val sni: String = "www.microsoft.com",
    val fingerprint: String = "chrome",
    val publicKey: String = "",
    val shortId: String = "",
    val spiderX: String = "/",
    val wsPath: String = "/",
    val wsHost: String = "",
    val grpcServiceName: String = "",
    val alpn: String = "h2,http/1.1",
    val pingMs: Int = 28,
    val loadPercent: Int = 14,
    val bandwidthGbps: Int = 10,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val category: String = "P2P", // P2P, STREAMING, Core, Custom
    val virtualIp: String = "95.216.140.18",
    val mapX: Float = 0.552f,
    val mapY: Float = 0.225f,
    val providerName: String = "BUSH",
    val protocol: String = "VLESS", // VLESS, Shadowsocks
    val subscriptionUrl: String = ""
) {
    val nodeAirportCode: String
        get() = when (countryCode.uppercase()) {
            "FI" -> "HEL"
            "DE" -> "FRA"
            "PL" -> "WAW"
            "NL" -> "AMS"
            "CH" -> "ZRH"
            "US" -> "NYC"
            "GB", "UK" -> "LON"
            "TR" -> "IST"
            "KZ" -> "ALA"
            "SG" -> "SIN"
            "JP" -> "TYO"
            "SE" -> "STO"
            "FR" -> "PAR"
            "LV" -> "RIX"
            "EE" -> "TLL"
            "LT" -> "VNO"
            "AT" -> "VIE"
            "CZ" -> "PRG"
            "ES" -> "MAD"
            "IT" -> "MIL"
            "AE" -> "DXB"
            "NO" -> "OSL"
            "DK" -> "CPH"
            "BE" -> "BRU"
            "IE" -> "DUB"
            "PT" -> "LIS"
            "BG" -> "SOF"
            "RO" -> "OTP"
            "HU" -> "BUD"
            "RS" -> "BEG"
            "MD" -> "RMO"
            "UA" -> "IEV"
            "AM" -> "EVN"
            "GE" -> "TBS"
            "IL" -> "TLV"
            "IN" -> "BOM"
            "HK" -> "HKG"
            "KR" -> "ICN"
            "CA" -> "YYZ"
            "BR" -> "GRU"
            else -> countryCode.uppercase().take(3)
        }

    fun localizedCity(isEn: Boolean): String {
        if (!isEn) return city
        return when (countryCode.uppercase()) {
            "FI" -> "Helsinki"
            "DE" -> "Frankfurt"
            "PL" -> "Warsaw"
            "NL" -> "Amsterdam"
            "CH" -> "Zurich"
            "US" -> "New York"
            "GB", "UK" -> "London"
            "TR" -> "Istanbul"
            "KZ" -> "Almaty"
            "SG" -> "Singapore"
            "JP" -> "Tokyo"
            "SE" -> "Stockholm"
            "FR" -> "Paris"
            "AE" -> "Dubai"
            "LV" -> "Riga"
            "EE" -> "Tallinn"
            "LT" -> "Vilnius"
            "AT" -> "Vienna"
            "CZ" -> "Prague"
            "ES" -> "Madrid"
            "IT" -> "Milan"
            "NO" -> "Oslo"
            "DK" -> "Copenhagen"
            "BE" -> "Brussels"
            "IE" -> "Dublin"
            "PT" -> "Lisbon"
            "BG" -> "Sofia"
            "RO" -> "Bucharest"
            "HU" -> "Budapest"
            "RS" -> "Belgrade"
            "MD" -> "Chisinau"
            "UA" -> "Kyiv"
            "AM" -> "Yerevan"
            "GE" -> "Tbilisi"
            "IL" -> "Tel Aviv"
            "IN" -> "Mumbai"
            "HK" -> "Hong Kong"
            "KR" -> "Seoul"
            "CA" -> "Toronto"
            "BR" -> "Sao Paulo"
            else -> city
        }
    }

    fun localizedCountry(isEn: Boolean): String {
        if (!isEn) return countryName
        return when (countryCode.uppercase()) {
            "FI" -> "Finland"
            "DE" -> "Germany"
            "PL" -> "Poland"
            "NL" -> "Netherlands"
            "CH" -> "Switzerland"
            "US" -> "United States"
            "GB", "UK" -> "United Kingdom"
            "TR" -> "Turkey"
            "KZ" -> "Kazakhstan"
            "SG" -> "Singapore"
            "JP" -> "Japan"
            "SE" -> "Sweden"
            "FR" -> "France"
            "AE" -> "UAE"
            "LV" -> "Latvia"
            "EE" -> "Estonia"
            "LT" -> "Lithuania"
            "AT" -> "Austria"
            "CZ" -> "Czechia"
            "ES" -> "Spain"
            "IT" -> "Italy"
            "NO" -> "Norway"
            "DK" -> "Denmark"
            "BE" -> "Belgium"
            "IE" -> "Ireland"
            "PT" -> "Portugal"
            "BG" -> "Bulgaria"
            "RO" -> "Romania"
            "HU" -> "Hungary"
            "RS" -> "Serbia"
            "MD" -> "Moldova"
            "UA" -> "Ukraine"
            "AM" -> "Armenia"
            "GE" -> "Georgia"
            "IL" -> "Israel"
            "IN" -> "India"
            "HK" -> "Hong Kong"
            "KR" -> "South Korea"
            "CA" -> "Canada"
            "BR" -> "Brazil"
            else -> countryName
        }
    }

    /**
     * Returns the clean server display title:
     * - For the built-in BUSH server (!isCustom), returns localizedCountry(isEn).
     * - For imported subscription/custom servers, strips any duplicate leading flag emoji from `name`
     *   (since `flagEmoji` is already rendered in the left badge) and returns the server's real remark/name.
     */
    fun displayTitle(isEn: Boolean): String {
        if (!isCustom) return localizedCountry(isEn)
        val cleaned = stripLeadingFlags(name).trim()
        return cleaned.ifBlank { localizedCountry(isEn) }
    }

    private fun stripLeadingFlags(input: String): String {
        var text = input.trim()
        if (flagEmoji.isNotBlank() && text.startsWith(flagEmoji)) {
            text = text.removePrefix(flagEmoji).trim()
        }
        // Also strip any leading Unicode Regional Indicator pair (flag emoji)
        var idx = 0
        while (idx < text.length) {
            val cp = text.codePointAt(idx)
            if (cp in 0x1F1E6..0x1F1FF || Character.isWhitespace(cp)) {
                idx += Character.charCount(cp)
            } else {
                break
            }
        }
        return text.substring(idx).trim()
    }

    val protocolBadge: String
        get() = buildString {
            append(protocol.uppercase())
            if (security.equals("reality", ignoreCase = true)) {
                append(" Reality")
            } else if (security.equals("tls", ignoreCase = true)) {
                append(" TLS")
            }
            if (flow.contains("vision", ignoreCase = true)) {
                append(" • Vision")
            } else if (networkType.uppercase() != "TCP") {
                append(" • ${networkType.uppercase()}")
            }
        }
}

data class VpnTunnelSettings(
    val routingMode: RoutingMode = RoutingMode.ALL_TRAFFIC,
    val primaryDns: String = "1.1.1.1",
    val secondaryDns: String = "8.8.8.8",
    val mtu: Int = 1400,
    val enableIpv6: Boolean = false,
    val enableMux: Boolean = false,
    val muxConcurrency: Int = 8,
    val enableFragment: Boolean = true,
    val fragmentPackets: String = "tlshello",
    val fragmentLength: String = "10-30",
    val fragmentInterval: String = "10-20",
    val enableSniffing: Boolean = true,
    val enableFakeDns: Boolean = false,
    val pingType: String = "TCP", // TCP, Proxy (HEAD), ICMP
    val pingUrl: String = "https://cp.cloudflare.com/generate_204",
    val autoUpdateSubs: Boolean = true,
    val updateIntervalHours: Int = 1,
    val updateOnOpen: Boolean = true,
    val pingOnOpen: Boolean = true,
    val collapseSubsByDefault: Boolean = false,
    val subscriptionUserAgent: String = "Happ/1.4 (Android; Xray-core)",
    val perAppProxyMode: String = "Все приложения",
    val showSpeedNotification: Boolean = true,
    val bypassLan: Boolean = true,
    val bypassRuDomains: Boolean = false,
    val blockAdsAndTrackers: Boolean = true,
    val autoReconnect: Boolean = true,
    val killSwitchVisual: Boolean = true,
    val defaultFingerprint: String = "chrome"
)

enum class RoutingMode(val title: String, val subtitle: String) {
    ALL_TRAFFIC(
        "Весь трафик через VPN (Global)",
        "Все приложения и домены маршрутизируются через прокси-туннель"
    ),
    RU_BYPASS(
        "Обход РФ (.ru / .рф / geoip:ru напрямую)",
        "Российские сайты, банки и госсервисы работают напрямую без VPN"
    ),
    LOW_LATENCY_SPLIT(
        "Только заблокированные (Rule-based)",
        "Через туннель идут только заблокированные ресурсы из гео-базы"
    )
}
