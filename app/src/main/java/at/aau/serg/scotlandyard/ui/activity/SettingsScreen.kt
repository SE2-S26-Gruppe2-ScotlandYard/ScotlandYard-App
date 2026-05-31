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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.ui.theme.AccentGlow
import at.aau.serg.scotlandyard.ui.theme.AccentTeal
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.ui.theme.SidebarBg
import at.aau.serg.scotlandyard.ui.theme.SidebarBorder
import at.aau.serg.scotlandyard.ui.theme.TextMuted
import at.aau.serg.scotlandyard.ui.theme.TextPrimary
import com.example.scotlandyard.R
import at.aau.serg.scotlandyard.data.getDisplayModePreference
import at.aau.serg.scotlandyard.data.getLanguagePreference
import at.aau.serg.scotlandyard.data.saveDisplayModePreference
import at.aau.serg.scotlandyard.data.saveLanguagePreference

private enum class SettingsCategory(@StringRes val labelRes: Int, val icon: ImageVector) {
    GAMEBOARD(R.string.title_gameboard, Icons.Default.Map),
    LANGUAGE(R.string.title_language, Icons.Default.Language)
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onDisplayModeChange: (BoardDisplayMode) -> Unit = {},
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GAMEBOARD) }
    var displayMode by remember { mutableStateOf(context.getDisplayModePreference()) }
    var selectedLanguage by remember { mutableStateOf(context.getLanguagePreference()) }

    BaseScreen(onBackClick = onBackClick) { _ ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            SettingsSidebar(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
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
                }
            }
        }
    }
}

@Composable
private fun SettingsSidebar(
    selected: SettingsCategory,
    onSelect: (SettingsCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .background(SidebarBg)
            .border(
                width = 1.dp,
                color = SidebarBorder,
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
            )
            .padding(vertical = 16.dp, horizontal = 8.dp),
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
            SidebarItem(
                label = stringResource(category.labelRes),
                icon = category.icon,
                isSelected = category == selected,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AccentTeal.copy(alpha = 0.35f) else Color.Transparent
    val textColor = if (isSelected) AccentGlow else TextPrimary
    val borderColor = if (isSelected) AccentGlow.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
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

        // Titel
        Text(
            text = stringResource(R.string.title_gameboard),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(R.string.settings_description_display_option),
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 20.dp)
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
    val bgColor = if (isSelected) AccentTeal.copy(alpha = 0.86f) else SidebarBg

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
        Text(
            text = stringResource(R.string.title_language),
            fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = Color.White, modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(R.string.settings_language_description),
            fontSize = 12.sp, color = TextMuted,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            languages.forEach { (code, name) ->
                LanguageOption(
                    modifier = Modifier.weight(1f),
                    languageName = name,
                    languageCode = code,
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
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentGlow else SidebarBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) AccentTeal.copy(alpha = 0.86f) else SidebarBg

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


@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SettingsScreenPreview() {
    ScotlandYardTheme {
        SettingsScreen(onBackClick = {})
    }
}