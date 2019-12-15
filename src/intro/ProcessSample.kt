package intro

fun process(n: Int): Int {
    println("process: ${Thread.currentThread()}")
    return n
}

fun main() {
    println(Thread.currentThread())
    println(process(2))
}