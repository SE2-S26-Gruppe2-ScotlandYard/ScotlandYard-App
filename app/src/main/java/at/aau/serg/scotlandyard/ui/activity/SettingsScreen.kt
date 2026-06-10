package at.aau.serg.scotlandyard.ui.activity

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.data.getDisplayModePreference
import at.aau.serg.scotlandyard.data.getLanguagePreference
import at.aau.serg.scotlandyard.data.getServerUriCustomPreference
import at.aau.serg.scotlandyard.data.getServerUriTypePreference
import at.aau.serg.scotlandyard.data.saveDisplayModePreference
import at.aau.serg.scotlandyard.data.saveLanguagePreference
import at.aau.serg.scotlandyard.data.saveServerUriCustomPreference
import at.aau.serg.scotlandyard.data.saveServerUriTypePreference
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.network.ServerConfig
import at.aau.serg.scotlandyard.ui.components.SectionHeader
import at.aau.serg.scotlandyard.ui.components.SidebarItem
import at.aau.serg.scotlandyard.ui.theme.AccentGlow
import at.aau.serg.scotlandyard.ui.theme.AccentTeal
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.ui.theme.SidebarBg
import at.aau.serg.scotlandyard.ui.theme.SidebarBorder
import at.aau.serg.scotlandyard.ui.theme.TextMuted
import at.aau.serg.scotlandyard.ui.theme.TextPrimary
import com.example.scotlandyard.R

private enum class SettingsCategory(@param:StringRes val labelRes: Int, val icon: ImageVector) {
    GAMEBOARD(R.string.title_gameboard, Icons.Default.Map),
    LANGUAGE(R.string.title_language, Icons.Default.Language),
    SERVER(R.string.title_server, Icons.Default.Wifi)
}

private val selectedFactor = 0.86f

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    isInGame: Boolean = false,
    onDisplayModeChange: (BoardDisplayMode) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onServerChange: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GAMEBOARD) }
    var displayMode by remember { mutableStateOf(context.getDisplayModePreference()) }
    var selectedLanguage by remember { mutableStateOf(context.getLanguagePreference()) }
    var serverUriType by remember { mutableStateOf(context.getServerUriTypePreference()) }
    var serverUriCustom by remember { mutableStateOf(context.getServerUriCustomPreference()) }

    BaseScreen(onBackClick = onBackClick, title = stringResource(R.string.title_settings)) { _ ->
        Row(modifier = Modifier.fillMaxSize()) {
            SettingsSidebar(
                selected = selectedCategory,
                isInGame = isInGame,
                onSelect = { selectedCategory = it }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            ) {
                when (selectedCategory) {
                    SettingsCategory.GAMEBOARD -> GameboardSettingsContent(
                        displayMode = displayMode,
                        onModeChange = { mode ->
                            displayMode = mode
                            context.saveDisplayModePreference(mode)
                            onDisplayModeChange(mode)
                        }
                    )

                    SettingsCategory.LANGUAGE -> LanguageSettingsContent(
                        selectedLanguage = selectedLanguage,
                        onLanguageChange = { lang ->
                            selectedLanguage = lang
                            context.saveLanguagePreference(lang)
                            onLanguageChange(lang)
                        }
                    )

                    SettingsCategory.SERVER -> ServerSettingsContent(
                        uriType = serverUriType,
                        customUri = serverUriCustom,
                        isInGame = isInGame,
                        onUriTypeChange = { type ->
                            serverUriType = type
                            context.saveServerUriTypePreference(type)
                            ServerConfig.init(context)
                            onServerChange()
                        },
                        onCustomUriChange = { uri ->
                            serverUriCustom = uri
                            context.saveServerUriCustomPreference(uri)
                            if (serverUriType == "DEVICE") ServerConfig.init(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSidebar(
    selected: SettingsCategory,
    isInGame: Boolean = false,
    onSelect: (SettingsCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SidebarBg.copy(alpha = 0.82f))
            .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.title_settings),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsCategory.entries.forEach { category ->
            val lockedInGame = isInGame && category == SettingsCategory.SERVER
            Box(modifier = Modifier.alpha(if (lockedInGame) 0.4f else 1f)) {
                SidebarItem(
                    label = stringResource(category.labelRes),
                    icon = category.icon,
                    isSelected = category == selected,
                    onClick = { onSelect(category) }
                )
            }
        }
    }
}

@Composable
private fun GameboardSettingsContent(
    displayMode: BoardDisplayMode,
    onModeChange: (BoardDisplayMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        SectionHeader(
            icon = Icons.Default.Map,
            title = stringResource(R.string.title_gameboard),
            subtitle = stringResource(R.string.settings_description_display_option),
            bottomSpacing = 20.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DisplayModeOption(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_gameboard_graph),
                description = stringResource(R.string.settings_gameboard_description_graph),
                previewImageRes = R.drawable.graph_preview,
                isSelected = displayMode == BoardDisplayMode.GRAPH,
                onClick = { onModeChange(BoardDisplayMode.GRAPH) }
            )

            DisplayModeOption(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.settings_gameboard_map),
                description = stringResource(R.string.settings_gameboard_description_map),
                previewImageRes = R.drawable.map,
                isSelected = displayMode == BoardDisplayMode.MAP,
                onClick = { onModeChange(BoardDisplayMode.MAP) }
            )
        }
    }
}

@Composable
private fun DisplayModeOption(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    previewImageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentGlow else SidebarBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) AccentTeal.copy(alpha = selectedFactor) else SidebarBg

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Preview image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0A1520))
                .border(
                    width = 1.dp,
                    color = if (isSelected) AccentGlow.copy(alpha = 0.4f) else SidebarBorder,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = previewImageRes),
                contentDescription = "$title preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Selected checkmark badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .background(AccentGlow, RoundedCornerShape(50))
                ) {
                    Text(
                        text = stringResource(R.string.checkmark),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Mode title
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) AccentGlow else TextPrimary
        )

        // Mode description
        Text(
            text = description,
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun LanguageSettingsContent(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "de" to "Deutsch"
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        SectionHeader(
            icon = Icons.Default.Language,
            title = stringResource(R.string.title_language),
            subtitle = stringResource(R.string.settings_language_description),
            bottomSpacing = 20.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            languages.forEach { (code, name) ->
                LanguageOption(
                    modifier = Modifier.weight(1f),
                    languageName = name,
                    isSelected = selectedLanguage == code,
                    onClick = { onLanguageChange(code) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.settings_language_restart_hint),
            fontSize = 11.sp, color = TextMuted
        )
    }
}

@Composable
private fun LanguageOption(
    modifier: Modifier = Modifier,
    languageName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentGlow else SidebarBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) AccentTeal.copy(alpha = selectedFactor) else SidebarBg

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = languageName,
            fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            color = if (isSelected) AccentGlow else TextPrimary
        )

        if (isSelected) {
            Text(
                text = stringResource(R.string.checkmark),
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = AccentGlow
            )
        }
    }
}

