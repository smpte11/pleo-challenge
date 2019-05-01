package io.pleo.antaeus.core.services

import arrow.effects.IO
import arrow.typeclasses.*
import arrow.data.extensions.list.functor.map
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import io.pleo.antaeus.core.external.PaymentProvider

typealias BillingTask = IO<List<Boolean>>

class BillingService<F>(M: Monad<F>, private val paymentProvider: PaymentProvider) : Monad<F> by M {
    private suspend fun log(message: String) = println(message)

    fun bill(invoiceService: InvoiceService, customerService: CustomerService): BillingTask = fx {
        !effect { log("Fetching customers...") }
        val customers = customerService.fetchAll()
        !effect { log("Done with customers") }
        !effect { log("Fetching all invoices...") }
        val invoices = customers.map { invoiceService.fetch(it.id) }
        !effect { log("Done with building invoice list") }
        !effect { log("Billing customers...") }
        !NonBlocking.parSequence(
                invoices.map { IO.just(paymentProvider.charge(it)) }
        )
    }.fix()
}