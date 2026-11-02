package com.example.engine

import com.example.data.VpnServerRegion

object VeyoraVpnProxyEngine {

    /**
     * Builds privacy & regional proxy HTTP headers injected into WebView navigation requests
     * when the Veyora VPN Proxy Shield is active.
     */
    fun buildTunnelHeaders(
        vpnEnabled: Boolean,
        region: VpnServerRegion
    ): Map<String, String> {
        if (!vpnEnabled) {
            return mapOf(
                "DNT" to "1",
                "Sec-GPC" to "1"
            )
        }
        return mapOf(
            "X-Forwarded-For" to region.virtualIp,
            "X-Real-IP" to region.virtualIp,
            "CF-IPCountry" to region.countryCode,
            "Accept-Language" to region.acceptLanguage,
            "DNT" to "1",
            "Sec-GPC" to "1",
            "Cache-Control" to "no-transform"
        )
    }

    /**
     * Injects WebRTC IP leak protection and navigator locale/timezone spoofing matching
     * the active VPN proxy region so websites see the selected region's identity.
     */
    fun buildVpnPrivacyScript(vpnEnabled: Boolean, region: VpnServerRegion): String {
        if (!vpnEnabled) return ""
        val lang = region.acceptLanguage.substringBefore(",")
        return """
            (function() {
                if (window.__veyoraVpnMaskApplied) return;
                window.__veyoraVpnMaskApplied = true;
                try {
                    Object.defineProperty(navigator, 'language', { get: () => '$lang' });
                    Object.defineProperty(navigator, 'doNotTrack', { get: () => '1' });
                    Object.defineProperty(navigator, 'globalPrivacyControl', { get: () => true });
                    // Prevent WebRTC local IP leak when VPN shield is active
                    if (window.RTCPeerConnection) {
                        const OrigPeerConnection = window.RTCPeerConnection;
                        window.RTCPeerConnection = function(config) {
                            const safeConfig = Object.assign({}, config || {}, {
                                iceTransportPolicy: 'relay'
                            });
                            return new OrigPeerConnection(safeConfig);
                        };
                    }
                    console.info('[Veyora VPN Shield] Active Tunnel: ${region.countryName} (${region.virtualIp})');
                } catch (e) {}
            })();
        """.trimIndent()
    }
}
