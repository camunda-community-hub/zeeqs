package io.zeebe.zeeqs.data.service

data class MessageSubscriptionDefinition(
        val messageName: String?,
        val messageCorrelationKey: String?
)
