package io.zeebe.zeeqs.importer.hazelcast

import com.google.protobuf.Struct
import com.google.protobuf.Value

object ProtobufTransformer {

    fun structToMap(struct: Struct): Map<String, String> {
        return struct.fieldsMap.mapValues { (_, value) -> valueToString(value) }
    }

    private fun valueToString(value: Value): String {
        return when (value.kindCase) {
            Value.KindCase.NULL_VALUE -> "null"
            Value.KindCase.BOOL_VALUE -> value.boolValue.toString()
            Value.KindCase.NUMBER_VALUE -> value.numberValue.toString()
            Value.KindCase.STRING_VALUE -> "\"${value.stringValue}\""
            Value.KindCase.LIST_VALUE -> value.listValue.valuesList.map { valueToString(it) }
                .joinToString(separator = ",", prefix = "[", postfix = "]")

            Value.KindCase.STRUCT_VALUE -> value.structValue.fieldsMap.map { (key, value) ->
                "\"$key\":" + valueToString(
                    value
                )
            }.joinToString(separator = ",", prefix = "{", postfix = "}")

            else -> value.toString()
        }
    }
    
}