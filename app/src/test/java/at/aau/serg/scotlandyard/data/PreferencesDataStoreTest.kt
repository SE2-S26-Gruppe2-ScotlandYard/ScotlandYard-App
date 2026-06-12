package at.aau.serg.scotlandyard.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class PreferencesDataStoreTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockConfiguration: Configuration

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any(), anyInt())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
    }

    @Test
    fun getDisplayModePreference_noPreferenceStored_returnsGraphMode() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(null)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun getDisplayModePreference_mapModeStored_returnsMapMode() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(BoardDisplayMode.MAP.name)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.MAP, mode)
    }

    @Test
    fun getDisplayModePreference_graphModeStored_returnsGraphMode() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(BoardDisplayMode.GRAPH.name)

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun getDisplayModePreference_invalidValueStored_returnsGraphMode() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("INVALID_MODE")

        val mode = mockContext.getDisplayModePreference()

        assertEquals(BoardDisplayMode.GRAPH, mode)
    }

    @Test
    fun saveDisplayModePreference_savesCorrectKeyAndValue() {
        mockContext.saveDisplayModePreference(BoardDisplayMode.MAP)

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString("board_display_mode", BoardDisplayMode.MAP.name)
        verify(mockEditor).apply()
    }

    @Test
    fun saveDisplayModePreference_overwritesPreviousValue() {
        mockContext.saveDisplayModePreference(BoardDisplayMode.GRAPH)
        mockContext.saveDisplayModePreference(BoardDisplayMode.MAP)

        verify(mockEditor, times(2)).putString(eq("board_display_mode"), any())
        verify(mockEditor).putString("board_display_mode", BoardDisplayMode.MAP.name)
    }

    @Test
    fun getLanguagePreference_noPreferenceStored_returnsEnglish() {
        whenever(mockSharedPreferences.getString(eq("app_language"), any())).thenReturn(null)

        val lang = mockContext.getLanguagePreference()

        assertEquals("en", lang)
    }

    @Test
    fun getLanguagePreference_englishStored_returnsEnglish() {
        whenever(mockSharedPreferences.getString(eq("app_language"), any())).thenReturn("en")

        val lang = mockContext.getLanguagePreference()

        assertEquals("en", lang)
    }

    @Test
    fun getLanguagePreference_otherStored_returnsOther() {
        whenever(mockSharedPreferences.getString(eq("app_language"), any())).thenReturn("de")

        val lang = mockContext.getLanguagePreference()

        assertEquals("de", lang)
    }

    @Test
    fun saveLanguagePreference_savesCorrectKeyAndValue() {
        mockContext.saveLanguagePreference("de")

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString("app_language", "de")
        verify(mockEditor).apply()
    }

    @Test
    fun saveLanguagePreference_overwritesPreviousValue() {
        mockContext.saveLanguagePreference("en")
        mockContext.saveLanguagePreference("de")

        verify(mockEditor, times(2)).putString(eq("app_language"), any())
        verify(mockEditor).putString("app_language", "de")
    }

    @Test
    fun applyLanguage_validLanguageCodeGiven_returnsNonNullContext() {
        whenever(mockContext.resources).thenReturn(mockResources)
        whenever(mockResources.configuration).thenReturn(mockConfiguration)
        whenever(mockContext.createConfigurationContext(any())).thenReturn(mockContext)

        val result = mockContext.applyLanguage("de")

        assertNotNull(result)
    }

    @Test
    fun applyLanguage_validLanguageCodeGiven_callsCreateConfigurationContext() {
        whenever(mockContext.resources).thenReturn(mockResources)
        whenever(mockResources.configuration).thenReturn(mockConfiguration)
        whenever(mockContext.createConfigurationContext(any())).thenReturn(mockContext)

        mockContext.applyLanguage("en")

        verify(mockContext).createConfigurationContext(any())
    }

    @Test
    fun applyLanguage_germanCodeGiven_setsDefaultLocaleToGerman() {
        whenever(mockContext.resources).thenReturn(mockResources)
        whenever(mockResources.configuration).thenReturn(mockConfiguration)
        whenever(mockContext.createConfigurationContext(any())).thenReturn(mockContext)

        mockContext.applyLanguage("de")

        assertEquals("de", java.util.Locale.getDefault().language)
    }

    @Test
    fun getServerUriTypePreference_noPreferenceStored_returnsGLOBAL() {
        whenever(mockSharedPreferences.getString(eq("server_uri_type"), any())).thenReturn(null)

        val type = mockContext.getServerUriTypePreference()

        assertEquals("GLOBAL", type)
    }

    @Test
    fun getServerUriTypePreference_globalStored_returnsGlobal() {
        whenever(mockSharedPreferences.getString(eq("server_uri_type"), any())).thenReturn("GLOBAL")

        val type = mockContext.getServerUriTypePreference()

        assertEquals("GLOBAL", type)
    }

    @Test
    fun getServerUriTypePreference_localStored_returnsLocal() {
        whenever(mockSharedPreferences.getString(eq("server_uri_type"), any())).thenReturn("LOCAL")

        val type = mockContext.getServerUriTypePreference()

        assertEquals("LOCAL", type)
    }

    @Test
    fun getServerUriTypePreference_deviceStored_returnsDevice() {
        whenever(mockSharedPreferences.getString(eq("server_uri_type"), any())).thenReturn("DEVICE")

        val type = mockContext.getServerUriTypePreference()

        assertEquals("DEVICE", type)
    }

    @Test
    fun saveServerUriTypePreference_savesCorrectKeyAndValue() {
        mockContext.saveServerUriTypePreference("LOCAL")

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString("server_uri_type", "LOCAL")
        verify(mockEditor).apply()
    }

    @Test
    fun saveServerUriTypePreference_overwritesPreviousValue() {
        mockContext.saveServerUriTypePreference("GLOBAL")
        mockContext.saveServerUriTypePreference("DEVICE")

        verify(mockEditor, times(2)).putString(eq("server_uri_type"), any())
        verify(mockEditor).putString("server_uri_type", "DEVICE")
    }

    @Test
    fun getServerUriCustomPreference_noPreferenceStored_returnsEmpty() {
        whenever(mockSharedPreferences.getString(eq("server_uri_custom"), any())).thenReturn(null)

        val uri = mockContext.getServerUriCustomPreference()

        assertEquals("", uri)
    }

    @Test
    fun getServerUriCustomPreference_uriStored_returnsUri() {
        whenever(mockSharedPreferences.getString(eq("server_uri_custom"), any())).thenReturn("ws://192.168.1.2:8080/scotlandyard")

        val uri = mockContext.getServerUriCustomPreference()

        assertEquals("ws://192.168.1.2:8080/scotlandyard", uri)
    }

    @Test
    fun saveServerUriCustomPreference_savesCorrectKeyAndValue() {
        mockContext.saveServerUriCustomPreference("ws://192.168.1.2:8080/scotlandyard")

        verify(mockSharedPreferences).edit()
        verify(mockEditor).putString("server_uri_custom", "ws://192.168.1.2:8080/scotlandyard")
        verify(mockEditor).apply()
    }

    @Test
    fun saveServerUriCustomPreference_emptyStringStored_savesEmpty() {
        mockContext.saveServerUriCustomPreference("")

        verify(mockEditor).putString("server_uri_custom", "")
        verify(mockEditor).apply()
    }

    @Test
    fun saveServerUriCustomPreference_overwritesPreviousValue() {
        mockContext.saveServerUriCustomPreference("ws://old:8080/scotlandyard")
        mockContext.saveServerUriCustomPreference("ws://new:8080/scotlandyard")

        verify(mockEditor, times(2)).putString(eq("server_uri_custom"), any())
        verify(mockEditor).putString("server_uri_custom", "ws://new:8080/scotlandyard")
    }
}