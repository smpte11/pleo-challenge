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

/**
 * Simple type alias to show that the return value of this method is NOT the result of the computation
 * but the task to be run within another context
 */
typealias BillingTask = IO<List<*>>

class BillingService(private val paymentProvider: PaymentProvider) {
    private fun logger(message: String) = println(message)

    fun bill(invoiceService: InvoiceService): BillingTask = fx {
        !effect { logger("Fetching pending invoices invoices...") }
        val invoices = invoiceService.fetchPending()
        !effect { logger("Done with building invoice list") }
        !effect { logger("Billing customers...") }
        !NonBlocking.parSequence(
                invoices.map { invoice ->
                    IO { paymentProvider.charge(invoice) }
                            .flatMap { IO { invoiceService.update(invoice) } }
                            .handleError { this@BillingService.handleBillingErrors(it) }
                }
        )
    }.fix()

    fun handleBillingErrors(t: Throwable) = fx {
        when (t) {
            is CustomerNotFoundException -> !effect { handleCustomerNotFound(t) }
            is CurrencyMismatchException -> !effect {handleCurrencyMismatch(t)}
            is InvoiceNotFoundException -> !effect{handleInvoiceNotFound(t)}
            is NetworkException -> !effect {handleNetworkException(t)}
            else -> !effect { logger("Unknown error happened. Please contact whoever is in charge...") }
        }
    }

    private fun handleCustomerNotFound(t: CustomerNotFoundException) = t.message?.let { logger(it) }

    private fun handleCurrencyMismatch(t: CurrencyMismatchException) = t.message?.let { logger(it) }

    private fun handleInvoiceNotFound(t: InvoiceNotFoundException) = t.message?.let { logger(it) }

    private fun handleNetworkException(t: NetworkException) =  t.message?.let { logger(it) }
}
