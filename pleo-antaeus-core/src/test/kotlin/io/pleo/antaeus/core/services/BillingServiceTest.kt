package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.exp
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

    private val customerSlot = slot<Customer>()
    private val customer = Customer(1, TestUtils.randomCurrency(), CustomerStatus.ONTIME)
    private val customerService = mockk<CustomerService> {
        every { fetch(any()) } returns customer
    }

    private val billingService = BillingService(provider, customerService, invoiceService)


    @Test
    fun `triggering billing task should be done explicitly with a certain execution type`() {
        val results = billingService.bill().unsafeRunSync()
        assertEquals(results.size, 10)
        verify(exactly = 10) {
            provider.charge(allAny())
        }
    }

    @Test
    fun `successful charge should lead to post processing`() {
        billingService.bill().unsafeRunSync()
        verifyOrder {
            provider.charge(allAny())
        }
    }

    @Test
    fun `should correctly bill customers`() {
        every { provider.charge(any()) } returns true
        every { invoiceService.updateStatus(any()) } answers {
            Invoice(1, 3, Money(100.toBigDecimal(), Currency.DKK), InvoiceStatus.PAID)
        }
        billingService.bill().unsafeRunSync()

        val expected = invoiceService.fetchPending()[0]
        verifyOrder {
            provider.charge(expected)
            invoiceService.updateStatus(expected)
        }
    }

    @Test
    fun `should handle a failure to charge a customer`() {
        val stubbedBillingService = spyk(BillingService(provider, customerService, invoiceService))
        every { stubbedBillingService.handleBillingFailures(any()) }
        every { provider.charge(any()) } returns false andThen true


        stubbedBillingService.bill().unsafeRunSync()

        verify(exactly = 1) {
            stubbedBillingService.handleBillingFailures(any())
            customerService
                .updateStatus(customer = eq(customer.copy(customer.id, customer.currency, CustomerStatus.LATE)))
        }
    }

    @Test
    fun `should handle when a customer is not found`() {
        val slot = slot<Exception>()
        val stubbedBillingService = spyk(BillingService(provider, customerService, invoiceService))
        every { stubbedBillingService.handleBillingErrors(any(), capture(slot)) }

        every { customerService.updateStatus(capture(customerSlot)) } returns customer

        every { provider.charge(any()) } throws CustomerNotFoundException(1) andThen true

        stubbedBillingService.bill().unsafeRunSync()

        verify {
            invoiceService.updateStatus(any()) wasNot Called // Throwing from lifted IO bypassed flatMap call
            stubbedBillingService.handleBillingErrors(any(), match { it is CustomerNotFoundException })
            customerService
                    .updateStatus(customer = eq(customer.copy(customer.id, customer.currency, CustomerStatus.INACTIVE)))
        }
    }
}