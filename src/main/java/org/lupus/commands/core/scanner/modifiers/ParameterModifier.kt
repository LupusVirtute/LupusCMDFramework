package org.lupus.commands.core.scanner.modifiers

import java.lang.reflect.Parameter

abstract class ParameterModifier(annotation: Class<out Annotation>)
    : BaseModifier<Parameter>(annotation)