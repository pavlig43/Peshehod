package ru.pavlig43.peshehod.server.storage

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalPhotoStorageTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `stores loads and deletes a photo`() {
        val storage = LocalPhotoStorage(directory.toString())
        val content = byteArrayOf(1, 2, 3)

        val stored = storage.store("photo-1.jpg", content)

        assertTrue(stored.startsWith(directory))
        assertContentEquals(content, storage.load("photo-1.jpg"))
        assertTrue(storage.delete("photo-1.jpg"))
    }

    @Test
    fun `rejects path traversal`() {
        val storage = LocalPhotoStorage(directory.toString())
        assertFailsWith<IllegalArgumentException> {
            storage.store("../outside.jpg", byteArrayOf())
        }
    }
}
