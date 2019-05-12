package io.pleo.antaeus.core.tasks

import java.time.LocalDateTime

interface Task {
    fun run()
}

class ScheduledTask(val on: LocalDateTime): Task {
    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}