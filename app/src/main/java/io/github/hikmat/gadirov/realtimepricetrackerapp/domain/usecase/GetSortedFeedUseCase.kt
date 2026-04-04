package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Extracts values from the repository maps, sorting them by highest price.
 */
class GetSortedFeedUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    operator fun invoke(): Flow<List<SymbolPrice>> {
        return repository.prices.map { pricesMap ->
            pricesMap.values.sortedByDescending { it.currentPrice }
        }
    }
}
