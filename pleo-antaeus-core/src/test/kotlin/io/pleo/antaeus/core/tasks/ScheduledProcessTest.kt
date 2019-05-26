package io.pleo.antaeus.core.tasks

import io.mockk.*
import arrow.effects.extensions.io.unsafeRun.runBlocking
import arrow.unsafe
import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.time.ZoneId

internal class ScheduledProcessTest {
    internal class Foo {
        fun bar() = Unit
    }

    private val onDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))

    @Test
    fun `should run any task multiple times on a given schedule`() {
        val mock = mockk<Foo>(relaxUnitFun = true){ every { bar() } }
        val scheduledProcess: ScheduledProcess = spyk(ScheduledProcess(onDate), recordPrivateCalls = true)
        every { scheduledProcess.isRunning } returnsMany listOf(true, true, false) // runs task more than once
        every { scheduledProcess["isTaskDate"]() } returns true // always on the right date
        every { scheduledProcess["untilNextDate"]() } returns 1000L // runs task immediately

        unsafe { runBlocking { scheduledProcess.run { mock.bar() } } }

        verify(exactly = 3) { mock.bar() }
    }
}