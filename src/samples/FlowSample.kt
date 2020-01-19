package samples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import java.net.CacheRequest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

fun sequenceBlockingThread(): Sequence<Int> = sequence {
    for (i in 1..3) {
        Thread.sleep(200)
        yield(i)
    }
}

fun flowNotBlockingThread(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(200)
        emit(i)
    }
}

fun infiniteFlow(): Flow<Int> = flow {
    var i = 0

    println("Starting infinite flow")

    while (true) {
        delay(100)
        emit(i++)
    }
}

fun main() = runBlocking {

//    (1..3).asFlow().collect { value -> println(value) }
//
//    launch {
//        for (i in 1..3) {
//            delay(100)
//            println("I'm not blocked")
//        }
//    }
//
//    flowNotBlockingThread().collect { println(it) }
//
//    withTimeoutOrNull(3500) {
//        infiniteFlow().collect { println(it) }
//    }

    val time1 = measureTimeMillis {
        (1..10)
            .map { request -> async { performRequest(request) } }
            .asFlow()
            .map { it.await() }
            .collect { result ->
                delay(100)
                println(result)
            }
    }

    println("-------Time 1 is $time1 ms-------")

    val time2 = measureTimeMillis {
        (1..10)
            .asFlow()
            .map { request -> performRequest(request) }
            .collect { result ->
                delay(100)
                println(result)
            }
    }

    println("-------Time 2 is $time2 ms-------")

    val time3 = measureTimeMillis {
        (1..10)
            .asFlow()
            .map { request -> performRequest(request) }
            .buffer()
            .collect { result ->
                delay(100)
                println(result)
            }
    }

    println("-------Time 3 is $time3 ms-------")

//    flowOnExample()
//        .flowOn(Dispatchers.Default)
//        .collect { log(it) }

//    val timeToCollectWithBuffer = measureTimeMillis {
//        flowOnExample(n = 10)
//            .buffer()
//            .collect {
//                delay(300)
//                log(it)
//            }
//    }
//
//    println("Process finished in $timeToCollectWithBuffer ms")
//
//    val timeToCollect = measureTimeMillis {
//        flowOnExample(n = 10)
//            .collect {
//                delay(300)
//                log(it)
//            }
//    }
//
//    println("Process finished in $timeToCollect ms")
}

fun flowOnExample(n: Int = 3): Flow<Int> {
    return (1..n).asFlow().transform { i ->
        delay(100)
        log("Emitting $i")
        emit(i)
    }
//    flow {
//        for (i in 1..n) {
//            delay(100)
//            log("Emitting $i")
//            emit(i)
//        }
//    }
}

suspend fun performRequest(request: Int): String {
    println("Performing request for id $request")
    delay((100 * request).toLong())
    return "Result for request $request"
}