package ru.pavlig43.peshehod.server.storage

import java.nio.file.Path

interface PhotoStorage {
    fun store(photoId: String, content: ByteArray): Path
    fun load(photoId: String): ByteArray
    fun delete(photoId: String): Boolean
}
