package com.example.data

object DefaultServers {
    /**
     * Built-in Free Standard Server (available out of the box).
     */
    fun getInitialServers(): List<VlessServerEntity> = listOf(
        VlessServerEntity(
            name = "Хельсинки • Free",
            countryCode = "FI",
            countryName = "Финляндия",
            city = "Хельсинки",
            flagEmoji = "🇫🇮",
            host = "fi-hel-free.bushvpn.net",
            port = 443,
            uuid = "7f2c1b09-44a8-4d91-b362-8e4a19f05c77",
            encryption = "none",
            security = "reality",
            flow = "xtls-rprx-vision",
            networkType = "tcp",
            sni = "www.apple.com",
            fingerprint = "chrome",
            publicKey = "qW4nL6jF1dC5bV0zX3yA8wE9rT2uI4oPmH8X9vR2kP7",
            shortId = "9901d8f4",
            spiderX = "/",
            pingMs = 28,
            loadPercent = 12,
            bandwidthGbps = 10,
            isFavorite = true,
            isCustom = false,
            category = "FREE",
            virtualIp = "95.216.140.18",
            mapX = 0.552f,
            mapY = 0.225f
        )
    )

    /**
     * Optional Europe Subscription Pack nodes (can be toggled on/off in the Subscriptions tab).
     */
    fun getEuropeSubscriptionNodes(): List<VlessServerEntity> = listOf(
        VlessServerEntity(
            name = "Франкфурт • Pro",
            countryCode = "DE",
            countryName = "Германия",
            city = "Франкфурт",
            flagEmoji = "🇩🇪",
            host = "de-fra-04.bushvpn.net",
            port = 443,
            uuid = "3b8e9a21-67c4-4f19-8a52-1c9d34e07b28",
            encryption = "none",
            security = "reality",
            flow = "xtls-rprx-vision",
            networkType = "tcp",
            sni = "dl.google.com",
            fingerprint = "chrome",
            publicKey = "vR4nL8jF2dC6bV1zX4yA7wE0rT3uI5oP2MkP9mW2xL7",
            shortId = "a1f904c28b3e",
            spiderX = "/watch",
            pingMs = 39,
            loadPercent = 24,
            bandwidthGbps = 10,
            isFavorite = false,
            isCustom = true,
            category = "SUB_EU",
            virtualIp = "91.108.194.42",
            mapX = 0.512f,
            mapY = 0.310f
        ),
        VlessServerEntity(
            name = "Варшава • Pro",
            countryCode = "PL",
            countryName = "Польша",
            city = "Варшава",
            flagEmoji = "🇵🇱",
            host = "pl-waw-01.bushvpn.net",
            port = 443,
            uuid = "5d1a8c34-90e2-4b11-8f45-2c7e61a09d12",
            encryption = "none",
            security = "reality",
            flow = "xtls-rprx-vision",
            networkType = "tcp",
            sni = "www.microsoft.com",
            fingerprint = "chrome",
            publicKey = "kP9mW2xL7vQ4nR8jF1dC5bV0zX3yA6wE9rT2uI4oP1M",
            shortId = "6ba85179e30d4fc2",
            spiderX = "/",
            pingMs = 34,
            loadPercent = 19,
            bandwidthGbps = 10,
            isFavorite = false,
            isCustom = true,
            category = "SUB_EU",
            virtualIp = "185.243.52.19",
            mapX = 0.538f,
            mapY = 0.305f
        )
    )
}
