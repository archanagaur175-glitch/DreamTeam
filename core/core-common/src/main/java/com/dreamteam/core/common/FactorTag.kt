package com.dreamteam.core.common

/**
 * Daily lifestyle factors a user can tag. Extensible: add enum entries and they surface
 * automatically in the logger chip UI and the correlation view.
 */
enum class FactorTag(val displayName: String, val emoji: String) {
    CAFFEINE("Caffeine", "☕"),
    ALCOHOL("Alcohol", "🍷"),
    EXERCISE("Exercise", "🏃"),
    STRESS("Stress", "😰"),
    LATE_SCREEN("Late screens", "📱"),
    NAP("Nap", "😴"),
    HEAVY_MEAL("Heavy meal", "🍕"),
    SCREEN_IN_BED("Screen in bed", "🛏️"),
}
