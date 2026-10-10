package com.jsontextfield.dtc.core.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jsontextfield.dtc.core.data.TtcRealtimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlin.time.Clock

sealed interface TtcArrivalsUIState {
    data object Loading : TtcArrivalsUIState
    data class Success(val arrivals: List<TtcArrivalItem>) : TtcArrivalsUIState
    data class Error(val message: String) : TtcArrivalsUIState
}

@Immutable
data class TtcArrivalItem(
    val tripId: String,
    val routeId: String?,
    val minutes: Int,
    val delaySeconds: Int?,
)

class TtcArrivalsViewModel(
    private val ttcRealtimeRepository: TtcRealtimeRepository,
    val stopId: String?,
) : ViewModel() {

    private val _uiState: MutableStateFlow<TtcArrivalsUIState> =
        MutableStateFlow(TtcArrivalsUIState.Loading)
    val uiState: StateFlow<TtcArrivalsUIState> = _uiState.asStateFlow()

    private val _timeRemaining: MutableStateFlow<Int> = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { TtcArrivalsUIState.Loading }
        fetchArrivals()
        startTimerJob()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_timeRemaining.value in 1001..<20000) {
                _timeRemaining.value = 1000
            }
        }
    }

    private fun startTimerJob() {
        timerJob = timerJob ?: viewModelScope.launch {
            while (isActive) {
                if (_timeRemaining.value <= 1000) {
                    _timeRemaining.value = 0
                    fetchArrivals()
                } else {
                    _timeRemaining.value -= 1000
                }
                delay(1000)
            }
        }.also {
            it.invokeOnCompletion {
                Logger.d("TtcArrivals timerJob cancelled")
            }
        }
    }

    private fun fetchArrivals() {
        viewModelScope.launch {
            runCatching {
                stopId?.let {
                    ttcRealtimeRepository.getArrivals(stopId)
                } ?: emptyList()
            }.onSuccess { arrivals ->
                val now = Clock.System.now().epochSeconds
                val items = arrivals.map { arrival ->
                    val minutes =
                        ((arrival.predictedEpochSeconds - now) / 60L).toInt()
                    TtcArrivalItem(
                        tripId = arrival.tripId,
                        routeId = arrival.routeId,
                        minutes = minutes.coerceAtLeast(0),
                        delaySeconds = arrival.delaySeconds,
                    )
                }.sortedBy { it.minutes }
                _uiState.update {
                    TtcArrivalsUIState.Success(items)
                }
                _timeRemaining.value = 20000
            }.onFailure { exception ->
                if (exception is IOException) {
                    _timeRemaining.value = 1000
                }
                _uiState.update {
                    TtcArrivalsUIState.Error(
                        exception.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
