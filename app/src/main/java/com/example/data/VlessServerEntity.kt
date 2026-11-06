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
    val flow: String = "xtls-rprx-vision",
    val networkType: String = "tcp", // tcp, ws, grpc, httpupgrade, xhttp
    val sni: String = "www.microsoft.com",
    val fingerprint: String = "chrome",
    val publicKey: String = "",
    val shortId: String = "",
    val spiderX: String = "/",
    val wsPath: String = "/",
    val wsHost: String = "",
    val grpcServiceName: String = "vless-grpc",
    val alpn: String = "h2,http/1.1",
    val pingMs: Int = 28,
    val loadPercent: Int = 14,
    val bandwidthGbps: Int = 10,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val category: String = "P2P", // P2P, STREAMING, Core, Custom
    val virtualIp: String = "95.216.140.18",
    val mapX: Float = 0.552f,
    val mapY: Float = 0.225f
) {
    val nodeAirportCode: String
        get() = when (countryCode.uppercase()) {
            "FI" -> "HEL"
            "DE" -> "FRA"
            "PL" -> "WAW"
            "NL" -> "AMS"
            "CH" -> "ZRH"
            "US" -> "NYC"
            "GB" -> "LON"
            "TR" -> "IST"
            "KZ" -> "ALA"
            "SG" -> "SIN"
            "JP" -> "TYO"
            "SE" -> "STO"
            "FR" -> "PAR"
            else -> countryCode.uppercase().take(3)
        }

    val cityImageRes: Int
        get() = when (countryCode.uppercase()) {
            "FI", "SE", "NL" -> R.drawable.img_city_helsinki_bw_1793947042162
            "DE", "FR", "CH", "GB" -> R.drawable.img_city_frankfurt_bw_1793947042312
            else -> R.drawable.img_city_warsaw_bw_1793947041805
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
            "GB" -> "London"
            "TR" -> "Istanbul"
            "KZ" -> "Almaty"
            "SG" -> "Singapore"
            "JP" -> "Tokyo"
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
            "GB" -> "United Kingdom"
            "TR" -> "Turkey"
            "KZ" -> "Kazakhstan"
            "SG" -> "Singapore"
            "JP" -> "Japan"
            else -> countryName
        }
    }

    val protocolBadge: String
        get() = buildString {
            append("VLESS")
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
    val bypassLan: Boolean = true,
    val bypassRuDomains: Boolean = false,
    val blockAdsAndTrackers: Boolean = true,
    val autoReconnect: Boolean = true,
    val killSwitchVisual: Boolean = true,
    val defaultFingerprint: String = "chrome"
)

enum class RoutingMode(val title: String, val subtitle: String) {
    ALL_TRAFFIC(
        "Весь трафик через туннель",
        "Максимальная защита: все приложения и DNS идут через VLESS узел"
    ),
    RU_BYPASS(
        "Умный обход (РФ и локальные напрямую)",
        "Сайты .ru / .рф и банки работают напрямую без задержки, остальной мир — через VLESS"
    ),
    LOW_LATENCY_SPLIT(
        "Только заблокированные и стриминг",
        "Оптимизировано для минимального пинга в играх и 4K стриминга"
    )
}
