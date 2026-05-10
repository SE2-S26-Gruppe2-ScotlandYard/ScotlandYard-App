package at.aau.serg.scotlandyard.model

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TicketStyleProviderTest {

    @Test
    fun testFromTypeWalking() {
        val style = TicketStyleProvider.fromType(TicketType.Walking)

        assertEquals(TicketType.Walking, style.type)
        assertEquals("WALKING", style.label)
        assertEquals(Color(0xFFD4B963), style.backgroundColor)
        assertEquals(TicketCenterStyle.WalkingIcon, style.centerStyle)
        assertEquals("", style.centerText)
    }

    @Test
    fun testFromTypeEScooter() {
        val style = TicketStyleProvider.fromType(TicketType.EScooter)

        assertEquals(TicketType.EScooter, style.type)
        assertEquals("E-SCOOTER", style.label)
        assertEquals(Color(0xFF3D8E79), style.backgroundColor)
        assertEquals(TicketCenterStyle.EScooterIcon, style.centerStyle)
        assertEquals("", style.centerText)
    }

    @Test
    fun testFromTypeCarSharing() {
        val style = TicketStyleProvider.fromType(TicketType.CarSharing)

        assertEquals(TicketType.CarSharing, style.type)
        assertEquals("CAR SHARING", style.label)
        assertEquals(Color(0xFFA67C65), style.backgroundColor)
        assertEquals(TicketCenterStyle.CarIcon, style.centerStyle)
        assertEquals("", style.centerText)
    }

    @Test
    fun testFromTypeBlack() {
        val style = TicketStyleProvider.fromType(TicketType.Black)

        assertEquals(TicketType.Black, style.type)
        assertEquals("BLACK TICKET", style.label)
        assertEquals(Color(0xFF2C2C2C), style.backgroundColor)
        assertEquals(TicketCenterStyle.EmptyCircle, style.centerStyle)
        assertEquals("", style.centerText)
    }

    @Test
    fun testFromTypeDouble() {
        val style = TicketStyleProvider.fromType(TicketType.Double)

        assertEquals(TicketType.Double, style.type)
        assertEquals("2X", style.label)
        assertEquals(Color(0xFFF090F5), style.backgroundColor)
        assertEquals(TicketCenterStyle.Text2x, style.centerStyle)
        assertEquals("2x", style.centerText)
    }

    @Test
    fun testAllTicketsReturnsAllTypes() {
        val allTickets = TicketStyleProvider.allTickets()

        assertEquals(5, allTickets.size)
        assertTrue(allTickets.any { it.type == TicketType.Walking })
        assertTrue(allTickets.any { it.type == TicketType.EScooter })
        assertTrue(allTickets.any { it.type == TicketType.CarSharing })
        assertTrue(allTickets.any { it.type == TicketType.Black })
        assertTrue(allTickets.any { it.type == TicketType.Double })
    }

    @Test
    fun testAllTicketsOrderConsistent() {
        val allTickets = TicketStyleProvider.allTickets()

        // Verify all labels are unique
        val labels = allTickets.map { it.label }
        assertEquals(labels.size, labels.distinct().size)

        // Verify all types are unique
        val types = allTickets.map { it.type }
        assertEquals(types.size, types.distinct().size)
    }

    @Test
    fun testTicketColorDifference() {
        val allTickets = TicketStyleProvider.allTickets()

        // Each ticket should have a distinct background color
        val colors = allTickets.map { it.backgroundColor }
        assertEquals(colors.size, colors.distinct().size)
    }

    @Test
    fun testTicketStyleDataClassEquality() {
        val style1 = TicketStyleProvider.fromType(TicketType.Walking)
        val style2 = TicketStyleProvider.fromType(TicketType.Walking)

        assertEquals(style1, style2)
    }
}

