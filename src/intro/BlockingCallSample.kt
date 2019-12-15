package intro

import kotlin.sequences.sequence

val sequence = sequence {
    println("one")
    yield(1)

    println("two")
    yield(2)

    println("three")
    yield(3)

    println("four")
    yield(4)

    println("done")
}

fun main() {
    for (value in sequence) {
        println("The value is $value")
    }
}