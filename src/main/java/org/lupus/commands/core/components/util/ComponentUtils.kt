package org.lupus.commands.core.components.util

import org.lupus.commands.core.components.Component
import java.lang.reflect.Method

class ComponentUtils {
    fun getComponentProcessors(component: Component): List<Method> {
        val methods = component::class.java.declaredMethods
        return methods.filter { it.canAccess(component) }
    }
}