package com.dreamteam.feature.circadian.domain

import kotlinx.coroutines.flow.Flow

/** Data boundary for the circadian feature. Implemented in the app module. */
interface CircadianRepository {

    /** Today's energy curve, recomputed when sessions, debt or day changes. */
    fun observeCurve(): Flow<EnergyCurve>
}
