package ru.pavlig43.peshehod.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PeshehodServerApplication

fun main(args: Array<String>) {
    runApplication<PeshehodServerApplication>(*args)
}
