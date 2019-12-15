package samples

import kotlinx.coroutines.*

fun main() = runBlocking {
    val deferred: Deferred<Int> = async(Dispatchers.Default) {
        loadData()
    }
    println("waiting thread ${Thread.currentThread()}")
    log("waiting...")
    log(deferred.await())
}

suspend fun loadData(): Int {
    println("load thread ${Thread.currentThread()}")
    log("loading...")
    delay(1000L)
    log("loaded!")
    return 42
}