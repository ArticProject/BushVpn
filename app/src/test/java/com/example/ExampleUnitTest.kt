package com.example

import com.example.data.VlessParser
import com.example.data.VpnTunnelSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun vlessUriParser_parsesRealityVisionCorrectly() {
        val sampleUri =
            "vless://9a4d7c12-58f0-4b88-9c31-7f1e62a09d41@nl-ams-01.aethervless.net:443?encryption=none&security=reality&sni=www.microsoft.com&fp=chrome&pbk=kP9mW2xL7vQ4nR8jF1dC5bV0zX3yA6wE9rT2uI4oP1M&sid=6ba85179e30d4fc2&type=tcp&flow=xtls-rprx-vision#NL-AMS%20Reality"
        val result = VlessParser.parseUri(sampleUri)
        assertTrue(result.isSuccess)
        val server = result.getOrThrow()
        assertEquals("nl-ams-01.aethervless.net", server.host)
        assertEquals(443, server.port)
        assertEquals("9a4d7c12-58f0-4b88-9c31-7f1e62a09d41", server.uuid)
        assertEquals("reality", server.security)
        assertEquals("xtls-rprx-vision", server.flow)
        assertEquals("www.microsoft.com", server.sni)
        assertEquals("NL", server.countryCode)

        val rebuiltUri = VlessParser.toUri(server)
        assertTrue(rebuiltUri.startsWith("vless://9a4d7c12-58f0-4b88-9c31-7f1e62a09d41@nl-ams-01.aethervless.net:443"))

        val xrayJson = VlessParser.toXrayJsonConfig(server, VpnTunnelSettings())
        assertTrue(xrayJson.contains("\"protocol\": \"vless\""))
        assertTrue(xrayJson.contains("\"realitySettings\""))
    }
}
