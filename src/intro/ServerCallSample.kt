package intro

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.TimeUnit

fun main() {
    concurrency()
//    dupleCallSample()
}

private fun concurrency() {
    val tickers = listOf("GOOG", "AMZN", "MSFT")

    runBlocking {
        measureTime {
            val prices = mutableListOf<Deferred<String>>()

            for (ticker in tickers) {
                prices += async { "Price for $ticker is ${getStockPrice(ticker)}" }
            }

            for (price in prices) {
                println(price.await())
            }
        }
    }
}

suspend fun measureTime(block: suspend () -> Unit) {
    val start = System.nanoTime()
    block()
    val end = System.nanoTime()
    println("${TimeUnit.NANOSECONDS.toMillis(end - start)} ms")
}

private fun dupleCallSample() {
    runBlocking {
        launch {
            val ticker = "GOOG"
            try {
                println(Thread.currentThread())
                val price = getStockPrice(ticker)

                try {
                    val ip = getCallerIP()

                    println("Price for stock $ticker is $price, requesting from ip $ip")
                } catch (ex: Exception) {
                    println("Error getting the ip")
                }
            } catch (ex: Exception) {
                println("Error getting price for stock $ticker")
            }
        }
    }
}

suspend fun getCallerIP(): String {
    return URL("https://api.ipify.org").readText()
}

suspend fun getStockPrice(ticker: String): String {
    val url = when (ticker) {
        "GOOG" -> "http://www.mocky.io/v2/5df6387a3400006d00e5a54b"
        "AMZN" -> "http://www.mocky.io/v2/5df63eca340000223de5a553"
        else -> "http://www.mocky.io/v2/5df63ef83400002900e5a554"
    }
    return URL(url).readText()
}