package com.decisionlog.domain;

public enum ConversationState {
    IDLE,

    // /add flow
    WAITING_CONTEXT,
    WAITING_DECISION,
    WAITING_EXPECTED_RESULT,

    // /reflect flow
    WAITING_ACTUAL_RESULT,
    WAITING_LESSON
}
