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
import io.pleo.antaeus.models.CustomerStatus
import io.pleo.antaeus.models.Invoice

/**
 * Simple type alias to show that the return value of this method is NOT the result of the computation
 * but the task to be run within another context
 */
typealias BillingTask = IO<List<*>>

class BillingService(
        private val paymentProvider: PaymentProvider,
        val customerService: CustomerService,
        private val invoiceService: InvoiceService
) {
    private fun logger(message: String) = println(message)

    fun bill(): BillingTask = fx {
        !effect { logger("Fetching pending invoices invoices...") }
        val invoices = invoiceService.fetchPending()
        !effect { logger("Done with building invoice list") }
        !effect { logger("Billing customers...") }
        !NonBlocking.parSequence(
                invoices.map { invoice ->
                    fx {
                        when (!IO { paymentProvider.charge(invoice) }) {
                            true -> !IO { invoiceService.updateStatus(invoice) }
                            false -> this@BillingService.handleBillingFailures(invoice)
                        }
                    }.handleError { this@BillingService.handleBillingErrors(invoice, it) }
                }
        )
    }.fix()

    fun handleBillingFailures(invoice: Invoice) = fx {
        !effect { logger("Failure to bill customer ${invoice.customerId}") }
        val customer = !IO { customerService.fetch(invoice.customerId) }
        !IO { customerService.updateStatus(customer.copy(customer.id, customer.currency, CustomerStatus.LATE)) }
        invoice
    }.unsafeRunSync()

    fun handleBillingErrors(invoice: Invoice, t: Throwable) = fx {
        when (t) {
            is CustomerNotFoundException -> !effect { handleCustomerNotFound(invoice) }
            is CurrencyMismatchException -> !effect { handleCurrencyMismatch(t) }
            is InvoiceNotFoundException -> !effect { handleInvoiceNotFound(t) }
            is NetworkException -> !effect { handleNetworkException(t) }
            else -> !effect { logger("Unknown error happened. Please contact whoever is in charge...") }
        }
    }.unsafeRunSync()

    private fun handleCustomerNotFound(invoice: Invoice) = fx {
        !effect { logger("Customer ${invoice.customerId} not found. Marking as inactive...") }
        val customer = !IO { customerService.fetch(invoice.customerId) }
        !IO { customerService.updateStatus(customer.copy(customer.id, customer.currency, CustomerStatus.INACTIVE)) }
        invoice
    }.unsafeRunSync()

    private fun handleCurrencyMismatch(t: CurrencyMismatchException) = t.message?.let { logger(it) }

    private fun handleInvoiceNotFound(t: InvoiceNotFoundException) = t.message?.let { logger(it) }

    private fun handleNetworkException(t: NetworkException) = t.message?.let { logger(it) }
}
