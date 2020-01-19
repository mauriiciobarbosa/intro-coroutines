package samples

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.system.measureTimeMillis

fun main() = runBlocking {

    (1..3).asFlow().collect { value -> println(value) }

    flowNotBlockingThread().collect { println(it) }

    withTimeoutOrNull(3500) {
        infiniteFlow().collect { println(it) }
    }

    comparingPerformance()

    flowOnOperator()

    // Operators for slow consumers
    conflation()
    xxxLatest()

    // Combine operators
    zipOperator()
    combineOperator()

    // Flattening operators
    flatMapConcat()
    flatMapMerge()
    flatMapLatest()
}

suspend fun flatMapLatest() {
    val startTime = System.currentTimeMillis()

    produceInts(n = 4, delay = 100)
        .flatMapLatest { requestFlow(it) }
        .collect {
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }
}

suspend fun flatMapMerge() {
    val startTime = System.currentTimeMillis()

    produceInts(n = 4, delay = 100)
        .map { requestFlow(it) }
        .flattenMerge()
        .collect {
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }
}

suspend fun flatMapConcat() {
    val startTime = System.currentTimeMillis()

    produceInts(n = 4, delay = 100)
        .flatMapConcat { requestFlow(it) }
        .collect {
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }
}

suspend fun zipOperator() {
    val nums = (1..3).asFlow().onEach { delay(300) }
    val chars = ('a'..'e').asFlow().onEach { delay(400) }
    nums
        .zip(chars) { num, char ->
            "$num${char.toUpperCase()}"
        }
        .collect {
            log(it)
        }
}

suspend fun combineOperator() {
    val nums = (1..3).asFlow().onEach { delay(300) }
    val chars = ('a'..'e').asFlow().onEach { delay(400) }
    nums
        .combine(chars) { num, char ->
            "$num${char.toUpperCase()}"
        }
        .collect {
            log(it)
        }
}

suspend fun xxxLatest() {
    val timeMillis = measureTimeMillis {
        produceInts(n = 5, delay = 100)
            .collectLatest { value ->
                log("Collecting $value")
                delay(300)
                log("done $value")
            }
    }
    log("CollectLatest finished in $timeMillis ms")
}

suspend fun conflation() {
    val timeMillis = measureTimeMillis {
        produceInts(n = 5, delay = 100)
            .conflate()
            .collect { value ->
                delay(300)
                log("Receiving $value")
            }
    }
    log("Conflation finished in $timeMillis ms")
}

fun flowNotBlockingThread(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(200)
        emit(i)
    }
}

private suspend fun flowOnOperator() {
    produceInts()
        .flowOn(Dispatchers.Default)
        .collect { log(it) }
}

private suspend fun comparingPerformance() = coroutineScope {
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
}

fun infiniteFlow(): Flow<Int> = flow {
    var i = 0

    println("Starting infinite flow")

    while (true) {
        delay(100)
        emit(i++)
    }
}

fun produceInts(n: Int = 3, delay: Long = 100): Flow<Int> {
    return (1..n).asFlow().transform { i ->
        delay(delay)
        log("Emitting $i")
        emit(i)
    }
}

fun requestFlow(number: Int): Flow<String> = flow {
    emit("$number: First")
    delay(500)
    emit("$number: Second")
}

suspend fun performRequest(request: Int): String {
    println("Performing request for id $request")
    delay((100 * request).toLong())
    return "Result for request $request"
}