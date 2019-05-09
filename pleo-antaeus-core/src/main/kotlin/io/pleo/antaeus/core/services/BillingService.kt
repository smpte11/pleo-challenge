package io.pleo.antaeus.core.services

import arrow.data.extensions.list.functor.map
import arrow.effects.IO
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import io.pleo.antaeus.core.external.PaymentProvider

/**
 * Simple type alias to show that the return value of this method is NOT the result of the computation
 * but the task to be run within another context
 */
typealias BillingTask = IO<List<*>>

class BillingService(private val paymentProvider: PaymentProvider) {
    fun logger(message: String) = println(message)

    fun bill(invoiceService: InvoiceService): BillingTask = fx {
        !effect { logger("Fetching pending invoices invoices...") }
        val invoices = invoiceService.fetchPending()
        !effect { logger("Done with building invoice list") }
        !effect { logger("Billing customers...") }
        !NonBlocking.parSequence(
                invoices.map {invoice ->
                    IO {paymentProvider.charge(invoice) }
                            .flatMap { IO {invoiceService.update(invoice)} }
                }
        )
    }.fix()

    fun handleFailure(vararg i: Exception) = fx {
        !effect { logger("There was a problem handling customer 1") }
    }
}