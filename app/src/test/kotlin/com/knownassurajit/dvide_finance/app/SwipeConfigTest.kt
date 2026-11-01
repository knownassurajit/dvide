package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.domain.model.SwipeConfig
import com.knownassurajit.dvide_finance.app.domain.model.SwipeSide
import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeConfigTest {

    @Test
    fun `defaults are swipe left edit and swipe right delete`() {
        val c = SwipeConfig.DEFAULT
        assertEquals(SwipeAction.EDIT, c.actionFor(SwipeSide.LEFT))
        assertEquals(SwipeAction.DELETE, c.actionFor(SwipeSide.RIGHT))
    }

    @Test
    fun `custom mapping is honoured per side`() {
        val c = SwipeConfig(left = SwipeAction.DELETE, right = SwipeAction.NONE)
        assertEquals(SwipeAction.DELETE, c.actionFor(SwipeSide.LEFT))
        assertEquals(SwipeAction.NONE, c.actionFor(SwipeSide.RIGHT))
    }

    @Test
    fun `edit collapses to none for entries that cannot be edited`() {
        val c = SwipeConfig.DEFAULT
        assertEquals(SwipeAction.NONE, c.effectiveActionFor(SwipeSide.LEFT, entryEditable = false))
        assertEquals(SwipeAction.EDIT, c.effectiveActionFor(SwipeSide.LEFT, entryEditable = true))
    }

    @Test
    fun `delete stays available for non editable entries`() {
        assertEquals(
            SwipeAction.DELETE,
            SwipeConfig.DEFAULT.effectiveActionFor(SwipeSide.RIGHT, entryEditable = false),
        )
    }

    @Test
    fun `unknown or missing stored keys fall back to the supplied default`() {
        assertEquals(SwipeAction.EDIT, SwipeAction.fromKey(null, SwipeAction.EDIT))
        assertEquals(SwipeAction.DELETE, SwipeAction.fromKey("archive", SwipeAction.DELETE))
        assertEquals(SwipeAction.NONE, SwipeAction.fromKey("none", SwipeAction.EDIT))
    }

    @Test
    fun `every action key round trips`() {
        SwipeAction.entries.forEach { assertEquals(it, SwipeAction.fromKey(it.key, SwipeAction.NONE)) }
    }

    @Test
    fun `drag direction maps to the physical side in both layout directions`() {
        assertEquals(SwipeSide.RIGHT, SwipeSide.fromDrag(towardEnd = true, rtl = false))
        assertEquals(SwipeSide.LEFT, SwipeSide.fromDrag(towardEnd = false, rtl = false))
        assertEquals(SwipeSide.LEFT, SwipeSide.fromDrag(towardEnd = true, rtl = true))
        assertEquals(SwipeSide.RIGHT, SwipeSide.fromDrag(towardEnd = false, rtl = true))
    }
}
