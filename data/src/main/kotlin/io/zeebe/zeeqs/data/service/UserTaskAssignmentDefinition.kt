package io.zeebe.zeeqs.data.service

data class UserTaskAssignmentDefinition(
        val assignee : String? = null,
        val candidateGroups : String? = null
)
