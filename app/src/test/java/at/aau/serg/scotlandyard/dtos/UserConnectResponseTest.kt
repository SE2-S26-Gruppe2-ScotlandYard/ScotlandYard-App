package at.aau.serg.scotlandyard.dtos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class UserConnectResponseTest {

    // ── Data class basics ─────────────────────────────────────────────────

    @Test
    fun userConnectResponse_stores_all_fields() {
        val user = User(id = "user-1", nickName = "Hans")
        val response = UserConnectResponse(
            success = true,
            message = "Connected",
            user = user
        )
        assertTrue(response.success)
        assertEquals("Connected", response.message)
        assertNotNull(response.user)
        assertEquals("user-1", response.user!!.id)
        assertEquals("Hans", response.user!!.nickName)
    }

    @Test
    fun userConnectResponse_success_false() {
        val response = UserConnectResponse(
            success = false,
            message = "User already connected",
            user = null
        )
        assertFalse(response.success)
        assertEquals("User already connected", response.message)
        assertNull(response.user)
    }

    @Test
    fun userConnectResponse_equality() {
        val user = User("u1", "Hans")
        val r1 = UserConnectResponse(true, "OK", user)
        val r2 = UserConnectResponse(true, "OK", user)
        assertEquals(r1, r2)
    }

    @Test
    fun userConnectResponse_inequality_different_success() {
        val r1 = UserConnectResponse(true, "OK", null)
        val r2 = UserConnectResponse(false, "OK", null)
        assertNotEquals(r1, r2)
    }

    // ── User data class ────────────────────────────────────────────────────

    @Test
    fun user_stores_id_and_nickname() {
        val user = User(id = "abc-123", nickName = "Martina")
        assertEquals("abc-123", user.id)
        assertEquals("Martina", user.nickName)
    }

    @Test
    fun user_equality() {
        val u1 = User("1", "Hans")
        val u2 = User("1", "Hans")
        assertEquals(u1, u2)
    }

    @Test
    fun user_inequality_different_id() {
        val u1 = User("1", "Hans")
        val u2 = User("2", "Hans")
        assertNotEquals(u1, u2)
    }

    @Test
    fun user_inequality_different_nickname() {
        val u1 = User("1", "Hans")
        val u2 = User("1", "Fritz")
        assertNotEquals(u1, u2)
    }

    @Test
    fun user_copy_works() {
        val original = User("1", "Hans")
        val copy = original.copy(nickName = "Fritz")
        assertEquals("Hans", original.nickName)
        assertEquals("Fritz", copy.nickName)
        assertEquals(original.id, copy.id)
    }

    // ── Null handling ──────────────────────────────────────────────────────

    @Test
    fun userConnectResponse_null_user_on_failure() {
        val response = UserConnectResponse(false, "Error", null)
        assertNull(response.user)
        assertFalse(response.success)
    }

    @Test
    fun userConnectResponse_with_user_on_success() {
        val response = UserConnectResponse(true, "OK", User("u1", "Test"))
        assertNotNull(response.user)
        assertTrue(response.success)
    }

    // ── toString ──────────────────────────────────────────────────────────

    @Test
    fun user_toString_contains_fields() {
        val user = User("u1", "Hans")
        assertTrue(user.toString().contains("u1"))
        assertTrue(user.toString().contains("Hans"))
    }

    @Test
    fun userConnectResponse_toString_contains_success() {
        val response = UserConnectResponse(true, "Connected", null)
        assertTrue(response.toString().contains("true"))
    }
}

