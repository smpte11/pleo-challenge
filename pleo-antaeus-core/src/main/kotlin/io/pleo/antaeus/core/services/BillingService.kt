package io.pleo.antaeus.core.services

import arrow.effects.IO
import arrow.data.extensions.list.functor.map
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import io.pleo.antaeus.core.external.PaymentProvider

/**
 * Simple type alias to show that the return value of this method is NOT the result of the computation
 * but the task to be run within another context
 */
typealias BillingTask = IO<List<Boolean>>

class BillingService(private val paymentProvider: PaymentProvider) {
    private fun log(message: String) = println(message)

    fun bill(invoiceService: InvoiceService, customerService: CustomerService): BillingTask = fx {
        !effect { log("Fetching customers...") }
        val customers = customerService.fetchAll()
        !effect { log("Done with customers") }
        !effect { log("Fetching all invoices...") }
        val invoices = customers.map { invoiceService.fetch(it.id) }
        !effect { log("Done with building invoice list") }
        !effect { log("Billing customers...") }
        !NonBlocking.parSequence(invoices.map { IO.just(paymentProvider.charge(it)) })
    }.fix()
}