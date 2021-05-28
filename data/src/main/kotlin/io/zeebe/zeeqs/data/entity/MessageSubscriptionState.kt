package io.zeebe.zeeqs.data.entity

enum class MessageSubscriptionState {
    CREATED,
    CORRELATING,
    CORRELATED,
    REJECTED,
    DELETED
}