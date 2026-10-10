package com.jsontextfield.dtc.core.domain

import com.jsontextfield.dtc.core.data.IPreferencesRepository
import com.jsontextfield.dtc.core.entities.Stop
import kotlinx.coroutines.flow.first

class SetFavouriteStopUseCase(
    private val preferencesRepository: IPreferencesRepository,
) {
    suspend operator fun invoke(stop: Stop) {
        val favouriteStops = preferencesRepository.getFavouriteStops().first()
        val updatedStops = if (stop.name in favouriteStops) {
            favouriteStops - stop.name
        } else {
            favouriteStops + stop.name
        }
        preferencesRepository.setFavouriteStops(updatedStops)
    }
}