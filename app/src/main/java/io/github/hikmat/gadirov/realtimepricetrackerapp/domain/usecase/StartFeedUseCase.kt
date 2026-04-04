package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import javax.inject.Inject

class StartFeedUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    operator fun invoke() {
        repository.startFeed()
    }
}
