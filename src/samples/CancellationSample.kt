package samples

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val job1 = notCancelableJob()
    val job2 = cancelableJob()

    delay(1000L)

    println("Cancel!")

    job1.cancel()
    job2.cancel()

    println("Done!")
}

private fun CoroutineScope.cancelableJob(): Job {
    return launch(Dispatchers.Default) {
        var i = 0
        repeat(5) {
            println("Hello from Job 1: ${i++}")
            delay(500)
        }
    }
}

private fun CoroutineScope.notCancelableJob(): Job {
    val startTime = System.currentTimeMillis()
    return launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) {
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("Hello from Job 2: ${i++}")
                nextPrintTime += 500L
            }
        }
    }
}