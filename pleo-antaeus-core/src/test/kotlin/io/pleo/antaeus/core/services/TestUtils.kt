package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.Currency
import kotlin.random.Random

object TestUtils {
    fun randomCurrency(): Currency {
        return Currency.values()[Random.nextInt(0, Currency.values().size)]
    }
}