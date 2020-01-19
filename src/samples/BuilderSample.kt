package samples

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.concurrent.TimeUnit

fun main() {
    val time = measureTime {
//        usingCoroutineScope()
//        usingLaunchAndCoroutineScope()
//        coroutineVersunsThreads()
//        usinWithTimeout()
//        println("The answer is ${concurrentSum()}")
//        exceptionHandling()
//        testingExceptionHandler()
//        exceptionHandling3()
//        usingSupervisorJob()
        usingSupervisorScope()
    }
    println("finished in $time ms")
}

fun testingExceptionHandler() = runBlocking {

    val job = GlobalScope.launch(errorHandler) {
        throw AssertionError()
    }
    val deferred = GlobalScope.async(errorHandler) {
        throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
    }
    joinAll(job, deferred)
}

fun usingCoroutineScope() = runBlocking {

    coroutineScope {
        computation(1)
    }

    println("before delay")

    delay(1000L)

    println("Cancel!")

    cancel()

    println("Done!")

}

fun usingLaunchAndCoroutineScope() = runBlocking {
    // this: CoroutineScope
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope {
        // Creates a coroutine scope
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // This line will be printed before the nested launch
    }

    println("Coroutine scope is over") // This line is not printed until the nested launch completes
}

fun coroutineVersunsThreads() = runBlocking {
    // this: CoroutineScope
    repeat(100_000) {
        launch {
            delay(1000L)
            print(".")
        }
    }
}

fun usinWithTimeout() = runBlocking {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
}

fun concurrentSum(): Int = runBlocking {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

fun exceptionHandling() = with(CoroutineScope(Dispatchers.Main + errorHandler)) {
    launch {
        launch {
            doSomethingAndThrowAnException()
        }
        try {
            val result = withContext(Dispatchers.IO) {
                doSomethingAndThrowAnException()
            }
            println("$result")
        } catch (e: IOException) {
            println("Fail to get result")
        }
    }
}

fun exceptionHandling3() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            // the first child
            try {
                delay(Long.MAX_VALUE)
            } finally {
                withContext(NonCancellable) {
                    println("Children are cancelled, but exception is not handled until all children terminate")
                    delay(100)
                    println("The first child finished its non cancellable block")
                }
            }
        }
        launch {
            // the second child
            delay(10)
            println("Second child throws an exception")
            throw ArithmeticException()
        }
    }
    job.join()
}

fun usingSupervisorJob() = runBlocking {
    val job = SupervisorJob()
    with(CoroutineScope(coroutineContext + job)) {
        val firstChild = launch(errorHandler) {
            println("First child is failing")
            throw java.lang.AssertionError("First child cancelled")
        }

        val secondChild = launch {
            firstChild.join()

            println("First child is cancelled: ${firstChild.isCancelled}, but second one is still alive")
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("Second child is cancelled because supervisor is cancelled")
            }
        }

        firstChild.join()

        println("Cancelling supervisor")
        job.cancel()
        secondChild.join()
    }
}


fun usingSupervisorScope() = runBlocking {
    supervisorScope {
        val firstChild = launch(errorHandler) {
            println("First child is failing")
            throw java.lang.AssertionError("First child cancelled")
        }

        val secondChild = launch {
            firstChild.join()

            println("First child is cancelled: ${firstChild.isCancelled}, but second one is still alive")
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("Second child is cancelled because supervisor is cancelled")
            }
        }

        firstChild.join()

        println("Cancelling supervisor")
        cancel()
        secondChild.join()
    }
}

suspend fun computation(n: Int) {
    var i = 0
    repeat(5) {
        println("Hello from Job $n: ${i++}")
        delay(800)
    }
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L)
    return 42
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L)
    return 35
}

inline fun measureTime(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    val end = System.nanoTime()
    return TimeUnit.NANOSECONDS.toMillis(end - start)
}

suspend fun doSomethingAndThrowAnException(): Int {
    delay(1000)
    throw IOException("Fail to read file")
}

val errorHandler = CoroutineExceptionHandler { _, throwable ->
    println("uncaught exception $throwable")
}