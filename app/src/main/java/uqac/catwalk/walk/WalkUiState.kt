package uqac.catwalk.walk

import android.location.Location

data class WalkUiState(
    val progress: Double = 0.0,
    val isTooFast: Boolean = false,
    val lastLocation: Location? = null,
    val previousLocationTime: Long? = null
)
