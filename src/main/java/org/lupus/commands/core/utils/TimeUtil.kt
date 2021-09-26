package org.lupus.commands.core.utils

import java.time.Instant

object TimeUtil {
    val numberRegex = "[0-9]*".toRegex()
    val timeRegex = "([0-9]*mo)|([0-9]*d)|([0-9]*h)|([0-9]*m)|([0-9]*s)".toRegex()
    fun stringTimeToInstant(input: String): Instant {
        val values = timeRegex.findAll(input)
        var instant = Instant.now()
        for (value in values) {
            val output = value.value
            for (timeClassifier in timeClassifiers) {
                val classifierOutput = classifyAndCheck(timeClassifier, output)
                if (classifierOutput != 0L) {
                    instant = instant.plusSeconds(classifierOutput)
                    continue
                }
            }
        }
        return instant
    }
    private fun classifyAndCheck(classifier: TimeClassifier, input: String): Long {
        if (!classifier.classifies(input)) return 0
        return classifier.parse(input)
    }

    val timeClassifiers: List<TimeClassifier> = listOf(
        TimeClassifier("mo\$", 3600L*24*30),
        TimeClassifier("d\$", 3600L*24),
        TimeClassifier("h\$", 3600L),
        TimeClassifier("m\$", 60L),
        TimeClassifier("s\$", 1L),


        )



    class TimeClassifier(
        val identifier: String,
        val numberValue: Long
    ) {
        fun parse(input: String): Long {
            val output = numberRegex.find(input)!!.value.toLong()
            return output * numberValue
        }
        fun classifies(input: String): Boolean {
            return input.contains(identifier.toRegex())
        }
    }
}