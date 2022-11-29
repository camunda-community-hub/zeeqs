package io.zeebe.zeeqs.data.service

data class BpmnElementMetadata(
        val jobType: String? = null,
        val conditionExpression: String? = null,
        val timerDefinition: String? = null,
        val errorCode: String? = null,
        val calledProcessId: String? = null,
        val messageSubscriptionDefinition: MessageSubscriptionDefinition? = null,
        val assignee : String? = null,
        val candidateGroups : String? = null
)
