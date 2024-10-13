package io.zeebe.zeeqs.data.entity

enum class ComparisonOperation {
    EQUALS,
    CONTAINS
}

enum class FilterOperation {
    AND,
    OR
}

class VariableFilter (
        val name: String,
        val value: String,
        val comparisonOperation: ComparisonOperation = ComparisonOperation.EQUALS
)

class VariableFilterGroup (
        val variables: List<VariableFilter>,
        val filterOperation: FilterOperation = FilterOperation.OR
)