package io.pleo.antaeus.core.tasks

import arrow.core.*

interface Retryable {
    tailrec fun <T> retry(times: Int, fn: () -> T): Either<String, T> {
        return when (val x =  fn()) {
            is Either.Right<*> -> x.right()
            else -> {
                when (times) {
                    0 -> "Operation couldn't complete".left()
                    else -> retry(times - 1, fn)
                }
            }
        }
    }

    tailrec fun <T> retryWithDelay(times: Int, delay: Long, fn: () -> T): Either<String, T> {
        Thread.sleep(delay)
        return when (val x =  fn()) {
            is Either.Right<*> -> x.right()
            else -> {
                when (times) {
                    0 -> "Operation couldn't complete".left()
                    else -> retryWithDelay(times - 1, delay, fn)
                }
            }
        }
    }
}