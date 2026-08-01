package com.dreamteam.feature.logger.domain

import com.dreamteam.core.common.FactorTag
import java.time.LocalDate

/** One day's factor tags + optional free note. */
data class DailyLog(
    val date: LocalDate,
    val tags: Set<FactorTag>,
    val freeNote: String? = null,
)
