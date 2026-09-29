package ru.pavlig43.peshehod.server.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ApiRootController {
    @GetMapping
    fun root(): Map<String, String> = mapOf("status" to "ready")
}
