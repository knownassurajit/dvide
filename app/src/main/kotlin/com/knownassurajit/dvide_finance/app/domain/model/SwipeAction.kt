package com.knownassurajit.dvide_finance.app.domain.model

/** What a swipe on a ledger row does. [NONE] disables the gesture for that direction. */
enum class SwipeAction(val key: String, val label: String) {
    EDIT("edit", "Edit"),
    DELETE("delete", "Delete"),
    NONE("none", "Do nothing");

    companion object {
        /** Unknown or missing keys fall back to [default] so a stale preference can never break the list. */
        fun fromKey(key: String?, default: SwipeAction): SwipeAction =
            entries.firstOrNull { it.key == key } ?: default
    }
}

/** Physical direction of the finger, independent of layout direction (LTR/RTL). */
enum class SwipeSide {
    LEFT, RIGHT;

    companion object {
        /**
         * Compose reports drags relative to the layout direction ("start to end"). Users think in physical
         * directions, so a drag towards the end is a swipe right in LTR but a swipe left in RTL.
         */
        fun fromDrag(towardEnd: Boolean, rtl: Boolean): SwipeSide = if (towardEnd != rtl) RIGHT else LEFT
    }
}

/**
 * User-configurable swipe mapping for ledger rows.
 *
 * Defaults: swipe left → Edit, swipe right → Delete. Delete always goes through the existing
 * confirmation dialog; this class only decides *which* action a direction triggers.
 */
data class SwipeConfig(
    val left: SwipeAction = DEFAULT_LEFT,
    val right: SwipeAction = DEFAULT_RIGHT,
) {
    fun actionFor(side: SwipeSide): SwipeAction = when (side) {
        SwipeSide.LEFT -> left
        SwipeSide.RIGHT -> right
    }

    /**
     * The action that can really run for an entry. Top-up (income) entries are not editable, so an
     * Edit mapping collapses to [SwipeAction.NONE] for them instead of opening an editor that cannot save.
     */
    fun effectiveActionFor(side: SwipeSide, entryEditable: Boolean): SwipeAction {
        val action = actionFor(side)
        return if (action == SwipeAction.EDIT && !entryEditable) SwipeAction.NONE else action
    }

    companion object {
        val DEFAULT_LEFT = SwipeAction.EDIT
        val DEFAULT_RIGHT = SwipeAction.DELETE
        val DEFAULT = SwipeConfig()
    }
}
