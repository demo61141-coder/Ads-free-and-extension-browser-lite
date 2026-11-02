package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VpnServerRegion
import com.example.engine.VeyoraAdBlocker
import com.example.engine.VeyoraVpnProxyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Veyora Browser", appName)
    }

    @Test
    fun `ad blocker intercepts known ad networks and allows news articles`() {
        val blockedDoubleClick = VeyoraAdBlocker.evaluateRequest("https://securepubads.g.doubleclick.net/gampad/ads?iu=/123")
        assertTrue(blockedDoubleClick.isBlocked)

        val blockedAdSense = VeyoraAdBlocker.evaluateRequest("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        assertTrue(blockedAdSense.isBlocked)

        val allowedNewsArticle = VeyoraAdBlocker.evaluateRequest("https://www.prothomalo.com/bangladesh/article-123")
        assertFalse(allowedNewsArticle.isBlocked)
    }

    @Test
    fun `vpn proxy engine generates regional headers`() {
        val headers = VeyoraVpnProxyEngine.buildTunnelHeaders(
            vpnEnabled = true,
            region = VpnServerRegion.GERMANY
        )
        assertEquals("185.220.101.14", headers["X-Forwarded-For"])
        assertEquals("DE", headers["CF-IPCountry"])
    }
}
