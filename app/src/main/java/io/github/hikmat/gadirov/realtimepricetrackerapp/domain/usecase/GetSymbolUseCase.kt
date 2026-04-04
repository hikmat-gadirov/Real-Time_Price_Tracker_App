package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Filters the global real-time mapping strictly down to a single symbol flow.
 */
class GetSymbolUseCase @Inject constructor(
    private val repository: PriceRepository
) {
    operator fun invoke(symbol: String): Flow<SymbolPrice?> {
        return repository.prices.map { pricesMap ->
            pricesMap[symbol]
        }
    }
}
