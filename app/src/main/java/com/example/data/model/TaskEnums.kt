package com.example.data.model

enum class TaskStatus(val order: Int) {
    TODO(0),
    IN_PROGRESS(1),
    DONE(2)
}

enum class TaskPriority(val weight: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    CRITICAL(3)
}

enum class TaskCategory {
    FEATURE,
    BUG,
    REFACTOR,
    DEVOPS,
    DOCS
}
