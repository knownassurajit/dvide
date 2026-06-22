package com.knownassurajit.dvide_finance.app.domain.model

data class SeedPreset(val name: String, val hue: Int)

val SEED_PRESETS = listOf(
    SeedPreset("Violet", 300),
    SeedPreset("Indigo", 265),
    SeedPreset("Forest", 152),
    SeedPreset("Clay",   38),
)
