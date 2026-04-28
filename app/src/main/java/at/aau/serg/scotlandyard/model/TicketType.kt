package at.aau.serg.scotlandyard.model

import androidx.compose.ui.graphics.Color

enum class TicketType {
    Walking,
    EScooter,
    CarSharing,
    Black,
    Double
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
    private val WalkingTicketColor = Color(0xFFD4B963)
    private val EScooterTicketColor = Color(0xFF3D8E79)
    private val CarSharingTicketColor = Color(0xFFA67C65)
    private val BlackTicketColor = Color(0xFF2C2C2C)
    private val DoubleTicketColor = Color(0xFFF090F5)

    fun fromType(type: TicketType): TicketStyle {
        return when (type) {
            TicketType.Walking -> TicketStyle(
                type = TicketType.Walking,
                label = "WALKING",
                backgroundColor = WalkingTicketColor,
                centerStyle = TicketCenterStyle.WalkingIcon
            )
            TicketType.EScooter -> TicketStyle(
                type = TicketType.EScooter,
                label = "E-SCOOTER",
                backgroundColor = EScooterTicketColor,
                centerStyle = TicketCenterStyle.EScooterIcon
            )
            TicketType.CarSharing -> TicketStyle(
                type = TicketType.CarSharing,
                label = "CAR SHARING",
                backgroundColor = CarSharingTicketColor,
                centerStyle = TicketCenterStyle.CarIcon
            )
            TicketType.Black -> TicketStyle(
                type = TicketType.Black,
                label = "BLACK TICKET",
                backgroundColor = BlackTicketColor,
                centerStyle = TicketCenterStyle.EmptyCircle
            )
            TicketType.Double -> TicketStyle(
                type = TicketType.Double,
                label = "2X",
                backgroundColor = DoubleTicketColor,
                centerStyle = TicketCenterStyle.Text2x,
                centerText = "2x"
            )
        }
    }

    fun allTickets(): List<TicketStyle> {
        return TicketType.entries.map { fromType(it) }
    }
}

