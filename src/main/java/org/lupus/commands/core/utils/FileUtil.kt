package org.lupus.commands.core.utils

import java.io.File

object FileUtil {
    fun dumpToFile(input: String, file: File) {
        file.createNewFile()
        file.writeText(
            input,
            Charsets.UTF_8
        )
    }
}