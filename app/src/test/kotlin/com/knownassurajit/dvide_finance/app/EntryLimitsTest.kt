package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryLimitsTest {

    @Test
    fun `limits are larger than the previous 32 and 24 character caps`() {
        assertTrue(EntryLimits.NOTE_MAX > 32)
        assertTrue(EntryLimits.CATEGORY_MAX > 24)
    }

    @Test
    fun `text within the limit is untouched`() {
        val s = "a".repeat(EntryLimits.NOTE_MAX)
        assertEquals(s, EntryLimits.clampInput(s, EntryLimits.NOTE_MAX))
    }

    @Test
    fun `text over the limit is cut to the limit`() {
        val s = "a".repeat(EntryLimits.NOTE_MAX + 50)
        assertEquals(EntryLimits.NOTE_MAX, EntryLimits.clampInput(s, EntryLimits.NOTE_MAX).length)
    }

    @Test
    fun `line breaks become single spaces`() {
        assertEquals("a b c d", EntryLimits.clampInput("a\nb\r\nc\rd", 50))
    }

    @Test
    fun `clamping never splits an emoji surrogate pair`() {
        val emoji = "😀" // 😀 = 2 UTF-16 units
        val out = EntryLimits.clampInput("ab$emoji", 3)
        assertEquals("ab", out)
        assertTrue(out.none { Character.isSurrogate(it) })
    }

    @Test
    fun `normalize trims after clamping`() {
        assertEquals("hello", EntryLimits.normalize("  hello  ", 50))
    }

    @Test
    fun `empty input stays empty`() {
        assertEquals("", EntryLimits.normalize("   ", 10))
    }
}
