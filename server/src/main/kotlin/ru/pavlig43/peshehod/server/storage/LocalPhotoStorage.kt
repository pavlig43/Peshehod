package ru.pavlig43.peshehod.server.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class LocalPhotoStorage(
    @Value("\${peshehod.photo.directory}") directory: String,
) : PhotoStorage {
    private val root = Path.of(directory).toAbsolutePath().normalize()

    override fun store(photoId: String, content: ByteArray): Path {
        val target = resolve(photoId)
        Files.createDirectories(root)
        Files.write(target, content)
        return target
    }

    override fun load(photoId: String): ByteArray = Files.readAllBytes(resolve(photoId))

    override fun delete(photoId: String): Boolean = Files.deleteIfExists(resolve(photoId))

    private fun resolve(photoId: String): Path {
        require(photoId.matches(Regex("[A-Za-z0-9._-]+"))) { "Invalid photo id" }
        return root.resolve(photoId).normalize().also {
            require(it.startsWith(root)) { "Photo path escapes storage root" }
        }
    }
}
