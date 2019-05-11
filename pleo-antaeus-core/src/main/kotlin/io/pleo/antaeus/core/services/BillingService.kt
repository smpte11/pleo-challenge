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
            invoices.map {invoice ->
                IO { paymentProvider.charge(invoice) }
                    .flatMap { IO {invoiceService.update(invoice)} }
                        .handleError { this@BillingService.handleBillingFailure(it) }
            }
        )
    }.fix()

    fun handleBillingFailure(t: Throwable) = when(t) {
        is CustomerNotFoundException -> handleCustomerNotFound(t)
        is CurrencyMismatchException -> handleCurrencyMismatch(t)
        is InvoiceNotFoundException -> handleInvoiceNotFound(t)
        is NetworkException -> handleNetworkException(t)
        else -> IO { print("") }
    }

    private fun handleCustomerNotFound(t: CustomerNotFoundException) = fx {
        !effect{ t.message?.let { logger(it) } }
    }

    private fun handleCurrencyMismatch(t: CurrencyMismatchException) = fx {
        !effect{ t.message?.let { logger(it) } }
    }

    private fun handleInvoiceNotFound(t: InvoiceNotFoundException) = fx {
        !effect{ t.message?.let { logger(it) } }
    }

    private fun handleNetworkException(t: NetworkException) = fx {
        !effect{ t.message?.let { logger(it) } }
    }
}