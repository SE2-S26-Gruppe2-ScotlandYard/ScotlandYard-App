package at.aau.serg.scotlandyard.model

import androidx.compose.ui.graphics.Color
import at.aau.serg.scotlandyard.ui.theme.*

enum class TicketType {
    WALKING,
    ESCOOTER,
    CARSHARING,
    BLACK,
    DOUBLE
}

data class TicketStyle(
    val type: TicketType,
    val label: String,
    val backgroundColor: Color,
    val centerStyle: TicketCenterStyle,
    val centerText: String = ""
)

enum class TicketCenterStyle {
    WalkingIcon,
    EScooterIcon,
    CarIcon,
    EmptyCircle,
    Text2x,
    TextValue
}

object TicketStyleProvider {

    fun fromType(type: TicketType): TicketStyle {
        return when (type) {
            TicketType.WALKING -> TicketStyle(
                type = TicketType.WALKING,
                label = "WALKING",
                backgroundColor = WalkingColor,
                centerStyle = TicketCenterStyle.WalkingIcon
            )
            TicketType.ESCOOTER -> TicketStyle(
                type = TicketType.ESCOOTER,
                label = "E-SCOOTER",
                backgroundColor = EScooterColor,
                centerStyle = TicketCenterStyle.EScooterIcon
            )
            TicketType.CARSHARING -> TicketStyle(
                type = TicketType.CARSHARING,
                label = "CAR SHARING",
                backgroundColor = CarSharingColor,
                centerStyle = TicketCenterStyle.CarIcon
            )
            TicketType.BLACK -> TicketStyle(
                type = TicketType.BLACK,
                label = "BLACK TICKET",
                backgroundColor = BlackColor,
                centerStyle = TicketCenterStyle.EmptyCircle
            )
            TicketType.DOUBLE -> TicketStyle(
                type = TicketType.DOUBLE,
                label = "2X",
                backgroundColor = MRX_COLOR,
                centerStyle = TicketCenterStyle.Text2x,
                centerText = "2x"
            )
        }
    }

    fun allTickets(): List<TicketStyle> {
        return TicketType.entries.map { fromType(it) }
    }
}

