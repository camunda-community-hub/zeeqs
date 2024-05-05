package io.zeebe.zeeqs.data.entity

enum class EqualityOperation {
    EQUALS,
    CONTAINS
}

class VariableFilter (
        val name: String,
        val value: String,
        val equalityOperation: EqualityOperation = EqualityOperation.EQUALS
)