package org.lupus.commands.core.utils

object ArrayUtil {
    fun getArgs(offset: Int, end: Int, args: Array<out String>): Array<out String> {
        var endOffset = end
        if (endOffset == -1)
            endOffset = args.size
        val length = endOffset-offset
        val arguments = Array(length) { "$it" }
        System.arraycopy(args, offset, arguments, 0, length)
        return arguments
    }
    fun getArgs(offset: Int, args: Array<out String>): Array<out String> {
        return getArgs(offset,-1, args)
    }
}