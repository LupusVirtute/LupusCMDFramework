package org.lupus.commands.core.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Method
import java.lang.reflect.Type

class MethodJsonSerializer : JsonSerializer<Method> {
    override fun serialize(method: Method?, type: Type?, serializationCtx: JsonSerializationContext?): JsonElement {
        method ?: return JsonPrimitive("null")
        val result = JsonObject()
        val annotationsArray = JsonArray(method.annotations.size)
        val parameterArray = JsonArray(method.parameters.size)

        result.add("name", JsonPrimitive(method.name))
        result.add("returnType", JsonPrimitive(method.returnType.name))

        method.annotations.forEach { annotationsArray.add(it.annotationClass.qualifiedName) }
        result.add("annotations", annotationsArray)

        method.parameters.forEach {
            val parameter = JsonObject()
            parameter.add("name", JsonPrimitive(it.name))
            parameter.add("type", JsonPrimitive(it.type.name))
            parameterArray.add(parameter)
        }
        result.add("parameters", parameterArray)
        return result
    }
}