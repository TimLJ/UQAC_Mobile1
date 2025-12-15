package uqac.catwalk.walk

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class walkViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WalkUiState())
    val uiState = _uiState.asStateFlow()

    // La file pour lisser la vitesse est maintenant dans le ViewModel
    private val recentSpeeds = ArrayDeque<Double>(5)

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            val currentTime = location.time
            val previousState = _uiState.value

            // Si on a une localisation précédente, on calcule la distance et la vitesse
            previousState.lastLocation?.let { prevLocation ->
                previousState.previousLocationTime?.let { prevTime ->

                    val distance = prevLocation.distanceTo(location)
                    val elapsedTimeInSeconds = (currentTime - prevTime) / 1000.0

                    // Éviter la division par zéro si le temps n'a pas changé
                    if (elapsedTimeInSeconds > 0) {
                        val speed = distance / elapsedTimeInSeconds

                        // Correction de la logique de la file
                        while (recentSpeeds.size > 5) {
                            recentSpeeds.removeFirst()
                        }
                        recentSpeeds.addLast(speed)

                        val averageSpeed = recentSpeeds.average()
                        val isTooFast = averageSpeed >= 5.6 // 5.6 m/s ≈ 20 km/h

                        _uiState.update { currentState ->
                            currentState.copy(
                                progress = if (!isTooFast) currentState.progress + distance else currentState.progress,
                                isTooFast = isTooFast
                            )
                        }
                    }
                }
            }

            // Mettre à jour la dernière localisation et le temps pour le prochain calcul
            _uiState.update {
                it.copy(
                    lastLocation = location,
                    previousLocationTime = currentTime
                )
            }
        }
    }
}
