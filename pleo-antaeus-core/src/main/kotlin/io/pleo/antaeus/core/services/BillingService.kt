package io.pleo.antaeus.core.services

import arrow.effects.IO
import arrow.typeclasses.*
import io.pleo.antaeus.core.external.PaymentProvider

class BillingService<F>(AP: Applicative<F>, paymentProvider: PaymentProvider): Applicative<F> by AP {
    var pp: IO<PaymentProvider> = IO.just(paymentProvider)
}