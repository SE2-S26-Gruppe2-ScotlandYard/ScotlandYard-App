package at.aau.serg.scotlandyard.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class PreferencesDataStoreSessionTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockContext: Context

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any<String>(), any<Int>())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any<String>(), any<String>())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any<String>(), any<Boolean>())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any<String>())).thenReturn(mockEditor)
    }

    @Test
    fun `saveUserSession stores userId and nickname`() {
        mockContext.saveUserSession("u1", "Stefan")
        verify(mockEditor).putString("user_id", "u1")
        verify(mockEditor).putString("user_nickname", "Stefan")
    }

    @Test
    fun `getUserId returns stored value`() {
        whenever(mockSharedPreferences.getString("user_id", null)).thenReturn("u42")
        assertEquals("u42", mockContext.getUserId())
    }

    @Test
    fun `getUserNickname returns stored value`() {
        whenever(mockSharedPreferences.getString("user_nickname", null)).thenReturn("Sepp")
        assertEquals("Sepp", mockContext.getUserNickname())
    }

    @Test
    fun `saveLobbyId stores when not null`() {
        mockContext.saveLobbyId("LOBBY1")
        verify(mockEditor).putString("lobby_id", "LOBBY1")
    }

    @Test
    fun `saveLobbyId removes when null`() {
        mockContext.saveLobbyId(null)
        verify(mockEditor).remove("lobby_id")
    }

    @Test
    fun `getLobbyId returns stored value`() {
        whenever(mockSharedPreferences.getString("lobby_id", null)).thenReturn("LOBBY1")
        assertEquals("LOBBY1", mockContext.getLobbyId())
    }

    @Test
    fun `saveGameId stores when not null`() {
        mockContext.saveGameId("GAME1")
        verify(mockEditor).putString("game_id", "GAME1")
    }

    @Test
    fun `saveGameId removes when null`() {
        mockContext.saveGameId(null)
        verify(mockEditor).remove("game_id")
    }

    @Test
    fun `getGameId returns stored value`() {
        whenever(mockSharedPreferences.getString("game_id", null)).thenReturn("GAME1")
        assertEquals("GAME1", mockContext.getGameId())
    }

    @Test
    fun `getGameId returns null when not set`() {
        whenever(mockSharedPreferences.getString("game_id", null)).thenReturn(null)
        assertNull(mockContext.getGameId())
    }

    @Test
    fun `savePlayerInfo stores playerId and isMrX`() {
        mockContext.savePlayerInfo("p1", true)
        verify(mockEditor).putString("player_id", "p1")
        verify(mockEditor).putBoolean("is_mrx", true)
    }

    @Test
    fun `getPlayerId returns stored value`() {
        whenever(mockSharedPreferences.getString("player_id", null)).thenReturn("p1")
        assertEquals("p1", mockContext.getPlayerId())
    }

    @Test
    fun `getIsMrX returns stored value`() {
        whenever(mockSharedPreferences.getBoolean("is_mrx", false)).thenReturn(true)
        assertEquals(true, mockContext.getIsMrX())
    }

    @Test
    fun `getIsMrX defaults to false`() {
        whenever(mockSharedPreferences.getBoolean("is_mrx", false)).thenReturn(false)
        assertFalse(mockContext.getIsMrX())
    }

    @Test
    fun `clearSession removes all session keys`() {
        mockContext.clearSession()
        verify(mockEditor).remove("user_id")
        verify(mockEditor).remove("user_nickname")
        verify(mockEditor).remove("lobby_id")
        verify(mockEditor).remove("game_id")
    }
}