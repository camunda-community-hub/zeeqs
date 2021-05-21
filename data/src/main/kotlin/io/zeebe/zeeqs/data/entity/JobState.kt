package io.zeebe.zeeqs.data.entity

enum class JobState {
    ACTIVATABLE,
    FAILED,
    COMPLETED,
    CANCELED,
    ERROR_THROWN
}