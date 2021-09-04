package org.lupus.commands.core.scanner

import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import org.lupus.commands.core.scanner.modifiers.any.*
import org.lupus.commands.core.scanner.modifiers.clazz.ContinuousMod
import org.lupus.commands.core.scanner.modifiers.clazz.HelpMod
import org.lupus.commands.core.scanner.modifiers.method.CMDPassMod
import org.lupus.commands.core.scanner.modifiers.method.DefaultMod
import org.lupus.commands.core.scanner.modifiers.method.NotCMDMod

object DefaultModifiers {
    val clazzMods = mutableListOf(ContinuousMod, HelpMod)
    val methodMods = mutableListOf(CMDPassMod, DefaultMod, NotCMDMod)
    val anyMods = mutableListOf(AliasesMod, AsyncMod, ConditionsMod, DescMod, NoPermModifier, SyntaxMod, PermModifier)
    val paramModifiers = mutableListOf<ParameterModifier>()
}