package io.pleo.antaeus.core.extensions

// Unused but kept for demonstration
inline infix fun <P1, P2, R> ((P1) -> P2).andThen(crossinline f: (P2) -> R) = { p1: P1 -> f(this(p1)) }

