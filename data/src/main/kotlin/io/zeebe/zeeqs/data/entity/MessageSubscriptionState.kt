package io.zeebe.zeeqs.data.entity

enum class MessageSubscriptionState {
    CREATING,
    CREATED,
    CORRELATING,
    CORRELATED,
    REJECTED,
    DELETED
}