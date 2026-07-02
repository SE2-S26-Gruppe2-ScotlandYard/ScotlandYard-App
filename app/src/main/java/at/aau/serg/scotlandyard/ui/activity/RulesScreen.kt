package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.components.SectionHeader
import at.aau.serg.scotlandyard.ui.components.SidebarItem
import at.aau.serg.scotlandyard.ui.theme.AccentGlow
import at.aau.serg.scotlandyard.ui.theme.BlackColor
import at.aau.serg.scotlandyard.ui.theme.CarSharingColor
import at.aau.serg.scotlandyard.ui.theme.EScooterColor
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.ui.theme.SidebarBg
import at.aau.serg.scotlandyard.ui.theme.SidebarBorder
import at.aau.serg.scotlandyard.ui.theme.TextMuted
import at.aau.serg.scotlandyard.ui.theme.TextPrimary
import at.aau.serg.scotlandyard.ui.theme.WalkingColor
import at.aau.serg.scotlandyard.R

private enum class RulesSection(
    val icon: ImageVector,
    val labelRes: Int,
    val descRes: Int
) {
    OBJECTIVE(Icons.Default.EmojiEvents, R.string.rules_title_objective, R.string.rules_description_objective),
    ROLES(Icons.Default.Groups, R.string.rules_title_roles, R.string.rules_description_roles),
    GAMEBOARD(Icons.Default.Map, R.string.title_gameboard, R.string.title_gameboard),
    GAMEPLAY(Icons.Default.Casino, R.string.rules_title_gameplay, R.string.rules_description_gameplay),
    TICKETS(Icons.Default.ConfirmationNumber, R.string.rules_title_tickets, R.string.rules_description_tickets),
    GAME_OVER(Icons.Default.Flag, R.string.rules_title_game_over, R.string.rules_description_game_over),
}

@Composable
fun RulesScreen(onBackClick: () -> Unit) {
    var selectedSection by remember { mutableStateOf(RulesSection.OBJECTIVE) }

    BaseScreen(onBackClick = onBackClick, title = stringResource(R.string.title_rules)) { _ ->
        Row(modifier = Modifier.fillMaxSize()) {
            RulesSidebar(selected = selectedSection, onSelect = { selectedSection = it })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            ) {
                RulesSectionContent(section = selectedSection)
            }
        }
    }
}

@Composable
private fun RulesSidebar(selected: RulesSection, onSelect: (RulesSection) -> Unit) {
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
            text = stringResource(R.string.title_rules).uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        RulesSection.entries.forEach { section ->
            SidebarItem(
                label = stringResource(section.labelRes),
                icon = section.icon,
                isSelected = section == selected,
                onClick = { onSelect(section) }
            )
        }
    }
}


@Composable
private fun RulesSectionContent(section: RulesSection) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        SectionHeader(icon = section.icon, title = stringResource(section.labelRes))

        when (section) {
            RulesSection.GAMEBOARD -> GameboardContent()
            RulesSection.TICKETS   -> TicketsContent()
            else -> RuleCard { Text(stringResource(section.descRes), fontSize = 14.sp, color = TextPrimary, lineHeight = 22.sp) }
        }
    }
}


@Composable
private fun GameboardContent() {
    androidx.compose.foundation.Image(
        painter = painterResource(id = R.drawable.map),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SidebarBorder, RoundedCornerShape(12.dp))
    )
}

@Composable
private fun RuleCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SidebarBg)
            .border(1.dp, SidebarBorder, RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
private fun TicketsContent() {
    data class TransportMode(val nameRes: Int, val detailRes: Int, val color: Color)
    val modes = listOf(
        TransportMode(R.string.ticket_walking,     R.string.rules_movement_walking_detail,    WalkingColor),
        TransportMode(R.string.ticket_escooter,    R.string.rules_movement_escooter_detail,   EScooterColor),
        TransportMode(R.string.ticket_car_sharing, R.string.rules_movement_carsharing_detail, CarSharingColor),
    )

    TicketGroupLabel(stringResource(R.string.rules_tickets_standard_header))
    Spacer(modifier = Modifier.height(8.dp))

    modes.forEach { mode ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(mode.color.copy(alpha = 0.14f))
                .border(1.dp, mode.color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(mode.color, RoundedCornerShape(50)))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(stringResource(mode.nameRes), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = mode.color)
                Spacer(modifier = Modifier.height(2.dp))
                Text(stringResource(mode.detailRes), fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp)
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    RuleCard {
        Text(stringResource(R.string.rules_movement_station_note), fontSize = 13.sp, color = TextMuted, lineHeight = 20.sp)
    }

    Spacer(modifier = Modifier.height(20.dp))
    TicketGroupLabel(stringResource(R.string.rules_tickets_special_header))
    Spacer(modifier = Modifier.height(8.dp))

    SpecialTicketRow(
        name   = stringResource(R.string.ticket_black),
        detail = stringResource(R.string.rules_ticket_black_detail),
        color  = BlackColor
    )
    Spacer(modifier = Modifier.height(8.dp))
    SpecialTicketRow(
        name   = stringResource(R.string.rules_ticket_double),
        detail = stringResource(R.string.rules_ticket_double_detail),
        color  = AccentGlow
    )

    Spacer(modifier = Modifier.height(20.dp))
    TicketGroupLabel(stringResource(R.string.rules_title_important_rules).uppercase())
    Spacer(modifier = Modifier.height(8.dp))
    RuleCard {
        Text(
            stringResource(R.string.rules_description_important_rules),
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun TicketGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = TextMuted
    )
}

@Composable
private fun SpecialTicketRow(name: String, detail: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(detail, fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp)
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun RulesScreenPreview() {
    ScotlandYardTheme {
        RulesScreen(onBackClick = {})
    }
}
