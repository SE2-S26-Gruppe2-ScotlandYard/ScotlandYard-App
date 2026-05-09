package at.aau.serg.scotlandyard.model

import androidx.compose.ui.graphics.Color

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
    private val WalkingTicketColor = Color(0xFFD4B963)
    private val EScooterTicketColor = Color(0xFF3D8E79)
    private val CarSharingTicketColor = Color(0xFFA67C65)
    private val BlackTicketColor = Color(0xFF2C2C2C)
    private val DoubleTicketColor = Color(0xFFF090F5)

    fun fromType(type: TicketType): TicketStyle {
        return when (type) {
            TicketType.WALKING -> TicketStyle(
                type = TicketType.WALKING,
                label = "WALKING",
                backgroundColor = WalkingTicketColor,
                centerStyle = TicketCenterStyle.WalkingIcon
            )
            TicketType.ESCOOTER -> TicketStyle(
                type = TicketType.ESCOOTER,
                label = "E-SCOOTER",
                backgroundColor = EScooterTicketColor,
                centerStyle = TicketCenterStyle.EScooterIcon
            )
            TicketType.CARSHARING -> TicketStyle(
                type = TicketType.CARSHARING,
                label = "CAR SHARING",
                backgroundColor = CarSharingTicketColor,
                centerStyle = TicketCenterStyle.CarIcon
            )
            TicketType.BLACK -> TicketStyle(
                type = TicketType.BLACK,
                label = "BLACK TICKET",
                backgroundColor = BlackTicketColor,
                centerStyle = TicketCenterStyle.EmptyCircle
            )
            TicketType.DOUBLE -> TicketStyle(
                type = TicketType.DOUBLE,
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

