package io.pleo.antaeus.core.services

import arrow.data.extensions.list.functor.map
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.tasks.Retryable
import io.pleo.antaeus.models.CustomerStatus
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

/**
 * Simple type alias to show that the return value of this method is NOT the result of the computation
 * but the task to be run within another context
 */
typealias BillingTask = IO<List<*>>

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val customerService: CustomerService,
        private val invoiceService: InvoiceService
) : Retryable {
    private fun logger(message: String) = println(message)

    fun bill(): BillingTask = fx {
        !effect { logger("Fetching pending invoices invoices...") }
        val invoices = invoiceService.fetchPending()
        !effect { logger("Done with building invoice list") }
        !effect { logger("Billing customers...") }
        !NonBlocking.parSequence(
                invoices.map { invoice ->
                    handleBilling(invoice)
                        .handleError { this@BillingService.handleBillingErrors(invoice, it) }
                }
        )
    }.fix()

    private fun handleBilling(invoice: Invoice): IO<Invoice> = fx {
        when (!IO { paymentProvider.charge(invoice) }) {
            true -> !IO { invoiceService.updateStatus(invoice) }
            false -> this@BillingService.handleBillingFailures(invoice)
        }
    }

    fun handleBillingFailures(invoice: Invoice): Invoice = fx {
        !effect { logger("Failure to bill customer ${invoice.customerId}") }
        val customer = !IO { customerService.fetch(invoice.customerId) }
        !IO { customerService.updateStatus(customer.copy(customer.id, customer.currency, CustomerStatus.LATE)) }
        invoice
    }.unsafeRunSync()

    fun handleBillingErrors(invoice: Invoice, t: Throwable): Invoice = fx {
        when (t) {
            is CustomerNotFoundException -> !effect { handleCustomerNotFound(invoice) }
            is CurrencyMismatchException -> !effect { handleCurrencyMismatch(t) }
            is InvoiceNotFoundException -> !effect { handleInvoiceNotFound(t) }
            is NetworkException -> !effect { handleNetworkException(invoice) }
        }
        invoice
    }.unsafeRunSync()

    private fun handleCustomerNotFound(invoice: Invoice): Invoice = fx {
        !effect { logger("Customer ${invoice.customerId} not found. Marking as inactive...") }
        val customer = !IO { customerService.fetch(invoice.customerId) }
        !IO { customerService.updateStatus(customer.copy(customer.id, customer.currency, CustomerStatus.INACTIVE)) }
        invoice
    }.unsafeRunSync()

    private fun handleNetworkException(invoice: Invoice) = fx {
        !effect { logger("There was an issue when charging invoice ${invoice.id}. Retrying...") }
        retryWithDelay(3, 1000L) {
            handleBilling(invoice)
                .attempt()
                .unsafeRunSync()
        }.fold(
            { !effect { logger(it) } },
            { invoiceService.updateStatus(invoice.copy(invoice.id, invoice.customerId, invoice.amount, InvoiceStatus.PAID)) }
        )
    }.unsafeRunSync()

    private fun handleCurrencyMismatch(t: CurrencyMismatchException) = t.message?.let { logger(it) }

    private fun handleInvoiceNotFound(t: InvoiceNotFoundException) = t.message?.let { logger(it) }
}
