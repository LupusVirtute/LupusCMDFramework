package org.lupus.commands.core.utils

import java.time.Instant

object TimeUtil {
    val numberRegex = "\\d*".toRegex()
    val timeRegex = "(\\d*mo)|(\\d*d)|(\\d*h)|(\\d*m)|(\\d*s)".toRegex()
    fun stringTimeToInstant(input: String): Instant {
        return Instant.now().plusSeconds(stringTimeToSeconds(input))
    }
    fun stringTimeToSeconds(input: String): Long {
        val values = timeRegex.findAll(input)
        var seconds = 0L
        for (value in values) {
            val output = value.value
            for (timeClassifier in timeClassifiers) {
                val classifierOutput = classifyAndCheck(timeClassifier, output)
                if (classifierOutput != 0L) {
                    seconds += classifierOutput
                    continue
                }
            }
        }
        return seconds
    }

    /**
     * Gets time
     */
    fun epochSecondToString(input: Instant): String {
        var epochSecond =  input.epochSecond - Instant.now().epochSecond
        val builder = StringBuilder()
        for (timeClassifier in timeClassifiers) {
            var calcRest = epochSecond
            calcRest /= timeClassifier.numberValue
            if (calcRest <= 0)
                continue
            val output = timeClassifier.parse(calcRest)
            epochSecond -= calcRest*timeClassifier.numberValue
            builder.append(output).append(' ')
        }
		builder.removeSuffix(" ")
        return builder.toString()
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
        fun parse(input: Long): String {

            return "$input$identifier".replace("$","")
        }
        fun classifies(input: String): Boolean {
            return input.contains(identifier.toRegex())
        }
    }
}
