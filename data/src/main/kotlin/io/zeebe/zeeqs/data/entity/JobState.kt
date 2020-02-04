package io.zeebe.zeeqs.data.entity

enum class JobState {
    ACTIVATABLE,
    ACTIVATED,
    FAILED,
    COMPLETED,
    CANCELED,
    ERROR_THROWN
}