@Composable
private fun ServerSettingsContent(
    uriType: String,
    customUri: String,
    isInGame: Boolean = false,
    onUriTypeChange: (String) -> Unit,
    onCustomUriChange: (String) -> Unit
) {
    val options = listOf(
        "GLOBAL" to stringResource(R.string.settings_server_global),
        "LOCAL" to stringResource(R.string.settings_server_local),
        "DEVICE" to stringResource(R.string.settings_server_custom)
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        SectionHeader(
            icon = Icons.Default.Wifi,
            title = stringResource(R.string.title_server),
            subtitle = if (isInGame)
                stringResource(R.string.settings_server_locked_ingame)
            else
                stringResource(R.string.settings_server_description),
            bottomSpacing = 20.dp
        )

        options.forEach { (type, label) ->
            val isSelected = uriType == type
            val alpha = if (isInGame) 0.38f else 1f
            val borderColor = (if (isSelected) AccentGlow else SidebarBorder).copy(alpha = alpha)
            val borderWidth = if (isSelected) 2.dp else 1.dp
            val bgColor = (if (isSelected) AccentTeal.copy(alpha = selectedFactor) else SidebarBg).copy(alpha = alpha)
            val uriHint = when (type) {
                "GLOBAL" -> ServerConfig.GLOBAL_URI
                "LOCAL" -> ServerConfig.LOCAL_URI
                else -> customUri.takeIf { it.isNotBlank() } ?: ServerConfig.DEVICE_URI
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                    .then(if (!isInGame) Modifier.clickable { onUriTypeChange(type) } else Modifier)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = (if (isSelected) AccentGlow else TextPrimary).copy(alpha = alpha)
                )

                Text(
                    text = uriHint,
                    fontSize = 11.sp,
                    color = TextMuted.copy(alpha = alpha)
                )

                if (isSelected) {
                    Text(
                        text = stringResource(R.string.checkmark),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = AccentGlow.copy(alpha = alpha)
                    )
                }
            }
        }

        if (uriType == "DEVICE") {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customUri,
                onValueChange = if (isInGame) { _ -> } else onCustomUriChange,
                enabled = !isInGame,
                label = {
                    Text(
                        stringResource(R.string.settings_text_custom_uri),
                        color = TextMuted
                    )
                },
                placeholder = { Text("ws://192.168.x.x:8080/scotlandyard", color = TextMuted) },
                singleLine = true,
                colors = colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentGlow,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = AccentGlow,
                    unfocusedLabelColor = TextMuted,
                    cursorColor = AccentGlow
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SettingsScreenPreview() {
    ScotlandYardTheme {
        SettingsScreen(onBackClick = {})
    }
}