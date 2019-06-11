package io.pleo.antaeus.core.tasks

import arrow.effects.IO
import arrow.effects.IODispatchers
import arrow.effects.extensions.io.fx.fx
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


interface Process {
    var isRunning: Boolean

    fun run(task: () -> Unit): IO<Unit>
}

@Suppress("MemberVisibilityCanBePrivate")
class ScheduledProcess(private val on: LocalDateTime) : Process {
    override var isRunning = true

    override fun run(task: () -> Unit): IO<Unit> = fx {
        continueOn(IODispatchers.CommonPool)
        do when {
            isTaskDate() -> {
                /**
                 * I kept this here cause I was looking into using extension to do function composition
                 * I thought this block of code read really well and was looking into augmenting the syntax
                 * to be more natural. Unfortunately, I ran into issues with suspension, crossinline keyword
                 * and coroutine scope and decided to drop it. I'd like to revisit this idea in the future though.
                 */
//                    task.andThen { delay(untilNextDate()) }
//                test()
                !IO { task() }
                !effect { waitForIt(untilNextDate()) }
            }
        }
        while (isRunning)
    }

    private fun untilNextDate(): Long {
        val now = LocalDateTime.now(ZoneId.of("UTC"))
        val nextMonthAtDate = on.plusMonths(1)
        return now.until(nextMonthAtDate, ChronoUnit.MILLIS)
    }

    private fun isTaskDate(): Boolean {
        val now = LocalDate.now(ZoneId.of("UTC"))
        val fromDateTime = LocalDate.of(on.year, on.month, on.dayOfMonth)
        return now.isEqual(fromDateTime)
    }

    private fun waitForIt(delayOf: Long) = Thread.sleep(delayOf)
}

