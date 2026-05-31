package at.aau.serg.scotlandyard.data

import android.content.Context
import android.content.SharedPreferences
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class PreferencesTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockContext: Context

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any(), anyInt())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
    }

    @Test
    fun `getDisplayModePreference when no preference set returns graph mode`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(null)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun `getDisplayModePreference when image mode stored returns image mode`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(BoardDisplayMode.MAP.name)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.MAP, mode)
    }

    @Test
    fun `getDisplayModePreference when graph mode stored returns graph mode`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(BoardDisplayMode.GRAPH.name)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun `getDisplayModePreference when invalid value stored returns graph mode`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("INVALID_MODE")

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun `saveDisplayModePreference calls edit with correct value`() {
        mockContext.saveDisplayModePreference(BoardDisplayMode.MAP)

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString("board_display_mode", BoardDisplayMode.MAP.name)
        verify(mockEditor).apply()
    }

    @Test
    fun `saveDisplayModePreference overwrites previous value`() {
        mockContext.saveDisplayModePreference(BoardDisplayMode.GRAPH)
        mockContext.saveDisplayModePreference(BoardDisplayMode.MAP)

        verify(mockEditor, times(2)).putString(eq("board_display_mode"), any())
        verify(mockEditor).putString("board_display_mode", BoardDisplayMode.MAP.name)
    }
}