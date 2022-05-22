package org.lupus.commands.core.data

import com.google.gson.*
import java.lang.reflect.Method
import java.lang.reflect.Type

class CommandLupiSerializer : JsonSerializer<CommandLupi> {
    override fun serialize(command: CommandLupi?, type: Type?, serializationContext: JsonSerializationContext?): JsonElement {
        command ?: return JsonPrimitive("null")
        val result = JsonObject()
        result.add("name", JsonPrimitive(command.name))
        result.add("fullName", JsonPrimitive(command.fullName))
        result.add("desc", JsonPrimitive(command.description))
        result.add("syntax", JsonPrimitive(command.syntax))
        result.add("permission", JsonPrimitive(command.permission))
        result.add("pluginRegistering", JsonPrimitive(command.pluginRegistering.name))

        val gson = GsonBuilder()
            .registerTypeAdapter(Method::class.java, MethodJsonSerializer())
            .create()
        result.add("method", gson.toJsonTree(command.method))

        val subCmdsArray = JsonArray(command.subCommands.size)
        command.subCommands.forEach { subCmdsArray.add(it.toGsonTree()) }

        result.add("subCommands", subCmdsArray)


        return result
    }
}