package io.pleo.antaeus.core.services

import arrow.data.ListK
import arrow.data.extensions.listk.applicative.applicative
import arrow.effects.IO
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class BillingServiceTest {
    private val provider = mockk<PaymentProvider>()

    @Test
    fun `should init properly`() {
        val billingService = BillingService(ListK.applicative(), provider)
        assertNotNull(billingService)
    }
}