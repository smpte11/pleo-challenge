package io.pleo.antaeus.core.services

import arrow.data.ListK
import arrow.data.extensions.listk.monad.monad
import arrow.data.k
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.BillingFailedException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random



class BillingServiceTest {
    private val provider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
    }
    private val invoiceService = mockk<InvoiceService>(relaxed = true) {
        every { fetch(any()) } returns Invoice(
                id = Random.nextInt(),
                customerId = Random.nextInt(),
                amount = Money(100.toBigDecimal(), TestUtils.randomCurrency()),
                status = TestUtils.randomStatus()
        )
    }
    private  val customerService = mockk<CustomerService> {
        every { fetchAll() } returns List(10) {
            Customer(it, TestUtils.randomCurrency() )
        }
    }

    @Test
    fun `should init properly with the list monad interface`() {
        val billingService = BillingService(provider)
        assertNotNull(billingService)
    }

    @Test
    fun `calling bill should return a lazy task`() {
        val billingService = BillingService(provider)
        billingService.bill(invoiceService, customerService)
        verify {
            provider wasNot Called
        }
    }

    @Test
    fun `triggering billing task should be done explicitly with a certain execution type`() {
        val billingService = BillingService(provider)
        val results = billingService.bill(invoiceService, customerService).unsafeRunSync()
        assertEquals(results.size, 10)
        verify(exactly = 10) {
            provider.charge(allAny())
        }
    }
}