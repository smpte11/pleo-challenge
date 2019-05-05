package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import kotlin.random.Random

object TestUtils {
    fun randomCurrency(): Currency {
        val currencies = Currency.values()
        return currencies[Random.nextInt(0, currencies.size)]
    }

    fun randomStatus(): InvoiceStatus {
        val statuses = InvoiceStatus.values()
        return statuses[Random.nextInt(0, statuses.size)]
    }
}