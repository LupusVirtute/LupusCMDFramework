package org.lupus.commands.core.scanner.modifiers

abstract class ClazzModifier(annotation: Class<out Annotation>)
    : BaseModifier<Class<out Any>>(annotation)