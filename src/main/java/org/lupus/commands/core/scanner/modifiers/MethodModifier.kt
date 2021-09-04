package org.lupus.commands.core.scanner.modifiers

import java.lang.reflect.Method

abstract class MethodModifier(annotation: Class<out Annotation>)
    : BaseModifier<Method>(annotation)