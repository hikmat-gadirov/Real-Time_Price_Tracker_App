package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Domain UseCase that exposes the connection status of the real-time price feed.
 */
class ObserveConnectionUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    operator fun invoke(): StateFlow<Boolean> {
        return repository.isConnected
    }
}
