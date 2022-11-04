package org.lupus.commands.core.scanner.modifiers

import java.lang.reflect.Field

abstract class FieldsModifier(annotation: Class<out Annotation>) : BaseModifier<Field>(annotation) {
}
