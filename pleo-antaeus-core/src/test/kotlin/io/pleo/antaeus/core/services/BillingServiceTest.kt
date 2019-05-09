package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random


class BillingServiceTest {
    val seed = {
        Invoice(
                id = Random.nextInt(),
                customerId = Random.nextInt(),
                amount = Money(100.toBigDecimal(), TestUtils.randomCurrency()),
                status = InvoiceStatus.PENDING
        )
    }

    private val provider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
    }
    private val invoiceService = mockk<InvoiceService>(relaxed = true) {
        every { fetchPending() } returns List(10) { seed() }
    }
//    private val customerService = mockk<CustomerService> {
//        every { fetchAll() } returns List(10) {
//            Customer(it, TestUtils.randomCurrency())
//        }
//    }

    private val billingService = BillingService(provider)

    @Test
    fun `triggering billing task should be done explicitly with a certain execution type`() {
        val results = billingService.bill(invoiceService).unsafeRunSync()
        assertEquals(results.size, 10)
        verify(exactly = 10) {
            provider.charge(allAny())
        }
    }

    @Test
    fun `successful charge should lead to post processing`() {
        billingService.bill(invoiceService).unsafeRunSync()
        verifyOrder {
            provider.charge(allAny())
        }
    }

    @Test
    fun `should correctly bill customers`() {
        every { invoiceService.update(any()) } answers {
            Invoice(1, 3, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID)
        }
        billingService.bill(invoiceService).unsafeRunSync()
        val expected = invoiceService.fetchPending()[0]
        verify {
            provider.charge(expected)
            invoiceService.update(expected)
        }
    }
}