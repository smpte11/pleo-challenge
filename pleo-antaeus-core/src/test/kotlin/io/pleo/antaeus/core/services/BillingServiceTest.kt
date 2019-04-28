package io.pleo.antaeus.core.services

import arrow.data.ListK
import arrow.data.extensions.listk.monad.monad
import arrow.data.k
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.random.Random

fun randomCurrency(): Currency {
    return Currency.values()[Random.nextInt(0, Currency.values().size)]
}

internal class BillingServiceTest {
    private val provider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService> {
        every { fetch(any()) } returns Invoice(
                id = Random.nextInt(),
                customerId = Random.nextInt(),
                amount = Money(100.toBigDecimal(), randomCurrency()),
                status = InvoiceStatus.PENDING
        )
    }
    private  val customerService = mockk<CustomerService> {
        every { fetchAll() } returns List(10) {
            Customer(it, randomCurrency() )
        }
    }

    @Test
    fun `should init properly`() {
        /**
         */
        val billingService = BillingService(ListK.monad(), provider)
        assertNotNull(billingService)
    }

    @Test
    fun `should bill customers`() {
        val billingService = BillingService(ListK.monad(), provider)
        val result = billingService.bill(invoiceService, customerService)
        print(result)
    }
}