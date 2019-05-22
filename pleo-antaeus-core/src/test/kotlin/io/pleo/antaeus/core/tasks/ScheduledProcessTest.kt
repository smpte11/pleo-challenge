package io.pleo.antaeus.core.tasks

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.time.ZoneId

@Suppress("RedundantSuspendModifier")
internal class ScheduledProcessTest {
    internal class Foo {
        suspend fun bar() = Unit
    }

    private val onDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))

    @Test
    fun `should run any task multiple times on a given schedule`() {
        val mock = mockk<Foo>(relaxUnitFun = true){ coEvery { bar() } }
        val scheduledProcess: ScheduledProcess = spyk(ScheduledProcess(onDate), recordPrivateCalls = true)
        every { scheduledProcess.isRunning } returnsMany listOf(true, true, false) // runs task more than once
        every { scheduledProcess["isTaskDate"]() } returns true // always on the right date
        every { scheduledProcess["untilNextDate"]() } returns 0L // runs task immediately

        runBlocking { scheduledProcess.run { mock.bar() } }

        coVerify(exactly = 3) { mock.bar() }
    }
}