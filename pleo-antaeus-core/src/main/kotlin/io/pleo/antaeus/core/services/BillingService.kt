package io.pleo.antaeus.core.services

import arrow.effects.IO
import arrow.typeclasses.*
import arrow.data.extensions.list.functor.map
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix
import io.pleo.antaeus.core.external.PaymentProvider

class BillingService<F>(M: Monad<F>, private val paymentProvider: PaymentProvider) : Monad<F> by M {
    fun bill(invoiceService: InvoiceService, customerService: CustomerService) = fx {
        val customers = customerService.fetchAll()
        val invoices = customers.map { invoiceService.fetch(it.id) }
        !NonBlocking.parSequence(
                invoices.map { IO.just(paymentProvider.charge(it)) }
        )
    }.fix().unsafeRunSync()
}