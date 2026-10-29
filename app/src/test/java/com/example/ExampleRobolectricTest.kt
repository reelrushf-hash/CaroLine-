package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.commands.LocalCommandParser
import com.example.commands.ParsedAction
import com.example.commands.SystemActionController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CARO LINE AI", appName)
    }

    @Test
    fun testOfflineHindiFlashlightCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = SystemActionController(context)
        val parser = LocalCommandParser(controller)

        val result = parser.parseAndExecute("Jarvis, torch on karo", "hi")
        assertTrue(result.isHandledOffline)
        assertEquals(ParsedAction.Flashlight(true), result.action)
        assertEquals("FLASHLIGHT_ON", result.actionCode)
    }

    @Test
    fun testOfflineBanglaBrowserCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = SystemActionController(context)
        val parser = LocalCommandParser(controller)

        val result = parser.parseAndExecute("আমার ব্রাউজার খোলো", "bn")
        assertTrue(result.isHandledOffline)
        assertEquals("BROWSER", result.actionCode)
    }

    @Test
    fun testOfflineTimeCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = SystemActionController(context)
        val parser = LocalCommandParser(controller)

        val result = parser.parseAndExecute("Abhi kitne baje hain?", "hi")
        assertTrue(result.isHandledOffline)
        assertEquals("TIME", result.actionCode)
    }
}
