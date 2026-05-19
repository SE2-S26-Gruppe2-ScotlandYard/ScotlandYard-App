package at.aau.serg.scotlandyard.ui.activity

import android.view.KeyEvent

/**
 * Lightweight singleton that allows the Composable layer to receive raw
 * hardware key-events forwarded from [MainActivity.dispatchKeyEvent].
 *
 * Listeners are registered / unregistered in [DisposableEffect] blocks, so
 * they are automatically removed when a screen leaves the composition.
 *
 * Keeping this separate from the UI means the logic is fully testable
 * without requiring an Activity or Compose host.
 */
object CheatKeyEventRegistry {

    private val listeners = mutableListOf<(KeyEvent) -> Unit>()

    /** Register a key-event listener. Call from a [DisposableEffect]. */
    fun addListener(listener: (KeyEvent) -> Unit) {
        synchronized(listeners) { listeners.add(listener) }
    }

    /** Unregister a previously added listener. Call from the onDispose block. */
    fun removeListener(listener: (KeyEvent) -> Unit) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    /**
     * Called by [MainActivity.dispatchKeyEvent]; forwards the event to all
     * currently registered listeners.
     */
    fun notify(event: KeyEvent) {
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { it(event) }
    }
}

