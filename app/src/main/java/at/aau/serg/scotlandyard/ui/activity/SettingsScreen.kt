package at.aau.serg.scotlandyard.ui.activity

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
import androidx.compose.ui.res.painterResource
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

private enum class SettingsCategory(val label: String, val icon: ImageVector) {
    GAMEBOARD("Gameboard", Icons.Default.Map)
    // TODO: Add more categories here [NICKNAME("Change nickname", Icons.Default.Person)] // Shared Preferences needed
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    initialDisplayMode: BoardDisplayMode = BoardDisplayMode.GRAPH,
    onDisplayModeChange: (BoardDisplayMode) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GAMEBOARD) }
    var displayMode by remember { mutableStateOf(initialDisplayMode) }

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
                            onDisplayModeChange(mode)
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
            text = "SETTINGS",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsCategory.entries.forEach { category ->
            SidebarItem(
                label = category.label,
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
            text = "Gameboard",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Select a gameboard display option.",
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
                title = "Graph",
                description = "Draw nodes and edges",
                previewImageRes = R.drawable.graph_preview,
                isSelected = displayMode == BoardDisplayMode.GRAPH,
                onClick = { onModeChange(BoardDisplayMode.GRAPH) }
            )

            DisplayModeOption(
                modifier = Modifier.weight(1f),
                title = "Map",
                description = "Map as background",
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
    val bgColor = if (isSelected) AccentTeal.copy(alpha = 0.18f) else SidebarBg

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
                        text = "✓",
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

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SettingsScreenPreview() {
    ScotlandYardTheme {
        SettingsScreen(onBackClick = {})
    }
}