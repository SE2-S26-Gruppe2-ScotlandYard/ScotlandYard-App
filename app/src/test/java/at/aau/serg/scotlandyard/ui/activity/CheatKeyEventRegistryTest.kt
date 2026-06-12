package at.aau.serg.scotlandyard.ui.activity

import android.view.KeyEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for CheatKeyEventRegistry.
 *
 * Verifies that listeners are correctly added, notified, and removed.
 * android.view.KeyEvent is mocked via Mockito since it requires Android runtime.
 */
class CheatKeyEventRegistryTest {

    private fun mockKeyEvent(keyCode: Int = KeyEvent.KEYCODE_VOLUME_DOWN): KeyEvent {
        val event = mock(KeyEvent::class.java)
        `when`(event.keyCode).thenReturn(keyCode)
        `when`(event.action).thenReturn(KeyEvent.ACTION_DOWN)
        return event
    }

    @BeforeEach
    fun clearListeners() {
        val noop: (KeyEvent) -> Unit = {}
        CheatKeyEventRegistry.addListener(noop)
        CheatKeyEventRegistry.removeListener(noop)
    }

    @Test
    fun addListener_and_notify_callsListener() {
        var received = false
        val listener: (KeyEvent) -> Unit = { received = true }

        CheatKeyEventRegistry.addListener(listener)
        CheatKeyEventRegistry.notify(mockKeyEvent())

        assertTrue(received, "Listener should have been called after notify()")

        CheatKeyEventRegistry.removeListener(listener)
    }

    @Test
    fun removeListener_stopsReceivingEvents() {
        var callCount = 0
        val listener: (KeyEvent) -> Unit = { callCount++ }

        CheatKeyEventRegistry.addListener(listener)
        CheatKeyEventRegistry.removeListener(listener)
        CheatKeyEventRegistry.notify(mockKeyEvent())

        assertEquals(0, callCount, "Listener should not be called after removal")
    }

    @Test
    fun multipleListeners_allReceiveNotification() {
        var count1 = 0
        var count2 = 0
        val listener1: (KeyEvent) -> Unit = { count1++ }
        val listener2: (KeyEvent) -> Unit = { count2++ }

        CheatKeyEventRegistry.addListener(listener1)
        CheatKeyEventRegistry.addListener(listener2)
        CheatKeyEventRegistry.notify(mockKeyEvent())

        assertEquals(1, count1, "First listener should be called")
        assertEquals(1, count2, "Second listener should be called")

        CheatKeyEventRegistry.removeListener(listener1)
        CheatKeyEventRegistry.removeListener(listener2)
    }

    @Test
    fun notify_withNoListeners_doesNotThrow() {
        assertDoesNotThrow { CheatKeyEventRegistry.notify(mockKeyEvent()) }
    }

    @Test
    fun listener_receivesCorrectKeyCode() {
        var receivedKeyCode = -1
        val listener: (KeyEvent) -> Unit = { event -> receivedKeyCode = event.keyCode }

        CheatKeyEventRegistry.addListener(listener)
        CheatKeyEventRegistry.notify(mockKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN))

        assertEquals(KeyEvent.KEYCODE_VOLUME_DOWN, receivedKeyCode)

        CheatKeyEventRegistry.removeListener(listener)
    }

    @Test
    fun removeNonExistentListener_doesNotThrow() {
        val listener: (KeyEvent) -> Unit = { /* no-op */ }
        assertDoesNotThrow { CheatKeyEventRegistry.removeListener(listener) }
    }
}
